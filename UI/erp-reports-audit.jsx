// ============================================================
// ERP System — Reports & Audit Log
// Sales summary from ERP main (8080); Audit from Admin (8081)
// ============================================================

const RPT_COLS = [
  { key: 'period',      label: 'Period' },
  { key: 'orders',      label: 'Orders',        width: '80px' },
  { key: 'gross',       label: 'Gross Revenue',  type: 'currency' },
  { key: 'tax',         label: 'Tax',            type: 'currency' },
  { key: 'net',         label: 'Net Revenue',    type: 'currency' },
];

const AUDIT_COLS = [
  { key: 'createdAt',  label: 'Timestamp',  type: 'datetime', width: '160px' },
  { key: 'userEmail',  label: 'User',        type: 'avatar' },
  { key: 'action',     label: 'Action',      width: '160px' },
  { key: 'entityType', label: 'Entity',      width: '140px' },
  { key: 'entityId',   label: 'Entity ID',   width: '90px' },
  { key: 'details',    label: 'Details' },
];

// ===== EXPORT MODAL =====
function ExportModal({ open, onClose }) {
  const cid = getCompanyId();
  const [format, setFormat] = useState('CSV');
  const [from, setFrom]     = useState('');
  const [to, setTo]         = useState('');

  const doExport = () => {
    const token = getToken();
    let url = ADMIN_BASE + '/api/companies/' + cid + '/reports/audit-export?limit=500';
    if (from) url += '&from=' + from;
    if (to)   url += '&to='   + to;
    const a = document.createElement('a');
    a.href = url + '&_token=' + encodeURIComponent(token || '');
    a.download = 'audit-log.csv';
    // Use fetch with Authorization header for the download
    fetch(url, { headers: { 'Authorization': 'Bearer ' + (token || '') } })
      .then(r => r.blob())
      .then(b => {
        const link = document.createElement('a');
        link.href = URL.createObjectURL(b);
        link.download = 'audit-log.csv';
        link.click();
      })
      .catch(() => {});
    onClose();
  };

  return (
    <Modal open={open} onClose={onClose} title="Export Audit Log" width={460}
      footer={<>
        <button className="btn btn--ghost" onClick={onClose}>Cancel</button>
        <button className="btn btn--primary" onClick={doExport}>Download CSV</button>
      </>}>
      <FormRow>
        <FormInput label="Date From" type="date" value={from} onChange={setFrom}/>
        <FormInput label="Date To"   type="date" value={to}   onChange={setTo}/>
      </FormRow>
      <div className="ff" style={{ marginTop: 8 }}>
        <label className="ff__label">Format</label>
        <div className="radio-group">
          {['CSV'].map(f => (
            <label key={f} className={'radio-opt radio-opt--active'}>
              <input type="radio" name="format" value={f} checked readOnly/> {f}
            </label>
          ))}
        </div>
      </div>
    </Modal>
  );
}

// ===== SALES SUMMARY PAGE =====
function SalesSummaryPage() {
  const { data: ordersRaw, loading, error, reload } = useLoad(() => erpApi('/api/sales-orders'));
  const orders = ordersRaw || [];

  // Aggregate by month
  const { periods, chartData } = React.useMemo(() => {
    const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
    const map = {};
    orders.forEach(o => {
      const d = o.orderDate || o.createdAt;
      if (!d) return;
      const dt = new Date(d);
      const key = months[dt.getMonth()] + ' ' + dt.getFullYear();
      if (!map[key]) map[key] = { period: key, orders: 0, gross: 0, tax: 0 };
      map[key].orders++;
      map[key].gross += Number(o.totalAmount  || 0);
      map[key].tax   += Number(o.taxAmount    || 0);
    });
    const sorted = Object.values(map).sort((a, b) => new Date('1 ' + a.period) - new Date('1 ' + b.period));
    const periods = sorted.map(r => ({ ...r, net: r.gross - r.tax }));
    const chartData = periods.slice(-8).map(r => ({ label: r.period.split(' ')[0], value: r.net }));
    return { periods, chartData };
  }, [orders]);

  const exportSalesCsv = React.useCallback(() => {
    const escapeCsv = (value) => {
      const s = String(value ?? '');
      if (s.includes('"') || s.includes(',') || s.includes('\n')) {
        return '"' + s.replace(/"/g, '""') + '"';
      }
      return s;
    };

    const rows = [
      ['Period', 'Orders', 'Gross Revenue', 'Tax', 'Net Revenue'],
      ...periods.map(p => [
        p.period,
        p.orders,
        Number(p.gross || 0).toFixed(2),
        Number(p.tax || 0).toFixed(2),
        Number(p.net || 0).toFixed(2),
      ]),
    ];
    const csv = rows.map(r => r.map(escapeCsv).join(',')).join('\n');

    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = 'sales-summary.csv';
    link.click();
    URL.revokeObjectURL(link.href);
  }, [periods]);

  return (
    <div>
      <PageHeader title="Sales Summary" subtitle="Revenue trends and aggregate metrics">
        <button className="btn btn--secondary" onClick={exportSalesCsv}>
          <NavIcon type="export"/> Export CSV
        </button>
      </PageHeader>

      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <>
          <div className="dash-card" style={{ marginBottom: 24 }}>
            <div className="dash-card__head">
              <h3>Net Revenue Trend</h3>
              <span className="dash-card__sub">By order month</span>
            </div>
            {chartData.length > 0
              ? <BarChart data={chartData} height={200}/>
              : <EmptyState message="No sales data available"/>}
          </div>
          <DataTable columns={RPT_COLS} data={periods}/>
        </>
      )}

    </div>
  );
}

// ===== AUDIT LOG PAGE =====
function AuditLogPage() {
  const [filterVals, setFilterVals]   = useState({});
  const [searchVal, setSearchVal]     = useState('');
  const [exportModal, setExportModal] = useState(false);

  const buildQuery = () => {
    let q = '/api/audit-log?limit=200';
    if (filterVals.action)     q += '&action='     + encodeURIComponent(filterVals.action);
    if (filterVals.entityType) q += '&entityType=' + encodeURIComponent(filterVals.entityType);
    return q;
  };

  const { data: logsRaw, loading, error, reload } = useLoad(() => adminApi(buildQuery()));
  const logs = logsRaw || [];

  // Unique action/entity values for filter dropdowns (derived from data)
  const actions      = React.useMemo(() => [...new Set(logs.map(l => l.action).filter(Boolean))].sort(),      [logs]);
  const entityTypes  = React.useMemo(() => [...new Set(logs.map(l => l.entityType).filter(Boolean))].sort(),  [logs]);

  const filters = [
    { key: 'action',     label: 'Action',     options: actions },
    { key: 'entityType', label: 'Entity',     options: entityTypes },
  ];

  const filtered = logs.filter(l => {
    if (!searchVal) return true;
    const q = searchVal.toLowerCase();
    return (l.userEmail || '').toLowerCase().includes(q)
        || (l.details   || '').toLowerCase().includes(q)
        || String(l.entityId || '').includes(q);
  });

  return (
    <div>
      <PageHeader title="Audit Log" subtitle="Track all system modifications">
        <button className="btn btn--secondary" onClick={() => setExportModal(true)}>
          <NavIcon type="export"/> Export CSV
        </button>
      </PageHeader>

      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <>
          <FilterBar filters={filters} values={filterVals} onChange={setFilterVals}
            searchVal={searchVal} onSearchChange={setSearchVal}/>
          <DataTable columns={AUDIT_COLS} data={filtered} pageSize={10}/>
        </>
      )}

      <ExportModal open={exportModal} onClose={() => setExportModal(false)}/>
    </div>
  );
}

Object.assign(window, { SalesSummaryPage, ExportModal, AuditLogPage });
