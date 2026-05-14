// ============================================================
// ERP System — Inventory Pages (Stock Levels, Movements, Counts, Alerts)
// All data from ERP main module at http://localhost:8080
// ============================================================

const STK_COLS = [
  { key: 'locationName',       label: 'Location' },
  { key: 'productName',        label: 'Product' },
  { key: 'warehouseName',      label: 'Warehouse' },
  { key: 'quantityOnHand',     label: 'On Hand',    width: '90px' },
  { key: 'quantityReserved',   label: 'Reserved',   width: '90px' },
  { key: 'quantityAvailable',  label: 'Available',  width: '90px' },
];
const MOV_COLS = [
  { key: 'movementId',  label: 'ID',         width: '80px' },
  { key: 'productName', label: 'Product' },
  { key: 'direction',   label: 'Type',       type: 'badge', width: '80px' },
  { key: 'qty',         label: 'Quantity',   width: '90px' },
  { key: 'reasonCode',  label: 'Reason' },
  { key: 'movedAt',     label: 'Date',       type: 'datetime', width: '150px' },
];
const CNT_COLS = [
  { key: 'countNumber', label: 'Count #',   width: '110px' },
  { key: 'warehouseName', label: 'Warehouse' },
  { key: 'countDate',   label: 'Date',      type: 'date', width: '120px' },
  { key: 'status',      label: 'Status',    type: 'badge', width: '120px' },
];

// ===== STOCK LEVELS PAGE =====
function StockLevelsPage() {
  const [sel, setSel]     = useState(null);
  const [filterVals, setFilterVals] = useState({});

  const { data: locsRaw,   loading: lL, error: lE, reload: lR } = useLoad(() => erpApi('/api/inventory-locations'));
  const { data: prodsRaw,  loading: pL } = useLoad(() => erpApi('/api/products'));
  const { data: whsRaw,    loading: wL } = useLoad(() => erpApi('/api/warehouses'));

  const prodMap = React.useMemo(() => {
    const m = {};
    (prodsRaw || []).forEach(p => { m[p.productId] = p; });
    return m;
  }, [prodsRaw]);

  const whMap = React.useMemo(() => {
    const m = {};
    (whsRaw || []).forEach(w => { m[w.warehouseId] = w.warehouseName; });
    return m;
  }, [whsRaw]);

  const stockRows = React.useMemo(() => (locsRaw || []).map(l => ({
    ...l,
    id:                 l.locationId,
    productName:        prodMap[l.productId]?.productName  || ('Product #' + l.productId),
    sku:                prodMap[l.productId]?.sku          || '—',
    reorderLevel:       prodMap[l.productId]?.reorderLevel || 0,
    warehouseName:      whMap[l.warehouseId]               || ('WH #' + l.warehouseId),
    quantityOnHand:     Number(l.quantityOnHand    || 0),
    quantityReserved:   Number(l.quantityReserved  || 0),
    quantityAvailable:  Number(l.quantityAvailable || 0),
  })), [locsRaw, prodMap, whMap]);

  const warehouses = React.useMemo(() => [...new Set(stockRows.map(r => r.warehouseName))], [stockRows]);
  const filters = [
    { key: 'warehouseName', label: 'Warehouse', options: warehouses },
  ];
  const filtered = stockRows.filter(s => {
    if (filterVals.warehouseName && s.warehouseName !== filterVals.warehouseName) return false;
    return true;
  });

  const loading = lL || pL || wL;

  return (
    <div>
      <PageHeader title="Stock Levels" subtitle="Real-time inventory across warehouses"/>
      {lE ? <PageError message={lE} onRetry={lR}/> : loading ? <PageLoad/> : (
        <>
          <FilterBar filters={filters} values={filterVals} onChange={setFilterVals}/>
          <div className="page-split">
            <div className="page-split__main">
              <DataTable columns={STK_COLS} data={filtered} selectedId={sel?.id} onRowClick={setSel}/>
            </div>
            <DetailPanel open={!!sel} onClose={() => setSel(null)} title={sel?.productName || ''}>
              {sel && <>
                <DPRow label="Location"   value={sel.locationName}/>
                <DPRow label="Product"    value={sel.productName}/>
                <DPRow label="SKU"        value={sel.sku}/>
                <DPRow label="Warehouse"  value={sel.warehouseName}/>
                <hr className="dp__divider"/>
                <DPRow label="On Hand"    value={sel.quantityOnHand} bold/>
                <DPRow label="Reserved"   value={sel.quantityReserved}/>
                <DPRow label="Available"  value={sel.quantityAvailable}/>
                {Number(sel.quantityAvailable) < Number(sel.reorderLevel) && sel.reorderLevel > 0 && (
                  <div className="dp__alert">Below reorder level — procurement recommended</div>
                )}
              </>}
            </DetailPanel>
          </div>
        </>
      )}
    </div>
  );
}

// ===== STOCK MOVEMENTS PAGE =====
function StockMovementsPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newMov, setNewMov] = useState({ productId: '', quantityChange: '', reasonCode: '' });

  const { data: movsRaw, loading: mL, error: mE, reload: mR } = useLoad(() => erpApi('/api/inventory/stock-movements'));
  const { data: prodsRaw } = useLoad(() => erpApi('/api/products'));

  const prodMap = React.useMemo(() => {
    const m = {};
    (prodsRaw || []).forEach(p => { m[p.productId] = p.productName; });
    return m;
  }, [prodsRaw]);

  const movements = React.useMemo(() => (movsRaw || []).map(m => ({
    ...m,
    id:          m.movementId,
    productName: prodMap[m.productId] || ('Product #' + m.productId),
    direction:   Number(m.quantityChange) >= 0 ? 'IN' : 'OUT',
    qty:         Math.abs(Number(m.quantityChange || 0)),
  })), [movsRaw, prodMap]);

  const recordMovement = async () => {
    setActionErr('');
    try {
      await erpApi('/api/inventory/stock-movements', {
        method: 'POST',
        body: JSON.stringify({
          productId:      Number(newMov.productId),
          quantityChange: parseFloat(newMov.quantityChange) || 0,
          reasonCode:     newMov.reasonCode || 'MANUAL',
        }),
      });
      setModal(false);
      setNewMov({ productId: '', quantityChange: '', reasonCode: '' });
      mR();
    } catch(e) { setActionErr(e.message); }
  };

  return (
    <div>
      <PageHeader title="Stock Movements" subtitle="Log of all inventory changes">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Record Movement</button>
      </PageHeader>
      {mE ? <PageError message={mE} onRetry={mR}/> : mL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={MOV_COLS} data={movements} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Movement #' + (sel?.movementId || '')}>
            {sel && <>
              <DPRow label="Movement ID"  value={sel.movementId}/>
              <DPRow label="Product"      value={sel.productName}/>
              <DPRow label="Type"         value={<Badge status={sel.direction}/>}/>
              <DPRow label="Qty Change"   value={sel.quantityChange} bold/>
              <DPRow label="Reason"       value={sel.reasonCode}/>
              <DPRow label="Ref Type"     value={sel.referenceType || '—'}/>
              <DPRow label="Ref ID"       value={sel.referenceId   || '—'}/>
              <DPRow label="Date"         value={fmtDateTime(sel.movedAt)}/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title="Record Stock Movement" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={recordMovement}>Record</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Product" value={newMov.productId}
          onChange={v => setNewMov(p => ({...p, productId: v}))} required
          options={(prodsRaw || []).map(p => ({ value: String(p.productId), label: p.productName + ' (' + p.sku + ')' }))}
          placeholder="Select product"/>
        <FormRow>
          <FormInput label="Quantity Change" type="number" value={newMov.quantityChange}
            onChange={v => setNewMov(p => ({...p, quantityChange: v}))} required
            placeholder="Positive = IN, Negative = OUT"/>
          <FormInput label="Reason Code" value={newMov.reasonCode}
            onChange={v => setNewMov(p => ({...p, reasonCode: v}))} placeholder="e.g. ADJUSTMENT"/>
        </FormRow>
      </Modal>
    </div>
  );
}

// ===== INVENTORY COUNTS PAGE =====
function InventoryCountsPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newCnt, setNewCnt] = useState({ warehouseId: '', countDate: '' });

  const { data: cntsRaw, loading: cL, error: cE, reload: cR } = useLoad(() => erpApi('/api/inventory/counts'));
  const { data: whsRaw } = useLoad(() => erpApi('/api/warehouses'));

  const whMap = React.useMemo(() => {
    const m = {};
    (whsRaw || []).forEach(w => { m[w.warehouseId] = w.warehouseName; });
    return m;
  }, [whsRaw]);

  const counts = React.useMemo(() => (cntsRaw || []).map(c => ({
    ...c,
    id:            c.countId,
    warehouseName: whMap[c.warehouseId] || (c.warehouseId ? 'WH #' + c.warehouseId : 'All'),
  })), [cntsRaw, whMap]);

  const warehouses = whsRaw || [];

  const createCount = async () => {
    setActionErr('');
    try {
      await erpApi('/api/inventory/counts', {
        method: 'POST',
        body: JSON.stringify({
          countNumber:  'CNT-' + Date.now(),
          warehouseId:  newCnt.warehouseId ? Number(newCnt.warehouseId) : null,
          countDate:    newCnt.countDate || new Date().toISOString().slice(0, 10),
          status:       'draft',
        }),
      });
      setModal(false);
      setNewCnt({ warehouseId: '', countDate: '' });
      cR();
    } catch(e) { setActionErr(e.message); }
  };

  return (
    <div>
      <PageHeader title="Inventory Counts" subtitle="Manage physical stock audits">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Create Count</button>
      </PageHeader>
      {cE ? <PageError message={cE} onRetry={cR}/> : cL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={CNT_COLS} data={counts} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Count ' + (sel?.countNumber || '')}>
            {sel && <>
              <DPRow label="Count #"   value={sel.countNumber}/>
              <DPRow label="Warehouse" value={sel.warehouseName}/>
              <DPRow label="Date"      value={fmtDate(sel.countDate)}/>
              <DPRow label="Status"    value={<Badge status={sel.status}/>}/>
              <DPRow label="Created"   value={fmtDateTime(sel.createdAt)}/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title="Create Inventory Count" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={createCount}>Create</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Warehouse" value={newCnt.warehouseId}
          onChange={v => setNewCnt(p => ({...p, warehouseId: v}))}
          options={warehouses.map(w => ({ value: String(w.warehouseId), label: w.warehouseName }))}
          placeholder="All warehouses"/>
        <FormInput label="Count Date" type="date" value={newCnt.countDate}
          onChange={v => setNewCnt(p => ({...p, countDate: v}))} required/>
      </Modal>
    </div>
  );
}

// ===== LOW STOCK ALERTS PAGE =====
function LowStockAlertsPage({ onNavigate }) {
  const { data: locsRaw, loading, error, reload } = useLoad(() => erpApi('/api/inventory-locations'));
  const { data: prodsRaw } = useLoad(() => erpApi('/api/products'));

  const prodMap = React.useMemo(() => {
    const m = {};
    (prodsRaw || []).forEach(p => { m[p.productId] = p; });
    return m;
  }, [prodsRaw]);

  const lowItems = React.useMemo(() => (locsRaw || [])
    .map(l => ({
      ...l,
      productName:  prodMap[l.productId]?.productName  || ('Product #' + l.productId),
      sku:          prodMap[l.productId]?.sku           || '—',
      reorderLevel: Number(prodMap[l.productId]?.reorderLevel || 0),
      available:    Number(l.quantityAvailable || 0),
    }))
    .filter(l => l.reorderLevel > 0 && l.available < l.reorderLevel),
    [locsRaw, prodMap]);

  return (
    <div>
      <PageHeader title="Low Stock Alerts" subtitle="Items requiring immediate procurement"/>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        lowItems.length === 0
          ? <EmptyState message="All stock levels are healthy"/>
          : (
            <div className="dt-wrap">
              <table className="dt">
                <thead>
                  <tr>
                    <th>Product</th><th>SKU</th>
                    <th>Available</th><th>Reorder Level</th><th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {lowItems.map(item => (
                    <tr key={item.locationId} className={item.available === 0 ? 'dt-row--danger' : ''}>
                      <td>{item.productName}</td>
                      <td className="mono">{item.sku}</td>
                      <td><span style={{ color: '#ef4444', fontWeight: 600 }}>{item.available}</span></td>
                      <td>{item.reorderLevel}</td>
                      <td>
                        <button className="btn btn--sm btn--primary"
                          onClick={() => onNavigate && onNavigate('/purchase/orders')}>
                          Create PO
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )
      )}
    </div>
  );
}

Object.assign(window, { StockLevelsPage, StockMovementsPage, InventoryCountsPage, LowStockAlertsPage });
