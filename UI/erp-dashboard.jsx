// ============================================================
// ERP System — Dashboard Page (role-aware)
// ============================================================

function DashboardPage() {
  const cid     = getCompanyId();
  const modules = getUserModules();
  const role    = getUserRole() || 'EMPLOYEE';

  const hasSales     = modules.has('SALES');
  const hasPurchasing = modules.has('PURCHASING');
  const hasInventory = modules.has('INVENTORY');
  const hasAdmin     = modules.has('ADMIN') || role === 'ADMIN';
  const hasMaster    = modules.has('MASTER DATA');

  // ---- data loads (conditional) ----
  const { data: orders,   loading: orL, error: orE, reload: orR } =
    useLoad(() => hasSales ? erpApi('/api/sales-orders') : Promise.resolve([]));

  const { data: invList,  loading: ivL, error: ivE, reload: ivR } =
    useLoad(() => hasSales ? erpApi('/api/sales-invoices') : Promise.resolve([]));

  const { data: pos,      loading: poL, error: poE, reload: poR } =
    useLoad(() => hasPurchasing ? erpApi('/api/purchase-orders') : Promise.resolve([]));

  const { data: bills,    loading: blL, error: blE, reload: blR } =
    useLoad(() => hasPurchasing ? erpApi('/api/supplier-bills') : Promise.resolve([]));

  const { data: stock,    loading: stL, error: stE, reload: stR } =
    useLoad(() => hasInventory ? erpApi('/api/inventory-locations') : Promise.resolve([]));

  const { data: admStats, loading: adL, error: adE, reload: adR } =
    useLoad(() => hasAdmin ? adminApi('/api/companies/' + cid + '/reports/admin-stats') : Promise.resolve(null));

  const { data: audits,   loading: auL, error: auE, reload: auR } =
    useLoad(() => hasAdmin ? adminApi('/api/audit-log?limit=8') : Promise.resolve([]));

  const { data: partners, loading: paL, error: paE, reload: paR } =
    useLoad(() => hasMaster ? erpApi('/api/business-partners') : Promise.resolve([]));

  const { data: products, loading: prL, error: prE, reload: prR } =
    useLoad(() => hasMaster ? erpApi('/api/products') : Promise.resolve([]));

  const orderArr   = orders   || [];
  const invArr     = invList  || [];
  const poArr      = pos      || [];
  const billArr    = bills    || [];
  const stockArr   = stock    || [];
  const auditArr   = audits   || [];
  const statsObj   = admStats || {};
  const partnerArr = partners || [];
  const productArr = products || [];

  // ---- KPIs ----
  const totalRevenue   = orderArr.filter(o => ['CONFIRMED','COMPLETED'].includes((o.status||'').toUpperCase()))
                                 .reduce((s, o) => s + Number(o.totalAmount || 0), 0);
  const openOrders     = orderArr.filter(o => !['COMPLETED','CANCELLED'].includes((o.status||'').toUpperCase())).length;
  const pendingInv     = invArr.filter(i => (i.status||'').toUpperCase() === 'UNPAID').length;
  const pendingPOs     = poArr.filter(p => (p.status||'').toLowerCase() === 'pending').length;
  const unpaidBills    = billArr.filter(b => (b.status||'').toLowerCase() === 'draft' || (b.status||'').toLowerCase() === 'unpaid').length;
  const lowStock       = stockArr.filter(s => Number(s.quantityAvailable || 0) <= 5).length;
  const activeUsers    = statsObj.activeUsers ?? '—';

  // monthly revenue chart
  const monthlyData = React.useMemo(() => {
    const map = {};
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    orderArr.forEach(o => {
      const d = o.orderDate || o.createdAt;
      if (!d) return;
      const mo = new Date(d).getMonth();
      const key = months[mo];
      map[key] = (map[key] || 0) + Number(o.totalAmount || 0);
    });
    return Object.entries(map).map(([label, value]) => ({ label, value }));
  }, [orderArr]);

  const pendingInvItems = invArr.filter(i => (i.status||'').toUpperCase() === 'UNPAID').slice(0, 5);

  return (
    <div className="dashboard">
      <PageHeader
        title="Dashboard"
        subtitle={'Welcome back, ' + (getUserEmail() || 'User').split('@')[0] + '  •  ' + role}
      />

      {/* ── KPI row — only show cards relevant to this user's modules ── */}
      <div className="kpi-grid">
        {hasSales && (
          <>
            <KPICard label="Total Revenue"    value={fmt$(totalRevenue)}   trend="Confirmed + Completed orders" trendUp/>
            <KPICard label="Open Orders"      value={String(openOrders)}   trend="Not yet completed" trendUp/>
            <KPICard label="Unpaid Invoices"  value={String(pendingInv)}   trend="Awaiting payment"  trendUp={pendingInv === 0}/>
          </>
        )}
        {hasPurchasing && (
          <>
            <KPICard label="Pending POs"      value={String(pendingPOs)}   trend="Purchase orders pending" trendUp={pendingPOs === 0}/>
            <KPICard label="Unpaid Bills"     value={String(unpaidBills)}  trend="Supplier bills due"      trendUp={unpaidBills === 0}/>
          </>
        )}
        {hasInventory && (
          <KPICard label="Low Stock Items"    value={String(lowStock)}     trend="Qty available ≤ 5"       trendUp={lowStock === 0}/>
        )}
        {hasMaster && (
          <>
            <KPICard label="Products"         value={String(productArr.length)}  trend="In catalog" trendUp/>
            <KPICard label="Partners"         value={String(partnerArr.length)}  trend="Customers & suppliers" trendUp/>
          </>
        )}
        {hasAdmin && (
          <KPICard label="Active Users"       value={String(activeUsers)}  trend={statsObj.totalUsers ? statsObj.totalUsers + ' total' : ''} trendUp/>
        )}
      </div>

      <div className="dash-grid">
        {/* Revenue chart — Sales only */}
        {hasSales && (
          <div className="dash-card dash-card--wide">
            <div className="dash-card__head">
              <h3>Revenue Overview</h3>
              <span className="dash-card__sub">By order month</span>
            </div>
            {orL ? <PageLoad/> : orE ? <PageError message={orE} onRetry={orR}/> :
              monthlyData.length > 0
                ? <BarChart data={monthlyData} height={220}/>
                : <div className="empty-state">No order data yet</div>}
          </div>
        )}

        {/* Purchasing summary */}
        {hasPurchasing && (
          <div className="dash-card">
            <div className="dash-card__head"><h3>Purchase Orders</h3></div>
            {poL ? <PageLoad/> : poE ? <PageError message={poE} onRetry={poR}/> : (
              <div className="activity-list">
                {poArr.length === 0
                  ? <div className="empty-state">No purchase orders</div>
                  : poArr.slice(0, 6).map(po => (
                    <div key={po.poId} className="activity-item">
                      <div className="activity-item__text">
                        <span className="activity-item__action">{po.poNumber}</span>
                        <span className="activity-item__time">{(po.status||'').toUpperCase()} — {fmt$(po.totalAmount)}</span>
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </div>
        )}

        {/* Inventory low stock */}
        {hasInventory && (
          <div className="dash-card">
            <div className="dash-card__head"><h3>Low Stock Alerts</h3><Badge status="WARNING"/></div>
            {stL ? <PageLoad/> : stE ? <PageError message={stE} onRetry={stR}/> : (
              <div className="activity-list">
                {stockArr.filter(s => Number(s.quantityAvailable || 0) <= 5).length === 0
                  ? <div className="empty-state">All stock levels OK</div>
                  : stockArr.filter(s => Number(s.quantityAvailable || 0) <= 5).slice(0, 6).map(s => (
                    <div key={s.locationId} className="activity-item">
                      <div className="activity-item__text">
                        <span className="activity-item__action">{s.locationName}</span>
                        <span className="activity-item__time">Available: {s.quantityAvailable}</span>
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </div>
        )}

        {/* Admin: recent activity */}
        {hasAdmin && (
          <div className="dash-card">
            <div className="dash-card__head"><h3>Recent Activity</h3></div>
            {auL ? <PageLoad/> : auE ? <PageError message={auE} onRetry={auR}/> : (
              <div className="activity-list">
                {auditArr.length === 0
                  ? <div className="empty-state">No recent activity</div>
                  : auditArr.map(a => (
                    <div key={a.auditId} className="activity-item">
                      <div className="avatar-xs" style={{ background: avatarBg(a.userEmail || 'U'), color: '#fff', flexShrink: 0 }}>
                        {initials(a.userEmail || 'U')}
                      </div>
                      <div className="activity-item__text">
                        <span className="activity-item__action">{a.action} — {a.entityType}{a.entityId ? ' #' + a.entityId : ''}</span>
                        <span className="activity-item__time">{fmtDateTime(a.createdAt)}</span>
                      </div>
                    </div>
                  ))}
              </div>
            )}
          </div>
        )}

        {/* Master Data: partner / product counts */}
        {hasMaster && !hasSales && !hasAdmin && (
          <div className="dash-card">
            <div className="dash-card__head"><h3>Master Data Summary</h3></div>
            <div className="activity-list">
              <div className="activity-item">
                <div className="activity-item__text">
                  <span className="activity-item__action">Products in catalog</span>
                  <span className="activity-item__time">{productArr.length} items</span>
                </div>
              </div>
              <div className="activity-item">
                <div className="activity-item__text">
                  <span className="activity-item__action">Business Partners</span>
                  <span className="activity-item__time">{partnerArr.length} ({partnerArr.filter(p=>p.type==='CUSTOMER'||p.type==='BOTH').length} customers, {partnerArr.filter(p=>p.type==='SUPPLIER'||p.type==='BOTH').length} suppliers)</span>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Unpaid invoices table — Sales only */}
      {hasSales && (
        <div className="dash-card" style={{ marginTop: 20 }}>
          <div className="dash-card__head">
            <h3>Unpaid Invoices</h3>
            <Badge status="UNPAID"/>
          </div>
          {ivL ? <PageLoad/> : ivE ? <PageError message={ivE} onRetry={ivR}/> : (
            pendingInvItems.length === 0
              ? <EmptyState message="No unpaid invoices"/>
              : (
                <table className="dt mini-dt">
                  <thead>
                    <tr><th>Invoice #</th><th>Order Ref</th><th>Customer</th><th>Amount</th><th>Due</th></tr>
                  </thead>
                  <tbody>
                    {pendingInvItems.map(inv => (
                      <tr key={inv.invoiceId}>
                        <td className="mono">{inv.invoiceNumber}</td>
                        <td className="mono">{inv.salesOrderId ? 'SO #' + inv.salesOrderId : '—'}</td>
                        <td>{inv.partnerName || '—'}</td>
                        <td className="mono">{fmt$(inv.totalAmount)}</td>
                        <td>{inv.dueDate || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )
          )}
        </div>
      )}

      {/* Fallback for users with no meaningful modules */}
      {!hasSales && !hasPurchasing && !hasInventory && !hasAdmin && !hasMaster && (
        <div className="dash-card" style={{ marginTop: 20 }}>
          <EmptyState message="No modules assigned to your role. Contact your administrator."/>
        </div>
      )}
    </div>
  );
}

Object.assign(window, { DashboardPage });
