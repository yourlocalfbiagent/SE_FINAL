// ============================================================
// ERP System — Layout Shell (Sidebar + Topbar)
// ============================================================

const NAV_SECTIONS = [
  { label: 'MAIN', items: [
    { label: 'Dashboard', icon: 'dashboard', path: '/' },
  ]},
  { label: 'MASTER DATA', items: [
    { label: 'Products',   icon: 'products',  path: '/master/products' },
    { label: 'Partners',   icon: 'users',     path: '/master/partners' },
    { label: 'Warehouses', icon: 'stock',     path: '/master/warehouses' },
    { label: 'Categories', icon: 'reports',   path: '/master/categories' },
  ]},
  { label: 'SALES', items: [
    { label: 'Orders', icon: 'orders', path: '/sales/orders' },
    { label: 'Invoices', icon: 'invoices', path: '/sales/invoices' },
    { label: 'Payments', icon: 'payments', path: '/sales/payments' },
    { label: 'Returns', icon: 'returns', path: '/sales/returns' },
  ]},
  { label: 'PURCHASING', items: [
    { label: 'Purchase Orders', icon: 'purchase', path: '/purchase/orders' },
    { label: 'Goods Receipts', icon: 'receipts', path: '/purchase/receipts' },
    { label: 'Supplier Bills', icon: 'bills', path: '/purchase/bills' },
  ]},
  { label: 'INVENTORY', items: [
    { label: 'Stock Levels', icon: 'stock', path: '/inventory/stock' },
    { label: 'Movements', icon: 'movements', path: '/inventory/movements' },
    { label: 'Counts', icon: 'counts', path: '/inventory/counts' },
    { label: 'Low Stock Alerts', icon: 'alerts', path: '/inventory/alerts' },
  ]},
  { label: 'REPORTS', items: [
    { label: 'Sales Summary', icon: 'reports', path: '/reports/sales' },
  ]},
  { label: 'ADMIN', items: [
    { label: 'Users', icon: 'users', path: '/admin/users' },
    { label: 'Roles', icon: 'roles', path: '/admin/roles' },
    { label: 'Permissions', icon: 'config', path: '/admin/permissions' },
    { label: 'Configuration', icon: 'config', path: '/admin/config' },
    { label: 'Audit Log', icon: 'audit', path: '/audit/logs' },
  ]},
];

function Sidebar({ route, onNavigate }) {
  const email = getUserEmail() || 'user@erp.com';
  const role  = getUserRole()  || 'EMPLOYEE';
  const name  = email.split('@')[0].replace(/[._]/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
  const ini   = name.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase() || 'U';

  const allowedModules = getUserModules();

  const handleLogout = () => {
    clearAuth();
    onNavigate('/login');
  };

  return (
    <aside className="sidebar">
      <div className="sidebar__brand">
        <ERPLogo size={30}/>
        <span className="sidebar__brand-text">ERP System</span>
      </div>
      <nav className="sidebar__nav">
        {NAV_SECTIONS.filter(sec => allowedModules.has(sec.label)).map(sec => (
          <div key={sec.label} className="sidebar__section">
            <div className="sidebar__sec-label">{sec.label}</div>
            {sec.items.map(item => (
              <button key={item.path}
                className={'sidebar__item' + (route === item.path ? ' sidebar__item--active' : '')}
                onClick={() => onNavigate(item.path)}>
                <NavIcon type={item.icon}/>
                <span>{item.label}</span>
              </button>
            ))}
          </div>
        ))}
      </nav>
      <div className="sidebar__footer">
        <div className="sidebar__user">
          <div className="avatar-xs" style={{ background: avatarBg(name), color: '#fff' }}>{ini}</div>
          <div className="sidebar__user-info">
            <span className="sidebar__user-name">{name}</span>
            <span className="sidebar__user-role">{role}</span>
          </div>
        </div>
        <button className="sidebar__logout" onClick={handleLogout} title="Logout">
          <NavIcon type="logout"/>
        </button>
      </div>
    </aside>
  );
}

function Topbar({ route }) {
  const info = useMemo(() => {
    for (const sec of NAV_SECTIONS) {
      for (const it of sec.items) {
        if (it.path === route) return { title: it.label, section: sec.label };
      }
    }
    return { title: 'Dashboard', section: 'MAIN' };
  }, [route]);

  const [searchOpen, setSearchOpen] = useState(false);

  return (
    <header className="topbar">
      <div className="topbar__left">
        <span className="topbar__crumb">{info.section} / {info.title}</span>
      </div>
      <div className="topbar__spacer"></div>
      <div className={'topbar__search' + (searchOpen ? ' topbar__search--open' : '')}>
        <button className="topbar__search-btn" onClick={() => setSearchOpen(!searchOpen)}>
          <NavIcon type="search"/>
        </button>
        {searchOpen && <input className="topbar__search-input" placeholder="Search anything…" autoFocus/>}
      </div>
      <button className="topbar__icon-btn">
        <NavIcon type="bell"/>
        <span className="topbar__notif-dot"></span>
      </button>
      <div className="topbar__user">
        {(() => {
          const e = getUserEmail() || '';
          const n = e.split('@')[0].replace(/[._]/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
          const ini = n.split(' ').map(w => w[0]).slice(0, 2).join('').toUpperCase() || 'U';
          return <div className="avatar-xs" style={{ background: avatarBg(n), color: '#fff' }}>{ini}</div>;
        })()}
      </div>
    </header>
  );
}

function AppLayout({ route, onNavigate, children }) {
  return (
    <div className="app-shell">
      <Sidebar route={route} onNavigate={onNavigate}/>
      <div className="app-main">
        <Topbar route={route}/>
        <main className="app-content">{children}</main>
      </div>
    </div>
  );
}

Object.assign(window, { AppLayout, NAV_SECTIONS, Sidebar, Topbar });
