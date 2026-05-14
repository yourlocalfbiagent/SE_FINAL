// ============================================================
// ERP System — Shared Components, Icons & Utilities
// ============================================================
const { useState, useEffect, useMemo, useCallback } = React;

// ===== UTILITIES =====
const fmt$ = (n) => '$' + Number(n).toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const fmtDate = (d) => { try { return new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }); } catch(e) { return d; } };
const fmtDateTime = (d) => { try { const dt = new Date(d); return dt.toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) + ' ' + dt.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' }); } catch(e) { return d; } };
const initials = (n) => n ? n.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2) : '?';
const avatarBg = (n) => {
  const c = ['#f59e0b','#3b82f6','#10b981','#ef4444','#8b5cf6','#ec4899','#14b8a6','#f97316'];
  let h = 0; for (let i = 0; i < (n||'').length; i++) h = n.charCodeAt(i) + ((h << 5) - h);
  return c[Math.abs(h) % c.length];
};

// ===== BADGE COLOR MAP =====
const BC = {
  PENDING: { bg: '#fef08a', text: '#ca8a04' },
  APPROVED: { bg: '#bbf7d0', text: '#15803d' },
  REJECTED: { bg: '#fee2e2', text: '#991b1b' },
  CONFIRMED: { bg: '#bbf7d0', text: '#15803d' },
  DRAFT: { bg: '#f1f5f9', text: '#475569' },
  PAID: { bg: '#bbf7d0', text: '#15803d' },
  PARTIAL: { bg: '#fed7aa', text: '#ea580c' },
  UNPAID: { bg: '#fee2e2', text: '#991b1b' },
  COMPLETED: { bg: '#dbeafe', text: '#1d4ed8' },
  DELIVERED: { bg: '#d1fae5', text: '#059669' },
  ACTIVE: { bg: '#bbf7d0', text: '#15803d' },
  INACTIVE: { bg: '#fee2e2', text: '#991b1b' },
  LOW: { bg: '#fee2e2', text: '#ef4444' },
  OK: { bg: '#bbf7d0', text: '#15803d' },
  IN: { bg: '#dbeafe', text: '#1d4ed8' },
  OUT: { bg: '#fed7aa', text: '#ea580c' },
  OPEN: { bg: '#fef08a', text: '#ca8a04' },
  CLOSED: { bg: '#f1f5f9', text: '#475569' },
  CREATE: { bg: '#dbeafe', text: '#1d4ed8' },
  UPDATE: { bg: '#fef08a', text: '#ca8a04' },
  DELETE: { bg: '#fee2e2', text: '#991b1b' },
  LOGIN: { bg: '#e0e7ff', text: '#4338ca' },
  RECEIVED: { bg: '#d1fae5', text: '#059669' },
  OVERDUE: { bg: '#fee2e2', text: '#991b1b' },
  COUNTED: { bg: '#dbeafe', text: '#1d4ed8' },
  ADMIN: { bg: '#fef3c7', text: '#92400e' },
  MANAGER: { bg: '#dbeafe', text: '#1d4ed8' },
  EMPLOYEE: { bg: '#f1f5f9', text: '#475569' },
  AUDITOR: { bg: '#e0e7ff', text: '#4338ca' },
};

// ===== ERP LOGO =====
function ERPLogo({ size = 28 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 40 40" fill="none">
      <rect x="2" y="2" width="16" height="16" rx="4" fill="#f59e0b"/>
      <rect x="22" y="2" width="16" height="16" rx="4" fill="#f59e0b" opacity="0.6"/>
      <rect x="2" y="22" width="16" height="16" rx="4" fill="#f59e0b" opacity="0.6"/>
      <rect x="22" y="22" width="16" height="16" rx="4" fill="#f59e0b" opacity="0.3"/>
    </svg>
  );
}

// ===== NAV ICON =====
function NavIcon({ type }) {
  return (
    <svg width="20" height="20" viewBox="0 0 20 20" fill="none" stroke="currentColor"
      strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
      {type === 'dashboard' && <><rect x="2" y="2" width="7" height="7" rx="1.5"/><rect x="11" y="2" width="7" height="7" rx="1.5"/><rect x="2" y="11" width="7" height="7" rx="1.5"/><rect x="11" y="11" width="7" height="7" rx="1.5"/></>}
      {type === 'orders' && <><rect x="3" y="1" width="14" height="17" rx="2"/><line x1="7" y1="6" x2="13" y2="6"/><line x1="7" y1="9.5" x2="13" y2="9.5"/><line x1="7" y1="13" x2="10" y2="13"/></>}
      {type === 'invoices' && <><rect x="5" y="1" width="12" height="14" rx="2"/><path d="M5 4H4a2 2 0 00-2 2v11a2 2 0 002 2h9a2 2 0 002-2v-1"/></>}
      {type === 'payments' && <><rect x="1" y="4" width="18" height="12" rx="2"/><line x1="1" y1="9" x2="19" y2="9"/></>}
      {type === 'returns' && <><polyline points="5,8 1,5 5,2"/><path d="M1 5h13a4 4 0 010 8h-4"/></>}
      {type === 'purchase' && <><rect x="2" y="5" width="16" height="13" rx="2"/><path d="M6 5V3a2 2 0 012-2h4a2 2 0 012 2v2"/></>}
      {type === 'receipts' && <><rect x="3" y="2" width="14" height="16" rx="2"/><polyline points="7,10 9,12 13,8"/></>}
      {type === 'bills' && <><rect x="3" y="1" width="14" height="17" rx="2"/><line x1="8" y1="6" x2="12" y2="6"/><line x1="8" y1="10" x2="12" y2="10"/><line x1="8" y1="14" x2="10" y2="14"/></>}
      {type === 'stock' && <><line x1="3" y1="5" x2="17" y2="5"/><line x1="3" y1="10" x2="17" y2="10"/><line x1="3" y1="15" x2="17" y2="15"/><circle cx="6" cy="5" r="1" fill="currentColor" stroke="none"/><circle cx="6" cy="10" r="1" fill="currentColor" stroke="none"/><circle cx="6" cy="15" r="1" fill="currentColor" stroke="none"/></>}
      {type === 'movements' && <><line x1="7" y1="2" x2="7" y2="18"/><polyline points="4,5 7,2 10,5"/><line x1="13" y1="2" x2="13" y2="18"/><polyline points="10,15 13,18 16,15"/></>}
      {type === 'counts' && <><rect x="3" y="1" width="14" height="17" rx="2"/><line x1="7" y1="6" x2="7" y2="6.01"/><line x1="10" y1="6" x2="13" y2="6"/><line x1="7" y1="10" x2="7" y2="10.01"/><line x1="10" y1="10" x2="13" y2="10"/><line x1="7" y1="14" x2="7" y2="14.01"/><line x1="10" y1="14" x2="13" y2="14"/></>}
      {type === 'alerts' && <><path d="M10 2L1 17h18L10 2z"/><line x1="10" y1="8" x2="10" y2="12"/><circle cx="10" cy="14.5" r="0.5" fill="currentColor" stroke="none"/></>}
      {type === 'reports' && <><rect x="2" y="12" width="4" height="6" rx="1"/><rect x="8" y="7" width="4" height="11" rx="1"/><rect x="14" y="2" width="4" height="16" rx="1"/></>}
      {type === 'users' && <><circle cx="10" cy="6" r="3.5"/><path d="M2 18c0-4 3.6-7 8-7s8 3 8 7"/></>}
      {type === 'roles' && <><path d="M10 1l8 4v5c0 5-3.2 8.4-8 10-4.8-1.6-8-5-8-10V5l8-4z"/></>}
      {type === 'config' && <><circle cx="10" cy="10" r="3"/><path d="M10 1v2m0 14v2M1 10h2m14 0h2M3.9 3.9l1.4 1.4m9.4 9.4l1.4 1.4M3.9 16.1l1.4-1.4m9.4-9.4l1.4-1.4"/></>}
      {type === 'audit' && <><path d="M1 10s4-7 9-7 9 7 9 7-4 7-9 7-9-7-9-7z"/><circle cx="10" cy="10" r="3"/></>}
      {type === 'search' && <><circle cx="8.5" cy="8.5" r="5.5"/><line x1="13" y1="13" x2="18" y2="18"/></>}
      {type === 'bell' && <><path d="M10 17c1.1 0 2-.9 2-2H8c0 1.1.9 2 2 2z"/><path d="M15 11V7a5 5 0 00-10 0v4l-2 3h14l-2-3z"/></>}
      {type === 'logout' && <><path d="M6 3H4a2 2 0 00-2 2v10a2 2 0 002 2h2"/><line x1="9" y1="10" x2="19" y2="10"/><polyline points="15,6 19,10 15,14"/></>}
      {type === 'export' && <><line x1="10" y1="2" x2="10" y2="13"/><polyline points="6,9 10,13 14,9"/><line x1="3" y1="17" x2="17" y2="17"/></>}
    </svg>
  );
}

// ===== PAGE HEADER =====
function PageHeader({ title, subtitle, children }) {
  return (
    <div className="page-header">
      <div className="page-header__left">
        <h1 className="page-header__title">{title}</h1>
        {subtitle && <p className="page-header__sub">{subtitle}</p>}
      </div>
      {children && <div className="page-header__actions">{children}</div>}
    </div>
  );
}

// ===== BADGE =====
function Badge({ status }) {
  const key = status ? status.toUpperCase().replace(/\s+/g, '_') : '';
  const c = BC[key] || { bg: '#f1f5f9', text: '#475569' };
  return <span className="badge" style={{ background: c.bg, color: c.text }}>{status}</span>;
}

// ===== DATA TABLE =====
function DataTable({ columns, data, onRowClick, selectedId, showCheck, pageSize = 10 }) {
  const [sortKey, setSortKey] = useState(null);
  const [sortDir, setSortDir] = useState('asc');
  const [page, setPage] = useState(0);

  const sorted = useMemo(() => {
    if (!sortKey) return data;
    return [...data].sort((a, b) => {
      const va = a[sortKey], vb = b[sortKey];
      if (va == null) return 1;
      if (vb == null) return -1;
      const cmp = typeof va === 'number' ? va - vb : String(va).localeCompare(String(vb));
      return sortDir === 'asc' ? cmp : -cmp;
    });
  }, [data, sortKey, sortDir]);

  const totalPages = Math.ceil(data.length / pageSize);
  const paged = sorted.slice(page * pageSize, (page + 1) * pageSize);

  const toggleSort = (key) => {
    if (sortKey === key) setSortDir(d => d === 'asc' ? 'desc' : 'asc');
    else { setSortKey(key); setSortDir('asc'); }
  };

  const renderCell = (row, col) => {
    const v = row[col.key];
    switch (col.type) {
      case 'badge': return <Badge status={v}/>;
      case 'currency': return <span className="mono">{fmt$(v)}</span>;
      case 'date': return fmtDate(v);
      case 'datetime': return fmtDateTime(v);
      case 'avatar': return (
        <div className="avatar-cell">
          <div className="avatar-xs" style={{ background: avatarBg(v) }}>{initials(v)}</div>
          <span>{v}</span>
        </div>
      );
      default: return v;
    }
  };

  if (!data.length) return <div className="empty-state">No records found</div>;

  return (
    <div className="dt-wrap">
      <div className="dt-scroll">
        <table className="dt">
          <thead>
            <tr>
              {showCheck && <th className="dt-chk"><input type="checkbox" disabled/></th>}
              {columns.map(col => (
                <th key={col.key} style={col.width ? { width: col.width } : {}}
                  className="dt-th" onClick={() => toggleSort(col.key)}>
                  <span>{col.label}</span>
                  {sortKey === col.key && <span className="dt-sort-arrow">{sortDir === 'asc' ? '↑' : '↓'}</span>}
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {paged.map(row => (
              <tr key={row.id} className={selectedId === row.id ? 'dt-row--sel' : ''}
                onClick={() => onRowClick && onRowClick(row)}>
                {showCheck && <td className="dt-chk"><input type="checkbox" checked={selectedId === row.id} readOnly/></td>}
                {columns.map(col => (
                  <td key={col.key} className={col.type === 'currency' ? 'dt-num' : ''}>
                    {renderCell(row, col)}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <div className="dt-pag">
          <span className="dt-pag__info">
            {page * pageSize + 1}–{Math.min((page + 1) * pageSize, data.length)} of {data.length}
          </span>
          <div className="dt-pag__btns">
            <button className="dt-pag__btn" disabled={page === 0} onClick={() => setPage(p => p - 1)}>← Prev</button>
            <button className="dt-pag__btn" disabled={page >= totalPages - 1} onClick={() => setPage(p => p + 1)}>Next →</button>
          </div>
        </div>
      )}
    </div>
  );
}

// ===== DETAIL PANEL =====
function DetailPanel({ open, onClose, title, children, footer }) {
  if (!open) return null;
  return (
    <div className="dp">
      <div className="dp__head">
        <h3 className="dp__title">{title}</h3>
        <button className="dp__close" onClick={onClose}>✕</button>
      </div>
      <div className="dp__body">{children}</div>
      {footer && <div className="dp__foot">{footer}</div>}
    </div>
  );
}

function DPRow({ label, value, bold }) {
  return (
    <div className="dp-row">
      <span className="dp-row__label">{label}</span>
      <span className={'dp-row__value' + (bold ? ' dp-row__value--bold' : '')}>{value}</span>
    </div>
  );
}

// ===== MODAL =====
function Modal({ open, onClose, title, width, children, footer }) {
  if (!open) return null;
  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-box" style={{ maxWidth: width || 600 }} onClick={e => e.stopPropagation()}>
        <div className="modal-head">
          <h3>{title}</h3>
          <button className="modal-x" onClick={onClose}>✕</button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-foot">{footer}</div>}
      </div>
    </div>
  );
}

// ===== KPI CARD =====
function KPICard({ label, value, trend, trendUp }) {
  return (
    <div className="kpi">
      <p className="kpi__label">{label}</p>
      <h3 className="kpi__value">{value}</h3>
      {trend && (
        <span className={'kpi__trend' + (trendUp ? ' kpi__trend--up' : ' kpi__trend--down')}>
          {trendUp ? '↑' : '↓'} {trend}
        </span>
      )}
    </div>
  );
}

// ===== FILTER BAR =====
function FilterBar({ filters, values, onChange, searchVal, onSearchChange }) {
  return (
    <div className="filter-bar">
      {filters && filters.map(f => (
        <select key={f.key} className="filter-sel" value={values[f.key] || ''}
          onChange={e => onChange({ ...values, [f.key]: e.target.value })}>
          <option value="">{f.label}</option>
          {f.options.map(o => <option key={o} value={o}>{o}</option>)}
        </select>
      ))}
      {onSearchChange && (
        <div className="filter-search">
          <NavIcon type="search"/>
          <input className="filter-search__input" placeholder="Search..." value={searchVal || ''}
            onChange={e => onSearchChange(e.target.value)}/>
        </div>
      )}
    </div>
  );
}

// ===== BAR CHART =====
function BarChart({ data, height = 200 }) {
  const max = Math.max(...data.map(d => d.value), 1);
  return (
    <div className="barchart" style={{ height }}>
      <div className="barchart__bars">
        {data.map((d, i) => (
          <div key={i} className="barchart__col">
            <div className="barchart__bar" style={{ height: `${(d.value / max) * 100}%` }}
              title={`${d.label}: ${typeof d.value === 'number' && d.value > 100 ? fmt$(d.value) : d.value}`}/>
            <span className="barchart__lbl">{d.label}</span>
          </div>
        ))}
      </div>
    </div>
  );
}

// ===== FORM COMPONENTS =====
function FormInput({ label, value, onChange, type, placeholder, required, error, disabled }) {
  return (
    <div className="ff">
      <label className="ff__label">{label}{required && <span className="ff__req">*</span>}</label>
      <input className={'ff__input' + (error ? ' ff__input--err' : '')} type={type || 'text'}
        value={value} onChange={e => onChange(e.target.value)} placeholder={placeholder}
        disabled={disabled}/>
      {error && <span className="ff__err">{error}</span>}
    </div>
  );
}

function FormSelect({ label, value, onChange, options, placeholder, required, error, disabled }) {
  return (
    <div className="ff">
      <label className="ff__label">{label}{required && <span className="ff__req">*</span>}</label>
      <select className={'ff__input' + (error ? ' ff__input--err' : '')} value={value}
        onChange={e => onChange(e.target.value)} disabled={disabled}>
        <option value="">{placeholder || 'Select…'}</option>
        {options.map(o => {
          const val = typeof o === 'string' ? o : o.value;
          const lbl = typeof o === 'string' ? o : o.label;
          return <option key={val} value={val}>{lbl}</option>;
        })}
      </select>
      {error && <span className="ff__err">{error}</span>}
    </div>
  );
}

function FormRow({ children }) {
  return <div className="ff-row">{children}</div>;
}

// ===== EMPTY STATE =====
function EmptyState({ message }) {
  return <div className="empty-state">{message || 'No records found'}</div>;
}

// ===== EXPORTS =====
Object.assign(window, {
  fmt$, fmtDate, fmtDateTime, initials, avatarBg, BC,
  ERPLogo, NavIcon, PageHeader, Badge, DataTable, DetailPanel, DPRow,
  Modal, KPICard, FilterBar, BarChart, FormInput, FormSelect, FormRow, EmptyState,
});
