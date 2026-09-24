# Frontend API specification

Base URL: `http://localhost:8080`.

JSON uses `snake_case`. UUID values are strings. Date-time values are ISO-8601 UTC. Every `/api/**` endpoint requires `Authorization: Bearer <access_token>`; only `/auth/**` endpoints are public.

## Authentication

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/auth/config` | Returns `keycloak_base_url` and `client_id`. |
| `POST` | `/auth/callback` | Exchanges authorization code for tokens. |
| `POST` | `/auth/refresh` | Refreshes access token. |

`POST /auth/callback`:

```json
{ "code": "...", "redirect_uri": "http://frontend/callback" }
```

Response fields: `access_token`, `refresh_token`, `expires_in`, `refresh_expires_in`, `token_type`.

> Configure the Keycloak realm in the frontend environment: `/auth/config` does not return it yet.

## Users

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/users/{id}` | Get user. |
| `GET` | `/api/users/by-email?email=` | Find the application profile after OAuth login. |
| `POST` | `/api/users` | Create user. |
| `PUT` | `/api/users/{id}` | Update user. |

```json
{ "name": "Ivan Ivanov", "email": "ivan@example.com", "role": "USER" }
```

Response fields: `id`, `name`, `email`, `role`. Roles: `USER`, `MANAGER`.

## Events

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/events?title=&page=0&size=20` | List published events; search by title substring. |
| `GET` | `/api/events/{id}` | Get event. |
| `POST` | `/api/events` | Create event. |
| `PUT` | `/api/events/{id}` | Update event. |

`page` starts at `0`; `size` is from `1` to `100`. Statuses: `DRAFTED`, `PUBLISHED`, `CANCELLED`, `FINISHED`.

```json
{
  "title": "Meeting with an author",
  "description": "Discussion of a new book",
  "event_date": "2026-10-10T15:00:00Z",
  "location": "Main hall",
  "capacity": 50,
  "price": 300.00,
  "image_key": "lanterns",
  "created_by": "manager-uuid",
  "status": "DRAFTED"
}
```

Event response fields: `id`, `title`, `description`, `event_date`, `location`, `capacity`, `reserved_seats`, `available_seats`, `price`, `image_key`, `status`, `created_by`.

Lists are Spring Pages with `content`, `total_elements`, `total_pages`, `size`, and `number`.

## Orders

One order belongs to one event. `quantity` is the number of reserved seats.

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/orders` | Create order. |
| `GET` | `/api/orders/user/{userId}?page=0&size=20` | List user orders. |
| `GET` | `/api/orders/event/{eventId}?page=0&size=20` | List event orders. |
| `PATCH` | `/api/orders/{id}/status` | Confirm or cancel order. |

Create request:

```json
{ "user_id": "user-uuid", "event_id": "event-uuid", "quantity": 2 }
```

Response fields: `id`, `user_id`, `event_id`, `quantity`, `total_price`, `created_at`, `status`.

Statuses: `CREATED`, `CONFIRMED`, `CANCELLED`.

Status change request:

```json
{ "order_id": "the-same-id-as-in-path", "status": "CONFIRMED" }
```

The backend calculates the price, allows only `PUBLISHED` events, prohibits duplicate active orders, reserves seats on creation, and releases them on cancellation. A concurrent booking conflict returns `409`.

## User settings

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/api/user-settings/{userId}` | Get settings. |
| `PUT` | `/api/user-settings/{userId}` | Create or fully replace settings. |

```json
{
  "user_id": "user-uuid",
  "notifications_active": true,
  "language": "ru",
  "preferred_category": "Classics"
}
```

Settings are cached with cache-aside in Redis under `user-settings:v1:{userId}` with a 30-minute TTL.

## Draft orders in Redis

| Method | Path | Purpose |
| --- | --- | --- |
| `PUT` | `/api/orders/drafts/{userId}/{eventId}` | Create or update draft. |
| `GET` | `/api/orders/drafts/{userId}/{eventId}` | Get draft. |

```json
{ "quantity": 2 }
```

Draft response fields: `event_id`, `quantity`, `created_at`.

Drafts use key `order-draft:v1:{userId}:{eventId}`, expire after 15 minutes, and do not reserve seats.

## Event views in Redis

| Method | Path | Purpose |
| --- | --- | --- |
| `POST` | `/api/events/{eventId}/views` | Atomically increment views. |
| `GET` | `/api/events/{eventId}/views` | Get current views. |

```json
{ "event_id": "event-uuid", "views": 42 }
```

Views use Redis key `event-views:v1:{eventId}` and atomic `INCR`.

## Errors and current limitations

| Status | Meaning |
| --- | --- |
| `400` | Invalid request. |
| `401` | Missing or invalid access token. |
| `404` | Resource not found. |
| `409` | Business or concurrent update conflict. |
| `500` | Unexpected server error. |

Optimistic-locking failures return Spring `ProblemDetail` with `409`.

Role-based access is not enforced yet. APIs currently accept `userId` from path/body; it should later be derived from JWT. CORS is not configured yet.
