#!/usr/bin/env bash

set -Eeuo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_REPO="$ROOT_DIR/backend"
BACKEND_APP="$BACKEND_REPO/nosql_lab1"
FRONTEND_DIR="$ROOT_DIR/frontend"
BACKEND_ENV="$BACKEND_REPO/.env"
FRONTEND_LOG="$FRONTEND_DIR/vite.log"
FRONTEND_PID="$FRONTEND_DIR/.vite.pid"
COMPOSE_PROJECT="nosql_lab1"

info() {
  printf '\033[1;34m→\033[0m %s\n' "$1"
}

success() {
  printf '\033[1;32m✓\033[0m %s\n' "$1"
}

fail() {
  printf '\033[1;31m✗\033[0m %s\n' "$1" >&2
  exit 1
}

require_command() {
  command -v "$1" >/dev/null 2>&1 || fail "Не найдена команда: $1"
}

env_value() {
  local key="$1"
  local fallback="$2"
  local value=""

  if [[ -f "$BACKEND_ENV" ]]; then
    value="$(sed -n "s/^${key}=//p" "$BACKEND_ENV" | tail -n 1)"
  fi

  printf '%s' "${value:-$fallback}"
}

wait_for_url() {
  local name="$1"
  local url="$2"
  local attempts="${3:-90}"

  for ((attempt = 1; attempt <= attempts; attempt++)); do
    if curl --fail --silent --show-error "$url" >/dev/null 2>&1; then
      success "$name готов"
      return 0
    fi
    sleep 1
  done

  return 1
}

on_error() {
  fail "Запуск прерван. Проверьте $FRONTEND_LOG и команду: cd $BACKEND_REPO && docker compose -p $COMPOSE_PROJECT logs"
}

trap on_error ERR

require_command docker
require_command npm
require_command curl

docker compose version >/dev/null 2>&1 || fail "Docker Compose недоступен"
[[ -x "$BACKEND_APP/gradlew" ]] || fail "Не найден Gradle wrapper: $BACKEND_APP/gradlew"
[[ -f "$FRONTEND_DIR/package.json" ]] || fail "Не найден frontend/package.json"

if [[ ! -f "$BACKEND_ENV" ]]; then
  [[ -f "$BACKEND_REPO/.env.example" ]] || fail "Не найден backend/.env.example"
  cp "$BACKEND_REPO/.env.example" "$BACKEND_ENV"
  info "Создан backend/.env из примера"
fi

BACKEND_PORT="$(env_value PORT_BACKEND 8080)"
KEYCLOAK_PORT="$(env_value PORT_KEYCLOAK 8081)"
FRONTEND_PORT="${FRONTEND_PORT:-5173}"

BACKEND_URL="http://localhost:$BACKEND_PORT"
KEYCLOAK_URL="http://localhost:$KEYCLOAK_PORT"
FRONTEND_URL="http://localhost:$FRONTEND_PORT"
KEYCLOAK_ADMIN_USER="$(env_value KEYCLOAK_ADMIN admin)"
KEYCLOAK_ADMIN_PASSWORD="$(env_value KEYCLOAK_PASSWORD admin)"

info "Собираю Spring Boot"
(
  cd "$BACKEND_APP"
  ./gradlew bootJar --no-daemon
)

info "Поднимаю PostgreSQL, Redis, Keycloak и backend"
(
  cd "$BACKEND_REPO"
  docker compose -p "$COMPOSE_PROJECT" up --build -d
  docker compose -p "$COMPOSE_PROJECT" up -d --force-recreate --no-deps backend
)

if [[ ! -d "$FRONTEND_DIR/node_modules" ]]; then
  info "Устанавливаю зависимости frontend"
  (
    cd "$FRONTEND_DIR"
    npm install
  )
fi

if curl --fail --silent "$FRONTEND_URL" 2>/dev/null | grep -q '<title>Кагэ'; then
  success "Frontend уже запущен"
elif curl --fail --silent "$FRONTEND_URL" >/dev/null 2>&1; then
  fail "Порт $FRONTEND_PORT занят другим приложением"
else
  info "Запускаю frontend"
  (
    cd "$FRONTEND_DIR"
    nohup env VITE_BACKEND_URL="$BACKEND_URL" npm run dev -- --host 0.0.0.0 --port "$FRONTEND_PORT" >"$FRONTEND_LOG" 2>&1 &
    printf '%s\n' "$!" >"$FRONTEND_PID"
  )
fi

wait_for_url "Keycloak" "$KEYCLOAK_URL/realms/nosql-lab1/.well-known/openid-configuration" || fail "Keycloak не ответил вовремя"

info "Включаю самостоятельную регистрацию пользователей"
(
  cd "$BACKEND_REPO"
  docker compose -p "$COMPOSE_PROJECT" exec -T keycloak \
    /opt/keycloak/bin/kcadm.sh config credentials \
    --config /tmp/kage-kcadm.config \
    --server http://localhost:8080 \
    --realm master \
    --user "$KEYCLOAK_ADMIN_USER" \
    --password "$KEYCLOAK_ADMIN_PASSWORD" >/dev/null
  docker compose -p "$COMPOSE_PROJECT" exec -T keycloak \
    /opt/keycloak/bin/kcadm.sh update realms/nosql-lab1 \
    --config /tmp/kage-kcadm.config \
    -s registrationAllowed=true >/dev/null
)
success "Регистрация пользователей включена"

wait_for_url "Backend" "$BACKEND_URL/auth/config" || fail "Backend не ответил вовремя"
wait_for_url "Frontend" "$FRONTEND_URL" || fail "Frontend не ответил вовремя"

trap - ERR

printf '\n\033[1;32mВсе сервисы запущены.\033[0m\n\n'
printf '  Frontend:  \033[1;36m%s\033[0m\n' "$FRONTEND_URL"
printf '  Backend:   %s\n' "$BACKEND_URL"
printf '  Keycloak:  %s\n\n' "$KEYCLOAK_URL"
printf '  Менеджер:  manager / manager\n'
printf '  Читатель:  reader / reader\n\n'
