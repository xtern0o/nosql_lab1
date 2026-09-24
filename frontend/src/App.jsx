import { useEffect, useMemo, useState } from 'react'
import {
  ArrowLeft,
  ArrowRight,
  Bell,
  BookOpen,
  CalendarDays,
  Check,
  ChevronLeft,
  ChevronRight,
  CircleUserRound,
  Clock3,
  Eye,
  Feather,
  LogIn,
  LogOut,
  MapPin,
  Menu,
  Minus,
  MoonStar,
  Plus,
  Search,
  Settings,
  Shuffle,
  ShoppingBag,
  Sparkles,
  Ticket,
  UserPlus,
  Users,
  X,
} from 'lucide-react'
import heroImage from './assets/hero-library.png'
import lanternsPoster from './assets/poster-lanterns.png'
import mangaPoster from './assets/poster-manga.png'
import musicPoster from './assets/poster-music.png'
import {
  api,
  beginLogin,
  beginLogout,
  beginRegistration,
  clearSession,
  decodeToken,
  finishLogin,
  getStoredUser,
  getTokens,
  saveUser,
} from './api.js'

const posterPool = [
  { key: 'lanterns', label: 'Алые фонари', src: lanternsPoster },
  { key: 'manga', label: 'Чернильная мастерская', src: mangaPoster },
  { key: 'music', label: 'Ночная поэзия', src: musicPoster },
]
const postersByKey = Object.fromEntries(posterPool.map((poster) => [poster.key, poster]))

const randomPosterKey = (previousKey) => {
  const candidates = posterPool.filter((poster) => poster.key !== previousKey)
  return candidates[Math.floor(Math.random() * candidates.length)].key
}

const getEventPoster = (event, fallbackIndex = 0) => {
  const selectedPoster = postersByKey[event?.image_key]
  if (selectedPoster) return selectedPoster.src

  const seed = String(event?.id || event?.title || fallbackIndex)
  const hash = [...seed].reduce((value, character) => ((value * 31) + character.charCodeAt(0)) >>> 0, 0)
  return posterPool[hash % posterPool.length].src
}

const createEmptyPosterForm = () => ({
  title: '',
  description: '',
  event_date: '',
  location: '',
  capacity: 30,
  price: 0,
  status: 'PUBLISHED',
  image_key: randomPosterKey(),
})

const demoEvents = [
  {
    id: 'demo-path',
    title: 'Путь героя: как рождается история',
    description: 'Разбираем путь персонажа, учимся видеть внутренний конфликт и создаём героя для собственной повести.',
    event_date: '2026-10-10T15:00:00Z',
    location: 'Зал свитков, 2 этаж',
    capacity: 48,
    reserved_seats: 31,
    available_seats: 17,
    price: 300,
    image_key: 'lanterns',
    status: 'PUBLISHED',
    views: 128,
  },
  {
    id: 'demo-mask',
    title: 'Под маской: клуб городской фантастики',
    description: 'Вечер о двойной жизни героев, моральном выборе и образе города в современной японской прозе.',
    event_date: '2026-10-16T17:30:00Z',
    location: 'Тёмный читальный зал',
    capacity: 36,
    reserved_seats: 27,
    available_seats: 9,
    price: 450,
    image_key: 'manga',
    status: 'PUBLISHED',
    views: 243,
  },
  {
    id: 'demo-poetry',
    title: 'Луна над страницами',
    description: 'Камерные чтения стихов под фонарями. Можно прийти слушателем или прочитать собственный текст.',
    event_date: '2026-10-24T18:00:00Z',
    location: 'Сад на крыше',
    capacity: 60,
    reserved_seats: 18,
    available_seats: 42,
    price: 0,
    image_key: 'music',
    status: 'PUBLISHED',
    views: 91,
  },
]

const statusLabels = {
  DRAFTED: 'Черновик',
  PUBLISHED: 'Опубликовано',
  CANCELLED: 'Отменено',
  FINISHED: 'Завершено',
  CREATED: 'Создана',
  CONFIRMED: 'Подтверждена',
}

const formatDate = (value, withTime = true) => {
  if (!value) return 'Дата уточняется'
  return new Intl.DateTimeFormat('ru-RU', {
    day: 'numeric',
    month: 'long',
    ...(withTime ? { hour: '2-digit', minute: '2-digit' } : {}),
  }).format(new Date(value))
}

const money = (value) => Number(value || 0) === 0
  ? 'Бесплатно'
  : `${new Intl.NumberFormat('ru-RU').format(Number(value))} ₽`

const MAX_EVENT_PRICE = 9_999_999_999.99
const MAX_EVENT_CAPACITY = 2_147_483_647

function App() {
  const [tokens, setTokens] = useState(getTokens())
  const [profile, setProfile] = useState(getStoredUser())
  const [page, setPage] = useState('home')
  const [events, setEvents] = useState(tokens ? [] : demoEvents)
  const [pageInfo, setPageInfo] = useState({ number: 0, total_pages: 1, total_elements: 3 })
  const [search, setSearch] = useState('')
  const [selected, setSelected] = useState(null)
  const [eventViews, setEventViews] = useState(0)
  const [orders, setOrders] = useState([])
  const [settings, setSettings] = useState(null)
  const [loading, setLoading] = useState(Boolean(tokens))
  const [message, setMessage] = useState(null)
  const [menuOpen, setMenuOpen] = useState(false)

  const claims = useMemo(() => decodeToken(tokens?.access_token), [tokens])
  const isManager = Boolean(
    profile?.role === 'MANAGER' || claims?.realm_access?.roles?.some((role) => role.toLowerCase() === 'manager'),
  )

  useEffect(() => {
    const params = new URLSearchParams(window.location.search)
    const code = params.get('code')
    const authError = params.get('error_description') || params.get('error')
    if (authError) {
      window.history.replaceState({}, '', '/')
      showMessage(`Keycloak: ${authError}`, 'error')
      return
    }
    if (!code) return
    window.history.replaceState({}, '', '/')
    setLoading(true)
    finishLogin(code)
      .then((nextTokens) => {
        setTokens(nextTokens)
        showMessage('Добро пожаловать в библиотеку «Кагэ»', 'success')
      })
      .catch((error) => showMessage(error.message, 'error'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!tokens) {
      setEvents(demoEvents)
      return
    }
    ensureProfile()
    loadEvents(0, search)
  }, [tokens])

  useEffect(() => {
    if (!selected || selected.id.startsWith('demo-') || !tokens) {
      setEventViews(selected?.views || 0)
      return
    }
    Promise.allSettled([
      api(`/api/events/${selected.id}/views`, { method: 'POST' }),
      api(`/api/events/${selected.id}/views`),
    ]).then((results) => {
      const latest = [...results].reverse().find((result) => result.status === 'fulfilled')
      if (latest) setEventViews(latest.value.views)
    })
  }, [selected?.id])

  const showMessage = (text, type = 'info') => {
    setMessage({ text, type })
    window.clearTimeout(showMessage.timer)
    showMessage.timer = window.setTimeout(() => setMessage(null), 4200)
  }

  const ensureProfile = async () => {
    const stored = getStoredUser()
    if (stored) {
      setProfile(stored)
      return stored
    }
    const tokenClaims = decodeToken(getTokens()?.access_token)
    if (!tokenClaims) return null
    const role = tokenClaims.realm_access?.roles?.includes('manager') ? 'MANAGER' : 'USER'
    const email = tokenClaims.email || `${tokenClaims.preferred_username}@kage.local`
    try {
      const existingUser = await api(`/api/users/by-email?email=${encodeURIComponent(email)}`)
      saveUser(existingUser)
      setProfile(existingUser)
      return existingUser
    } catch {
      // The first login has no application profile yet.
    }
    try {
      const user = await api('/api/users', {
        method: 'POST',
        body: JSON.stringify({
          name: tokenClaims.name || tokenClaims.preferred_username || 'Читатель',
          email,
          role,
        }),
      })
      saveUser(user)
      setProfile(user)
      return user
    } catch (error) {
      showMessage(`Не удалось создать профиль: ${error.message}`, 'error')
      return null
    }
  }

  const loadEvents = async (number = 0, query = search) => {
    if (!getTokens()) return
    setLoading(true)
    try {
      const params = new URLSearchParams({ page: String(number), size: '9' })
      if (query.trim()) params.set('title', query.trim())
      const result = await api(`/api/events?${params}`)
      setEvents(result.content || [])
      setPageInfo(result)
    } catch (error) {
      showMessage(error.message, 'error')
    } finally {
      setLoading(false)
    }
  }

  const loadOrders = async () => {
    const currentProfile = profile || await ensureProfile()
    if (!currentProfile) return
    setLoading(true)
    try {
      const result = await api(`/api/orders/user/${currentProfile.id}?page=0&size=50`)
      setOrders(result.content || [])
    } catch (error) {
      showMessage(error.message, 'error')
    } finally {
      setLoading(false)
    }
  }

  const loadSettings = async () => {
    const currentProfile = profile || await ensureProfile()
    if (!currentProfile) return
    try {
      const result = await api(`/api/user-settings/${currentProfile.id}`)
      setSettings(result)
    } catch {
      setSettings({
        user_id: currentProfile.id,
        notifications_active: true,
        language: 'ru',
        preferred_category: 'Фэнтези',
      })
    }
  }

  const navigate = (next) => {
    setPage(next)
    setMenuOpen(false)
    setSelected(null)
    window.scrollTo({ top: 0, behavior: 'smooth' })
    if (next === 'orders') loadOrders()
    if (next === 'settings') loadSettings()
  }

  const logout = () => {
    setTokens(null)
    setProfile(null)
    setEvents(demoEvents)
    setPage('home')
    beginLogout().catch((error) => {
      clearSession()
      showMessage(`Не удалось завершить сессию Keycloak: ${error.message}`, 'error')
    })
  }

  const requireLogin = () => {
    showMessage('Войдите, чтобы оформить заявку', 'info')
    beginLogin().catch((error) => showMessage(`Бэкенд недоступен: ${error.message}`, 'error'))
  }

  return (
    <div className="app-shell">
      <Header
        page={page}
        navigate={navigate}
        tokens={tokens}
        profile={profile}
        isManager={isManager}
        onLogin={() => beginLogin().catch((error) => showMessage(error.message, 'error'))}
        onRegister={() => beginRegistration().catch((error) => showMessage(error.message, 'error'))}
        onLogout={logout}
        menuOpen={menuOpen}
        setMenuOpen={setMenuOpen}
      />

      <main>
        {page === 'home' && (
          <Home
            heroImage={heroImage}
            events={tokens ? events.slice(0, 3) : demoEvents}
            tokens={tokens}
            onExplore={() => navigate('catalog')}
            onLogin={requireLogin}
            onRegister={() => beginRegistration().catch((error) => showMessage(error.message, 'error'))}
            onSelect={setSelected}
            loading={loading}
          />
        )}

        {page === 'catalog' && (
          <Catalog
            events={events}
            tokens={tokens}
            search={search}
            setSearch={setSearch}
            pageInfo={pageInfo}
            onSearch={(event) => { event.preventDefault(); loadEvents(0, search) }}
            onPage={(number) => loadEvents(number, search)}
            onSelect={setSelected}
            onLogin={requireLogin}
            onRegister={() => beginRegistration().catch((error) => showMessage(error.message, 'error'))}
            loading={loading}
          />
        )}

        {page === 'orders' && (
          <Orders
            orders={orders}
            events={events}
            loading={loading}
            onChange={async (order, status) => {
              try {
                await api(`/api/orders/${order.id}/status`, {
                  method: 'PATCH',
                  body: JSON.stringify({ order_id: order.id, status }),
                })
                showMessage(status === 'CANCELLED' ? 'Заявка отменена' : 'Заявка подтверждена', 'success')
                loadOrders()
                loadEvents(pageInfo.number || 0, search)
              } catch (error) {
                showMessage(error.message, 'error')
              }
            }}
          />
        )}

        {page === 'settings' && (
          <SettingsPage
            settings={settings}
            profile={profile}
            onSave={async (form) => {
              try {
                const result = await api(`/api/user-settings/${profile.id}`, {
                  method: 'PUT',
                  body: JSON.stringify({ ...form, user_id: profile.id }),
                })
                setSettings(result)
                showMessage('Настройки сохранены в Redis-кэше', 'success')
              } catch (error) {
                showMessage(error.message, 'error')
              }
            }}
          />
        )}

        {page === 'manage' && tokens && (
          <PosterStudio
            events={events}
            profile={profile}
            onSaved={() => {
              loadEvents(0, '')
              showMessage('Афиша сохранена', 'success')
            }}
            onError={(error) => showMessage(error.message, 'error')}
          />
        )}
      </main>

      <Footer navigate={navigate} />

      {selected && (
        <EventModal
          event={selected}
          image={getEventPoster(selected, events.indexOf(selected))}
          views={eventViews}
          tokens={tokens}
          profile={profile}
          onClose={() => setSelected(null)}
          onLogin={requireLogin}
          onBooked={() => {
            setSelected(null)
            showMessage('Заявка создана. Место зарезервировано!', 'success')
            loadEvents(pageInfo.number || 0, search)
            navigate('orders')
          }}
          onNotify={showMessage}
        />
      )}

      {loading && <div className="top-loader" aria-label="Загрузка" />}
      {message && <Toast {...message} onClose={() => setMessage(null)} />}
    </div>
  )
}

function Header({ page, navigate, tokens, profile, isManager, onLogin, onRegister, onLogout, menuOpen, setMenuOpen }) {
  const links = [
    ['home', 'Главная'],
    ['catalog', 'Афиша'],
    ...(tokens ? [['orders', 'Мои заявки'], ['settings', 'Настройки']] : []),
    ...(tokens ? [['manage', 'Создать афишу']] : []),
  ]
  return (
    <header className="site-header">
      <button className="brand" onClick={() => navigate('home')} aria-label="На главную">
        <span className="brand-mark"><MoonStar size={22} /></span>
        <span><b>КАГЭ</b><small>библиотека событий</small></span>
      </button>
      <nav className={menuOpen ? 'nav-links open' : 'nav-links'}>
        {links.map(([id, label]) => (
          <button className={page === id ? 'active' : ''} onClick={() => navigate(id)} key={id}>{label}</button>
        ))}
      </nav>
      <div className="header-actions">
        {tokens ? (
          <>
            <div className="profile-chip">
              <CircleUserRound size={18} />
              <span>{profile?.name?.split(' ')[0] || 'Читатель'}</span>
              {isManager && <small>менеджер</small>}
            </div>
            <button className="icon-button" onClick={onLogout} title="Выйти"><LogOut size={19} /></button>
          </>
        ) : (
          <div className="auth-entry-actions">
            <button className="button button-ghost button-small" onClick={onRegister}><UserPlus size={17} /> Регистрация</button>
            <button className="button button-small" onClick={onLogin}><LogIn size={17} /> Войти</button>
          </div>
        )}
        <button className="menu-button" onClick={() => setMenuOpen(!menuOpen)} aria-label="Меню">
          {menuOpen ? <X /> : <Menu />}
        </button>
      </div>
    </header>
  )
}

function Home({ heroImage, events, tokens, onExplore, onLogin, onRegister, onSelect, loading }) {
  return (
    <>
      <section className="hero" style={{ '--hero-image': `url(${heroImage})` }}>
        <div className="hero-glow" />
        <div className="hero-copy reveal">
          <p className="eyebrow"><span /> библиотека пробуждается после заката</p>
          <h1>Твоя история<br />начинается <em>здесь</em></h1>
          <p className="hero-text">Встречи с авторами, клубы современной прозы и вечера, после которых книга продолжает жить внутри.</p>
          <div className="hero-actions">
            <button className="button" onClick={onExplore}>Смотреть афишу <ArrowRight size={18} /></button>
            {!tokens && <button className="button button-ghost" onClick={onRegister}><UserPlus size={18} /> Создать аккаунт</button>}
          </div>
          <div className="hero-meta">
            <div><b>03</b><span>события этой осенью</span></div>
            <i />
            <div><b>24/7</b><span>черновик хранится 15 минут</span></div>
          </div>
        </div>
        <div className="vertical-note">影の図書館 · KAGE LIBRARY</div>
        <button className="scroll-cue" onClick={onExplore}><span>листай</span><ArrowRight size={16} /></button>
      </section>

      <section className="section featured-section">
        <SectionHeading eyebrow="ближайшие встречи" title="Афиша этой главы" action="Вся афиша" onAction={onExplore} />
        <EventGrid events={events} onSelect={onSelect} loading={loading} />
      </section>

      <section className="section ritual-section">
        <div className="ritual-copy">
          <p className="eyebrow"><span /> твой путь читателя</p>
          <h2>Три шага к новой истории</h2>
          <p>Сохрани заявку как черновик, вернись к ней в течение 15 минут и подтверди участие, когда будешь готов.</p>
        </div>
        <div className="steps">
          <div className="step"><b>壱</b><span><strong>Выбери событие</strong><small>Найди встречу по настроению</small></span></div>
          <div className="step"><b>弐</b><span><strong>Сохрани черновик</strong><small>Количество мест запомнит Redis</small></span></div>
          <div className="step"><b>参</b><span><strong>Подтверди заявку</strong><small>И место станет твоим</small></span></div>
        </div>
      </section>
    </>
  )
}

function Catalog({ events, tokens, search, setSearch, pageInfo, onSearch, onPage, onSelect, onLogin, onRegister, loading }) {
  return (
    <section className="page-section section">
      <div className="page-intro">
        <p className="eyebrow"><span /> каталог событий</p>
        <h1>Выбери свою следующую главу</h1>
        <p>{tokens ? 'Живые данные из PostgreSQL. Просмотры и черновики работают через Redis.' : 'Войди в аккаунт, чтобы увидеть актуальные события и оформить заявку.'}</p>
      </div>
      <form className="search-bar" onSubmit={onSearch}>
        <Search size={20} />
        <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Найти событие по названию" disabled={!tokens} />
        <button className="button button-small" type="submit" disabled={!tokens}>Найти</button>
      </form>
      {!tokens && <div className="auth-banner"><Sparkles /><span><b>Это предпросмотр афиши.</b> Создай аккаунт, чтобы оформить первую заявку.</span><div className="auth-banner-actions"><button className="button button-ghost button-small" onClick={onRegister}>Регистрация</button><button className="button button-small" onClick={onLogin}>Войти</button></div></div>}
      <EventGrid events={tokens ? events : demoEvents} onSelect={onSelect} loading={loading} />
      {tokens && pageInfo.total_pages > 1 && (
        <div className="pagination">
          <button className="icon-button" disabled={pageInfo.number === 0} onClick={() => onPage(pageInfo.number - 1)}><ChevronLeft /></button>
          <span>{pageInfo.number + 1} / {pageInfo.total_pages}</span>
          <button className="icon-button" disabled={pageInfo.number + 1 >= pageInfo.total_pages} onClick={() => onPage(pageInfo.number + 1)}><ChevronRight /></button>
        </div>
      )}
    </section>
  )
}

function EventGrid({ events, onSelect, loading }) {
  if (loading && !events.length) return <div className="empty-state"><span className="spinner" /><p>Открываем архивы…</p></div>
  if (!events.length) return <div className="empty-state"><Feather size={32} /><h3>Пока ни одной истории</h3><p>Менеджер ещё не опубликовал события.</p></div>
  return (
    <div className="event-grid">
      {events.map((event, index) => (
        <article className="event-card" key={event.id} onClick={() => onSelect(event)} tabIndex="0" onKeyDown={(e) => e.key === 'Enter' && onSelect(event)}>
          <div className="event-image">
            <img src={getEventPoster(event, index)} alt="" />
            <span className="date-rune"><b>{new Date(event.event_date).getDate()}</b><small>{new Intl.DateTimeFormat('ru-RU', { month: 'short' }).format(new Date(event.event_date))}</small></span>
            <span className={`status status-${event.status?.toLowerCase()}`}>{statusLabels[event.status] || event.status}</span>
          </div>
          <div className="event-card-body">
            <div className="event-kicker"><MapPin size={14} /> {event.location}</div>
            <h3>{event.title}</h3>
            <p>{event.description}</p>
            <div className="event-footer">
              <span className="price">{money(event.price)}</span>
              <span><Users size={15} /> {event.available_seats ?? event.capacity} мест</span>
              <button className="arrow-button" aria-label="Подробнее"><ArrowRight size={18} /></button>
            </div>
          </div>
        </article>
      ))}
    </div>
  )
}

function EventModal({ event, image, views, tokens, profile, onClose, onLogin, onBooked, onNotify }) {
  const [quantity, setQuantity] = useState(1)
  const [draftLoaded, setDraftLoaded] = useState(false)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    if (!tokens || !profile || event.id.startsWith('demo-')) return
    api(`/api/orders/drafts/${profile.id}/${event.id}`)
      .then((draft) => {
        setQuantity(draft.quantity)
        setDraftLoaded(true)
      })
      .catch(() => {})
  }, [event.id, profile?.id])

  const saveDraft = async () => {
    if (!tokens) return onLogin()
    setBusy(true)
    try {
      await api(`/api/orders/drafts/${profile.id}/${event.id}`, {
        method: 'PUT',
        body: JSON.stringify({ quantity }),
      })
      setDraftLoaded(true)
      onNotify('Черновик сохранён на 15 минут', 'success')
    } catch (error) {
      onNotify(error.message, 'error')
    } finally {
      setBusy(false)
    }
  }

  const book = async () => {
    if (!tokens) return onLogin()
    setBusy(true)
    try {
      await api('/api/orders', {
        method: 'POST',
        body: JSON.stringify({ user_id: profile.id, event_id: event.id, quantity }),
      })
      onBooked()
    } catch (error) {
      onNotify(error.message, 'error')
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="event-modal" role="dialog" aria-modal="true">
        <button className="modal-close icon-button" onClick={onClose}><X /></button>
        <div className="modal-image"><img src={image} alt="" /><div className="modal-shade" /></div>
        <div className="modal-content">
          <div className="event-kicker"><span className="live-dot" /> {statusLabels[event.status] || event.status}</div>
          <h2>{event.title}</h2>
          <p className="modal-description">{event.description}</p>
          <div className="detail-grid">
            <div><CalendarDays /><span><small>Дата и время</small><b>{formatDate(event.event_date)}</b></span></div>
            <div><MapPin /><span><small>Место</small><b>{event.location}</b></span></div>
            <div><Users /><span><small>Свободно</small><b>{event.available_seats ?? event.capacity} мест</b></span></div>
            <div><Eye /><span><small>Просмотры</small><b>{views}</b></span></div>
          </div>
          <div className="booking-panel">
            <div className="quantity">
              <span><small>Количество мест</small><b>{money(Number(event.price) * quantity)}</b></span>
              <div><button onClick={() => setQuantity(Math.max(1, quantity - 1))}><Minus /></button><b>{quantity}</b><button onClick={() => setQuantity(Math.min(event.available_seats || event.capacity, quantity + 1))}><Plus /></button></div>
            </div>
            {!tokens ? (
              <button className="button button-wide" onClick={onLogin}><LogIn size={18} /> Войти и оформить</button>
            ) : (
              <div className="booking-actions">
                <button className="button button-ghost" onClick={saveDraft} disabled={busy}><Clock3 size={18} /> {draftLoaded ? 'Обновить черновик' : 'Сохранить на 15 минут'}</button>
                <button className="button" onClick={book} disabled={busy || (event.available_seats ?? 0) < quantity}><Ticket size={18} /> Оформить заявку</button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

function Orders({ orders, events, loading, onChange }) {
  return (
    <section className="page-section section narrow-section">
      <div className="page-intro"><p className="eyebrow"><span /> личный кабинет</p><h1>Мои заявки</h1><p>Созданная заявка уже резервирует места. Подтверди её или освободи места отменой.</p></div>
      {loading ? <div className="empty-state"><span className="spinner" /></div> : orders.length ? (
        <div className="orders-list">
          {orders.map((order, index) => {
            const event = events.find((item) => item.id === order.event_id)
            return (
              <article className="order-card" key={order.id}>
                <img src={getEventPoster(event || { id: order.event_id }, index)} alt="" />
                <div className="order-main">
                  <div><span className={`status status-${order.status.toLowerCase()}`}>{statusLabels[order.status]}</span><small>№ {order.id.slice(0, 8)}</small></div>
                  <h3>{event?.title || 'Библиотечное событие'}</h3>
                  <p><Ticket size={16} /> {order.quantity} {order.quantity === 1 ? 'место' : 'места'} · {money(order.total_price)}</p>
                </div>
                {order.status === 'CREATED' && <div className="order-actions"><button className="button button-small" onClick={() => onChange(order, 'CONFIRMED')}><Check size={17} /> Подтвердить</button><button className="button button-ghost button-small" onClick={() => onChange(order, 'CANCELLED')}>Отменить</button></div>}
              </article>
            )
          })}
        </div>
      ) : <div className="empty-state"><ShoppingBag /><h3>Здесь пока пусто</h3><p>Выбери событие в афише и оформи первую заявку.</p></div>}
    </section>
  )
}

function SettingsPage({ settings, profile, onSave }) {
  const [form, setForm] = useState(settings || { notifications_active: true, language: 'ru', preferred_category: 'Фэнтези' })
  useEffect(() => { if (settings) setForm(settings) }, [settings])
  return (
    <section className="page-section section narrow-section">
      <div className="page-intro"><p className="eyebrow"><span /> профиль читателя</p><h1>Настройки</h1><p>Эти данные кэшируются в Redis на 30 минут по сценарию cache-aside.</p></div>
      <div className="settings-layout">
        <aside className="profile-card"><div className="avatar"><CircleUserRound /></div><h3>{profile?.name}</h3><p>{profile?.email}</p><span className="status status-published">{profile?.role === 'MANAGER' ? 'Менеджер' : 'Читатель'}</span></aside>
        <form className="settings-form panel" onSubmit={(e) => { e.preventDefault(); onSave(form) }}>
          <label className="switch-row"><span><Bell /><b>Уведомления</b><small>Напоминания о заявках и событиях</small></span><input type="checkbox" checked={Boolean(form.notifications_active)} onChange={(e) => setForm({ ...form, notifications_active: e.target.checked })} /><i /></label>
          <label><span>Язык интерфейса</span><select value={form.language || 'ru'} onChange={(e) => setForm({ ...form, language: e.target.value })}><option value="ru">Русский</option><option value="en">English</option><option value="ja">日本語</option></select></label>
          <label><span>Любимая категория</span><input value={form.preferred_category || ''} onChange={(e) => setForm({ ...form, preferred_category: e.target.value })} placeholder="Например, городское фэнтези" /></label>
          <button className="button" type="submit"><Settings size={18} /> Сохранить настройки</button>
        </form>
      </div>
    </section>
  )
}

function PosterStudio({ events, profile, onSaved, onError }) {
  const [form, setForm] = useState(createEmptyPosterForm)
  const [editId, setEditId] = useState(null)
  const selectedPoster = postersByKey[form.image_key] || posterPool[0]
  const ownEvents = events.filter((event) => event.created_by === profile?.id)
  const edit = (event) => {
    setEditId(event.id)
    setForm({
      ...event,
      image_key: event.image_key || randomPosterKey(),
      event_date: new Date(event.event_date).toISOString().slice(0, 16),
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
  const submit = async (e) => {
    e.preventDefault()
    try {
      const capacity = Number(form.capacity)
      const price = Number(form.price)

      if (!Number.isInteger(capacity) || capacity < 1 || capacity > MAX_EVENT_CAPACITY) {
        throw new Error('Вместимость должна быть целым числом от 1 до 2 147 483 647')
      }
      if (!Number.isFinite(price) || price < 0 || price > MAX_EVENT_PRICE) {
        throw new Error('Цена должна быть от 0 до 9 999 999 999,99 ₽')
      }

      await api(editId ? `/api/events/${editId}` : '/api/events', {
        method: editId ? 'PUT' : 'POST',
        body: JSON.stringify({
          ...form,
          event_date: new Date(form.event_date).toISOString(),
          capacity,
          price,
          created_by: profile.id,
        }),
      })
      setForm(createEmptyPosterForm())
      setEditId(null)
      onSaved()
    } catch (error) { onError(error) }
  }
  return (
    <section className="page-section section">
      <div className="page-intro"><p className="eyebrow"><span /> мастерская читателя</p><h1>{editId ? 'Изменить афишу' : 'Создать афишу'}</h1><p>Заполни карточку события. Обложка выбирается случайно из трёх оригинальных аниме-иллюстраций.</p></div>
      <form className="manager-form panel" onSubmit={submit}>
        <div className="poster-designer wide">
          <div className="poster-preview">
            <img src={selectedPoster.src} alt={`Выбранная обложка: ${selectedPoster.label}`} />
            <span><Sparkles size={15} /> {selectedPoster.label}</span>
          </div>
          <div className="poster-controls">
            <div>
              <p className="eyebrow"><span /> обложка события</p>
              <h2>Случайный образ</h2>
              <p>При создании афиши система выбирает один из трёх вариантов. Можно перетасовать или выбрать понравившийся вручную.</p>
            </div>
            <button className="button button-ghost button-small" type="button" onClick={() => setForm({ ...form, image_key: randomPosterKey(form.image_key) })}><Shuffle size={17} /> Перетасовать</button>
            <div className="poster-options" aria-label="Выбор обложки">
              {posterPool.map((poster) => (
                <button className={form.image_key === poster.key ? 'selected' : ''} type="button" key={poster.key} onClick={() => setForm({ ...form, image_key: poster.key })} aria-label={poster.label} aria-pressed={form.image_key === poster.key}>
                  <img src={poster.src} alt="" />
                </button>
              ))}
            </div>
          </div>
        </div>
        <label className="wide"><span>Название</span><input required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} placeholder="Встреча с автором" /></label>
        <label className="wide"><span>Описание</span><textarea required rows="4" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="О чём эта встреча?" /></label>
        <label><span>Дата и время</span><input required type="datetime-local" value={form.event_date} onChange={(e) => setForm({ ...form, event_date: e.target.value })} /></label>
        <label><span>Место</span><input required value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} placeholder="Главный зал" /></label>
        <label><span>Вместимость</span><input required min="1" max={MAX_EVENT_CAPACITY} step="1" type="number" value={form.capacity} onChange={(e) => setForm({ ...form, capacity: e.target.value })} /></label>
        <label><span>Цена, ₽</span><input required min="0" max={MAX_EVENT_PRICE} step="0.01" type="number" value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} /></label>
        <label><span>Статус</span><select value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}><option value="DRAFTED">Черновик</option><option value="PUBLISHED">Опубликовано</option><option value="CANCELLED">Отменено</option><option value="FINISHED">Завершено</option></select></label>
        <div className="form-actions"><button className="button" type="submit" disabled={!profile}>{editId ? 'Сохранить изменения' : 'Создать афишу'} <ArrowRight size={18} /></button>{editId && <button type="button" className="button button-ghost" onClick={() => { setEditId(null); setForm(createEmptyPosterForm()) }}>Отмена</button>}</div>
      </form>
      <div className="manager-list">
        <h2>Мои опубликованные афиши</h2>
        {ownEvents.length ? ownEvents.map((event, index) => <button key={event.id} onClick={() => edit(event)}><img src={getEventPoster(event, index)} alt="" /><span><b>{event.title}</b><small>{formatDate(event.event_date)} · {event.location}</small></span><ArrowRight /></button>) : <div className="empty-state"><Feather /><h3>Первая афиша ждёт тебя</h3><p>Создай событие — оно появится здесь после публикации.</p></div>}
      </div>
    </section>
  )
}

function SectionHeading({ eyebrow, title, action, onAction }) {
  return <div className="section-heading"><div><p className="eyebrow"><span /> {eyebrow}</p><h2>{title}</h2></div>{action && <button onClick={onAction}>{action} <ArrowRight size={17} /></button>}</div>
}

function Footer({ navigate }) {
  return (
    <footer><div className="footer-mark"><MoonStar /><span><b>КАГЭ</b><small>Место, где истории находят читателя.</small></span></div><div className="footer-links"><button onClick={() => navigate('catalog')}>Афиша</button><button onClick={() => navigate('home')}>О библиотеке</button><a href="http://localhost:8181" target="_blank" rel="noreferrer">Keycloak</a></div><small>Лабораторная работа №1 · PostgreSQL + Redis</small></footer>
  )
}

function Toast({ text, type, onClose }) {
  return <div className={`toast toast-${type}`}><span>{type === 'success' ? <Check /> : type === 'error' ? <X /> : <Sparkles />}</span><p>{text}</p><button onClick={onClose}><X size={16} /></button></div>
}

export default App
