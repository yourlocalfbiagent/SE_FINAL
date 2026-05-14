// ============================================================
// ERP System — API Service Layer
// Admin module: http://localhost:8081
// ERP main module: http://localhost:8080
// ============================================================

const ERP_BASE   = 'http://localhost:8080';
const ADMIN_BASE = 'http://localhost:8081';

// ---- Auth storage ----
function getToken()     { return localStorage.getItem('erp_token'); }
function getCompanyId() { return localStorage.getItem('erp_company_id'); }
function getUserId()    { return localStorage.getItem('erp_user_id'); }
function getUserEmail() { return localStorage.getItem('erp_email'); }
function getUserRole()  { return localStorage.getItem('erp_role'); }

function saveAuth({ token, userId, companyId, email, role }) {
  localStorage.setItem('erp_token',      token);
  localStorage.setItem('erp_user_id',    String(userId));
  localStorage.setItem('erp_company_id', String(companyId));
  localStorage.setItem('erp_email',      email);
  localStorage.setItem('erp_role',       role || 'EMPLOYEE');
}

function clearAuth() {
  ['erp_token', 'erp_user_id', 'erp_company_id', 'erp_email', 'erp_role']
    .forEach(k => localStorage.removeItem(k));
}

// ---- HTTP helpers ----
async function _call(base, path, opts = {}) {
  const token = getToken();
  const headers = { 'Content-Type': 'application/json', ...(opts.headers || {}) };
  if (token) headers['Authorization'] = 'Bearer ' + token;
  const res = await fetch(base + path, { ...opts, headers });
  if (res.status === 401) {
    clearAuth();
    window.location.href = 'Registration.html';
    return null;
  }
  if (res.status === 204) return null;
  if (!res.ok) {
    let msg = 'HTTP ' + res.status;
    try { const j = await res.json(); msg = j.detail || j.message || j.error || msg; } catch {}
    throw new Error(msg);
  }
  return res.json();
}

const erpApi   = (path, opts) => _call(ERP_BASE,   path, opts);
const adminApi = (path, opts) => _call(ADMIN_BASE, path, opts);

// ---- React data-loading hook ----
// Returns { data, loading, error, reload }
function useLoad(fn) {
  const [data,    setData]    = React.useState(null);
  const [loading, setLoading] = React.useState(true);
  const [error,   setError]   = React.useState(null);

  const reload = React.useCallback(async () => {
    setLoading(true);
    setError(null);
    try     { setData(await fn()); }
    catch(e){ setError(e.message || 'Failed to load'); }
    finally { setLoading(false); }
  }, []);

  React.useEffect(() => { reload(); }, []);

  return { data, loading, error, reload };
}

// ---- Shared loading/error UI ----
function PageLoad() {
  return (
    <div className="empty-state" style={{ color: '#94a3b8' }}>
      Loading…
    </div>
  );
}

function PageError({ message, onRetry }) {
  return (
    <div className="empty-state" style={{ color: '#ef4444', gap: 12 }}>
      <div>⚠ {message}</div>
      {onRetry && (
        <button className="btn btn--ghost btn--sm" style={{ marginTop: 8 }} onClick={onRetry}>
          Retry
        </button>
      )}
    </div>
  );
}

Object.assign(window, {
  ERP_BASE, ADMIN_BASE,
  getToken, getCompanyId, getUserId, getUserEmail, getUserRole,
  saveAuth, clearAuth, erpApi, adminApi,
  useLoad, PageLoad, PageError,
});
