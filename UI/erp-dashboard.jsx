// ============================================================
// ERP System — Dashboard Page
// ============================================================

function DashboardPage() {
  const cid = getCompanyId();

  const { data: stats,   loading: stL, error: stE,  reload: stR  } = useLoad(() => adminApi('/api/companies/' + cid + '/reports/admin-stats'));
  const { data: orders,  loading: orL, error: orE,  reload: orR  } = useLoad(() => erpApi('/api/sales-orders'));
  const { data: audits,  loading: auL, error: auE,  reload: auR  } = useLoad(() => adminApi('/api/audit-log?limit=8'));
  const { data: invList, loading: ivL, error: ivE,  reload: ivR  } = useLoad(() => erpApi('/api/sales-invoices'));

  const orderArr  = orders  || [];
  const auditArr  = audits  || [];
  const invArr    = invList || [];
  const statsObj  = stats   || {};

  // KPIs from real data
  const totalRevenue  = orderArr.filter(o => ['CONFIRMED','COMPLETED'].includes((o.status||'').toUpperCase()))
                                .reduce((s, o) => s + Number(o.totalAmount || 0), 0);
  const openOrders    = orderArr.filter(o => !['COMPLETED','CANCELLED'].includes((o.status||'').toUpperCase())).length;
  const pendingInv    = invArr.filter(i => (i.status||'').toUpperCase() === 'PENDING').length;
  const activeUsers   = statsObj.activeUsers ?? '—';

  // Monthly revenue chart (group orders by month)
  const monthlyData = React.useMemo(() => {
    const map = {};
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    orderArr.forEach(o => {
      const d = o.orderDate || o.createdAt;
      if (!d) return;
      const mo = new Date(d).getMonth();
      const yr = new Date(d).getFullYear();
      const key = months[mo] + ' ' + yr;
      map[key] = (map[key] || 0) + Number(o.totalAmount || 0);
    });
    return Object.entries(map).slice(-8).map(([label, value]) => ({ label: label.split(' ')[0], value }));
  }, [orderArr]);

  // Pending items for bottom table (pending invoices)
  const pendingItems = invArr.filter(i => (i.status||'').toUpperCase() === 'PENDING').slice(0, 5);

  return (
    <div className="dashboard">
      <PageHeader title="Dashboard" subtitle={'Welcome back, ' + (getUserEmail() || 'User').split('@')[0]}/>

      <div className="kpi-grid">
        <KPICard label="Total Revenue"      value={fmt$(totalRevenue)} trend="Confirmed + Completed orders" trendUp/>
        <KPICard label="Active Users"       value={String(activeUsers)} trend={statsObj.totalUsers ? statsObj.totalUsers + ' total' : ''} trendUp/>
        <KPICard label="Pending Approvals"  value={String(pendingInv)} trend="Invoices awaiting review" trendUp={pendingInv === 0}/>
        <KPICard label="Open Orders"        value={String(openOrders)} trend="Not yet completed" trendUp/>
      </div>

      <div className="dash-grid">
        <div className="dash-card dash-card--wide">
          <div className="dash-card__head">
            <h3>Revenue Overview</h3>
            <span className="dash-card__sub">By order month</span>
          </div>
          {orL ? <PageLoad/> : orE ? <PageError message={orE} onRetry={orR}/> :
            monthlyData.length > 0 ? <BarChart data={monthlyData} height={220}/> :
            <div className="empty-state">No order data yet</div>}
        </div>

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
      </div>

      <div className="dash-card" style={{ marginTop: 20 }}>
        <div className="dash-card__head">
          <h3>Pending Invoice Approvals</h3>
          <Badge status="PENDING"/>
        </div>
        {ivL ? <PageLoad/> : ivE ? <PageError message={ivE} onRetry={ivR}/> : (
          pendingItems.length === 0
            ? <EmptyState message="No pending invoice approvals"/>
            : (
              <table className="dt mini-dt">
                <thead>
                  <tr><th>Invoice #</th><th>Order Ref</th><th>Customer</th><th>Amount</th><th></th></tr>
                </thead>
                <tbody>
                  {pendingItems.map(inv => (
                    <tr key={inv.invoiceId}>
                      <td className="mono">{inv.invoiceNumber}</td>
                      <td className="mono">{inv.salesOrderId ? 'SO #' + inv.salesOrderId : '—'}</td>
                      <td>{inv.partnerName || '—'}</td>
                      <td className="mono">{fmt$(inv.totalAmount)}</td>
                      <td><Badge status="PENDING"/></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )
        )}
      </div>
    </div>
  );
}

Object.assign(window, { DashboardPage });
