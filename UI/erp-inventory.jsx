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
  { key: 'locationName', label: 'Location' },
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
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [filterVals, setFilterVals] = useState({});
  const [newItem, setNewItem] = useState({ locationName: '', warehouseId: '', productId: '', quantityOnHand: 0 });

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

  const doSave = async () => {
    setActionErr('');
    try {
      const body = {
        locationName: newItem.locationName,
        warehouseId:  Number(newItem.warehouseId),
        productId:    Number(newItem.productId),
        quantityOnHand: parseFloat(newItem.quantityOnHand) || 0,
        quantityReserved: parseFloat(newItem.quantityReserved) || 0,
        quantityAvailable: (parseFloat(newItem.quantityOnHand) || 0) - (parseFloat(newItem.quantityReserved) || 0)
      };
      if (newItem.id) {
        await erpApi('/api/inventory-locations/' + newItem.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/inventory-locations', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      lR();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure you want to delete this inventory location?')) return;
    try {
      await erpApi('/api/inventory-locations/' + sel.locationId, { method: 'DELETE' });
      setSel(null);
      lR();
    } catch(e) { alert(e.message); }
  };

  const openAdd = () => {
    setNewItem({ locationName: '', warehouseId: '', productId: '', quantityOnHand: 0, quantityReserved: 0 });
    setModal(true);
  };

  const openEdit = () => {
    setNewItem({
      id: sel.locationId,
      locationName: sel.locationName,
      warehouseId: String(sel.warehouseId),
      productId: String(sel.productId),
      quantityOnHand: sel.quantityOnHand,
      quantityReserved: sel.quantityReserved,
    });
    setModal(true);
  };

  const loading = lL || pL || wL;

  return (
    <div>
      <PageHeader title="Stock Levels" subtitle="Real-time inventory across warehouses">
        <button className="btn btn--primary" onClick={openAdd}>+ Add Location</button>
      </PageHeader>
      {lE ? <PageError message={lE} onRetry={lR}/> : loading ? <PageLoad/> : (
        <>
          <FilterBar filters={filters} values={filterVals} onChange={setFilterVals}/>
          <div className="page-split">
            <div className="page-split__main">
              <DataTable columns={STK_COLS} data={filtered} selectedId={sel?.id} onRowClick={setSel}/>
            </div>
            <DetailPanel open={!!sel} onClose={() => setSel(null)} title={sel?.productName || ''}
              footer={sel && (
                <div className="dp__actions">
                  <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit</button>
                  <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
                </div>
              )}>
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

      <Modal open={modal} onClose={() => setModal(false)} title={newItem.id ? 'Edit Location' : 'Add Inventory Location'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newItem.id ? 'Save Changes' : 'Add Location'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormInput label="Location Name (e.g. Bin-A1)" value={newItem.locationName} onChange={v => setNewItem(p => ({...p, locationName: v}))} required/>
        <FormRow>
          <FormSelect label="Warehouse" value={newItem.warehouseId} onChange={v => setNewItem(p => ({...p, warehouseId: v}))} required
            options={(whsRaw || []).map(w => ({ value: String(w.warehouseId), label: w.warehouseName }))}/>
          <FormSelect label="Product" value={newItem.productId} onChange={v => setNewItem(p => ({...p, productId: v}))} required
            options={(prodsRaw || []).map(p => ({ value: String(p.productId), label: p.productName }))}/>
        </FormRow>
        <FormRow>
          <FormInput label="Qty On Hand" type="number" value={newItem.quantityOnHand} onChange={v => setNewItem(p => ({...p, quantityOnHand: v}))} required/>
          <FormInput label="Qty Reserved" type="number" value={newItem.quantityReserved} onChange={v => setNewItem(p => ({...p, quantityReserved: v}))}/>
        </FormRow>
      </Modal>
    </div>
  );
}

// ===== STOCK MOVEMENTS PAGE =====
function StockMovementsPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newMov, setNewMov] = useState({ productId: '', locationId: '', quantityChange: '', reasonCode: '' });

  const { data: movsRaw, loading: mL, error: mE, reload: mR } = useLoad(() => erpApi('/api/inventory/stock-movements'));
  const { data: prodsRaw } = useLoad(() => erpApi('/api/products'));
  const { data: locsRaw } = useLoad(() => erpApi('/api/inventory-locations'));

  const prodMap = React.useMemo(() => {
    const m = {};
    (prodsRaw || []).forEach(p => { m[p.productId] = p.productName; });
    return m;
  }, [prodsRaw]);

  const locMap = React.useMemo(() => {
    const m = {};
    (locsRaw || []).forEach(l => { m[l.locationId] = l.locationName; });
    return m;
  }, [locsRaw]);

  const movements = React.useMemo(() => (movsRaw || []).map(m => ({
    ...m,
    id:          m.movementId,
    productName: prodMap[m.productId] || ('Product #' + m.productId),
    locationName: m.locationId ? (locMap[m.locationId] || ('Loc #' + m.locationId)) : '—',
    direction:   Number(m.quantityChange) >= 0 ? 'IN' : 'OUT',
    qty:         Math.abs(Number(m.quantityChange || 0)),
  })), [movsRaw, prodMap, locMap]);

  const locationOptions = React.useMemo(() => {
    const selectedProductId = newMov.productId ? Number(newMov.productId) : null;
    const filtered = selectedProductId
      ? (locsRaw || []).filter(l => l.productId === selectedProductId)
      : (locsRaw || []);
    return filtered.map(l => ({
      value: String(l.locationId),
      label: l.locationName + ' (On Hand: ' + Number(l.quantityOnHand || 0) + ')',
    }));
  }, [locsRaw, newMov.productId]);

  const doSave = async () => {
    setActionErr('');
    try {
      if (!newMov.productId) {
        setActionErr('Please select a product.');
        return;
      }
      const body = {
        productId:      Number(newMov.productId),
        locationId:     newMov.locationId ? Number(newMov.locationId) : null,
        quantityChange: parseFloat(newMov.quantityChange) || 0,
        reasonCode:     newMov.reasonCode || 'MANUAL',
      };
      if (newMov.id) {
        await erpApi('/api/inventory/stock-movements/' + newMov.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/inventory/stock-movements', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      setNewMov({ productId: '', locationId: '', quantityChange: '', reasonCode: '' });
      mR();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure?')) return;
    try {
      await erpApi('/api/inventory/stock-movements/' + sel.movementId, { method: 'DELETE' });
      setSel(null);
      mR();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewMov({
      id: sel.movementId,
      productId: String(sel.productId),
      locationId: sel.locationId ? String(sel.locationId) : '',
      quantityChange: String(sel.quantityChange),
      reasonCode: sel.reasonCode,
    });
    setModal(true);
  };

  return (
    <div>
      <PageHeader title="Stock Movements" subtitle="Log of all inventory changes">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setNewMov({ productId: '', locationId: '', quantityChange: '', reasonCode: '' }); setModal(true); }}>+ Record Movement</button>
      </PageHeader>
      {mE ? <PageError message={mE} onRetry={mR}/> : mL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={MOV_COLS} data={movements} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Movement #' + (sel?.movementId || '')}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" onClick={openEdit}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Movement ID"  value={sel.movementId}/>
              <DPRow label="Product"      value={sel.productName}/>
              <DPRow label="Location"     value={sel.locationName}/>
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
      <Modal open={modal} onClose={() => setModal(false)} title={newMov.id ? 'Edit Movement' : 'Record Stock Movement'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newMov.id ? 'Save Changes' : 'Record'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Product" value={newMov.productId}
          onChange={v => setNewMov(p => ({...p, productId: v, locationId: ''}))} required
          options={(prodsRaw || []).map(p => ({ value: String(p.productId), label: p.productName + ' (' + p.sku + ')' }))}
          placeholder="Select product"/>
        <FormSelect
          label="Location"
          value={newMov.locationId}
          onChange={v => setNewMov(p => ({ ...p, locationId: v }))}
          options={locationOptions}
          placeholder="Select inventory location"
        />
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
  const [newCnt, setNewCnt] = useState({ warehouseId: '', countDate: '', lines: [] });

  const { data: cntsRaw, loading: cL, error: cE, reload: cR } = useLoad(() => erpApi('/api/inventory/counts'));
  const { data: whsRaw } = useLoad(() => erpApi('/api/warehouses'));
  const { data: prodsRaw } = useLoad(() => erpApi('/api/products'));

  const whMap = React.useMemo(() => {
    const m = {};
    (whsRaw || []).forEach(w => { m[w.warehouseId] = w.warehouseName; });
    return m;
  }, [whsRaw]);

  const prodMap = React.useMemo(() => {
    const m = {};
    (prodsRaw || []).forEach(p => { m[p.productId] = p.productName; });
    return m;
  }, [prodsRaw]);

  const counts = React.useMemo(() => (cntsRaw || []).map(c => ({
    ...c,
    id:            c.countId,
    warehouseName: whMap[c.warehouseId] || (c.warehouseId ? 'WH #' + c.warehouseId : 'All'),
  })), [cntsRaw, whMap]);

  const warehouses = whsRaw || [];
  const products = prodsRaw || [];

  const doSave = async () => {
    setActionErr('');
    try {
      const body = {
        countNumber:  newCnt.countNumber || 'CNT-' + Date.now(),
        warehouseId:  newCnt.warehouseId ? Number(newCnt.warehouseId) : null,
        countDate:    newCnt.countDate || new Date().toISOString().slice(0, 10),
        status:       newCnt.status || 'draft',
        lines:        newCnt.lines.map(l => ({
          productId: Number(l.productId),
          systemQuantity: parseFloat(l.systemQuantity) || 0,
          countedQuantity: parseFloat(l.countedQuantity) || 0
        }))
      };
      if (newCnt.id) {
        await erpApi('/api/inventory/counts/' + newCnt.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/inventory/counts', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      setNewCnt({ warehouseId: '', countDate: '', lines: [] });
      cR();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure?')) return;
    try {
      await erpApi('/api/inventory/counts/' + sel.countId, { method: 'DELETE' });
      setSel(null);
      cR();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewCnt({
      id: sel.countId,
      countNumber: sel.countNumber,
      warehouseId: sel.warehouseId ? String(sel.warehouseId) : '',
      countDate: sel.countDate,
      status: sel.status,
      lines: (sel.lines || []).map(l => ({
        productId: String(l.productId),
        systemQuantity: l.systemQuantity,
        countedQuantity: l.countedQuantity
      }))
    });
    setModal(true);
  };

  const addLine = () => setNewCnt(p => ({ ...p, lines: [...p.lines, { productId: '', systemQuantity: 0, countedQuantity: 0 }] }));
  
  const updateLine = (idx, k, v) => {
    const lines = [...newCnt.lines];
    lines[idx][k] = v;
    setNewCnt(p => ({ ...p, lines }));
  };

  const removeLine = (idx) => setNewCnt(p => ({ ...p, lines: p.lines.filter((_, i) => i !== idx) }));

  return (
    <div>
      <PageHeader title="Inventory Counts" subtitle="Manage physical stock audits">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setNewCnt({ warehouseId: '', countDate: '', lines: [] }); setModal(true); }}>+ Create Count</button>
      </PageHeader>
      {cE ? <PageError message={cE} onRetry={cR}/> : cL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={CNT_COLS} data={counts} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Count ' + (sel?.countNumber || '')}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" onClick={openEdit}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Count #"   value={sel.countNumber}/>
              <DPRow label="Warehouse" value={sel.warehouseName}/>
              <DPRow label="Date"      value={fmtDate(sel.countDate)}/>
              <DPRow label="Status"    value={<Badge status={sel.status}/>}/>
              <DPRow label="Created"   value={fmtDateTime(sel.createdAt)}/>
              <div style={{ marginTop: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', marginBottom: 4 }}>Counted Lines</div>
                {(sel.lines || []).map((l, i) => (
                  <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, padding: '4px 0', borderBottom: '1px solid #f1f5f9' }}>
                    <span>{prodMap[l.productId] || 'Product #'+l.productId}</span>
                    <span>System: {l.systemQuantity} / Counted: {l.countedQuantity} / Var: <span style={{color: l.varianceQuantity < 0 ? '#ef4444' : (l.varianceQuantity > 0 ? '#10b981' : 'inherit')}}>{l.varianceQuantity}</span></span>
                  </div>
                ))}
              </div>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={newCnt.id ? 'Edit Count' : 'Create Inventory Count'} width={650}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newCnt.id ? 'Save Changes' : 'Create'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Warehouse" value={newCnt.warehouseId}
          onChange={v => setNewCnt(p => ({...p, warehouseId: v}))}
          options={warehouses.map(w => ({ value: String(w.warehouseId), label: w.warehouseName }))}
          placeholder="All warehouses"/>
        <FormInput label="Count Date" type="date" value={newCnt.countDate}
          onChange={v => setNewCnt(p => ({...p, countDate: v}))} required/>
        
        <div style={{ marginTop: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 13, fontWeight: 600 }}>Count Lines</span>
            <button className="btn btn--secondary btn--sm" onClick={addLine}>+ Add Line</button>
          </div>
          {newCnt.lines.map((l, idx) => (
            <div key={idx} style={{ display: 'flex', gap: 8, marginBottom: 8, alignItems: 'flex-end' }}>
              <div style={{ flex: 2 }}>
                <FormSelect label={idx === 0 ? "Product" : ""} value={l.productId} onChange={v => updateLine(idx, 'productId', v)}
                  options={products.map(p => ({ value: String(p.productId), label: p.productName }))}/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "System Qty" : ""} type="number" value={l.systemQuantity} onChange={v => updateLine(idx, 'systemQuantity', v)}/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "Counted Qty" : ""} type="number" value={l.countedQuantity} onChange={v => updateLine(idx, 'countedQuantity', v)}/>
              </div>
              <button className="btn btn--ghost btn--sm" onClick={() => removeLine(idx)} style={{ color: '#ef4444', padding: '8px' }}>✕</button>
            </div>
          ))}
        </div>
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
