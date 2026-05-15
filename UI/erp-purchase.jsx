// ============================================================
// ERP System — Purchase Pages (POs, Goods Receipts, Supplier Bills)
// All data from ERP main module at http://localhost:8080
// ============================================================

const PO_COLS = [
  { key: 'poNumber',    label: 'PO Number',     width: '120px' },
  { key: 'partnerName', label: 'Supplier',       type: 'avatar' },
  { key: 'orderDate',   label: 'Order Date',     type: 'date', width: '120px' },
  { key: 'totalAmount', label: 'Total',          type: 'currency' },
  { key: 'status',      label: 'Status',         type: 'badge', width: '120px' },
];
const GR_COLS = [
  { key: 'receiptId',   label: 'Receipt ID',   width: '100px' },
  { key: 'poRef',       label: 'PO Ref',        width: '100px' },
  { key: 'receiptDate', label: 'Date Received', type: 'date', width: '130px' },
  { key: 'notes',       label: 'Notes' },
];
const BILL_COLS = [
  { key: 'billNumber',  label: 'Bill #',    width: '120px' },
  { key: 'partnerName', label: 'Supplier',  type: 'avatar' },
  { key: 'totalAmount', label: 'Amount',    type: 'currency' },
  { key: 'dueDate',     label: 'Due Date',  type: 'date', width: '120px' },
  { key: 'status',      label: 'Status',    type: 'badge', width: '110px' },
];

// helper to build partner map { id → name }
function usePartnerMap() {
  const { data } = useLoad(() => erpApi('/api/business-partners'));
  return React.useMemo(() => {
    const m = {};
    (data || []).forEach(p => { m[p.partnerId] = p.partnerName; });
    return m;
  }, [data]);
}

// ===== PURCHASE ORDERS PAGE =====
function PurchaseOrdersPage() {
  const [sel, setSel]             = useState(null);
  const [modal, setModal]         = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newPO, setNewPO]         = useState({ partnerId: '', orderDate: '', lines: [] });

  const { data: partnersRaw } = useLoad(() => erpApi('/api/business-partners'));
  const { data: productsRaw } = useLoad(() => erpApi('/api/products'));
  const partnerMap = usePartnerMap();
  const { data: poRaw, loading, error, reload } = useLoad(() => erpApi('/api/purchase-orders'));

  const suppliers = React.useMemo(
    () => (partnersRaw || []).filter(p => !p.type || p.type.toUpperCase() === 'SUPPLIER'),
    [partnersRaw]
  );
  
  const products = productsRaw || [];

  const pos = React.useMemo(() => (poRaw || []).map(p => ({
    ...p,
    id:          p.poId,
    partnerName: partnerMap[p.partnerId] || ('Partner #' + p.partnerId),
  })), [poRaw, partnerMap]);

  const doSave = async () => {
    setActionErr('');
    if (!newPO.partnerId) { setActionErr('Please select a supplier'); return; }
    if (!newPO.lines || newPO.lines.length === 0) { setActionErr('Please add at least one line item'); return; }
    if (newPO.lines.some(l => !l.productId)) { setActionErr('Each line must have a product selected.'); return; }
    if (newPO.lines.some(l => (parseFloat(l.quantityOrdered) || 0) <= 0)) { setActionErr('Line quantity must be greater than zero.'); return; }
    if (newPO.lines.some(l => (parseFloat(l.unitCost) || 0) < 0)) { setActionErr('Line cost cannot be negative.'); return; }
    
    try {
      const body = {
        poNumber:    newPO.id ? newPO.poNumber : 'PO-' + Date.now(),
        partnerId:   Number(newPO.partnerId),
        supplierId:  Number(newPO.partnerId),
        orderDate:   newPO.orderDate || new Date().toISOString().slice(0, 10),
        status:      newPO.status || 'pending',
        lines:       newPO.lines.map(l => ({
          productId: Number(l.productId),
          quantityOrdered: parseFloat(l.quantityOrdered),
          unitCost: parseFloat(l.unitCost),
          unitPrice: parseFloat(l.unitCost)
        }))
      };
      if (newPO.id) {
        await erpApi('/api/purchase-orders/' + newPO.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/purchase-orders', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      reload();
    } catch (e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure?')) return;
    try {
      await erpApi('/api/purchase-orders/' + sel.poId, { method: 'DELETE' });
      setSel(null);
      reload();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewPO({
      id: sel.poId,
      poNumber: sel.poNumber,
      partnerId: String(sel.partnerId),
      orderDate: sel.orderDate,
      status: sel.status,
      lines: (sel.lines || []).map(l => ({
        productId: String(l.productId),
        quantityOrdered: String(l.quantityOrdered),
        unitCost: String(l.unitCost ?? l.unitPrice ?? 0)
      }))
    });
    setModal(true);
  };

  const openModal = () => {
    setActionErr('');
    setNewPO({ partnerId: '', orderDate: new Date().toISOString().slice(0, 10), lines: [{ productId: '', quantityOrdered: 1, unitCost: 0 }] });
    setModal(true);
  };

  const addLine = () => setNewPO(p => ({ ...p, lines: [...p.lines, { productId: '', quantityOrdered: 1, unitCost: 0 }] }));
  
  const updateLine = (idx, k, v) => {
    const lines = [...newPO.lines];
    lines[idx][k] = v;
    if (k === 'productId') {
      const prod = products.find(p => String(p.productId) === v);
      if (prod) lines[idx].unitCost = prod.costPrice || 0;
    }
    setNewPO(p => ({ ...p, lines }));
  };

  const removeLine = (idx) => setNewPO(p => ({ ...p, lines: p.lines.filter((_, i) => i !== idx) }));

  return (
    <div>
      <PageHeader title="Purchase Orders" subtitle="Manage procurement orders">
        <button className="btn btn--primary" onClick={openModal}>+ Create PO</button>
      </PageHeader>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={PO_COLS} data={pos} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => { setSel(null); setActionErr(''); }}
            title={'PO ' + (sel?.poNumber || '')}
            footer={
              <div className="dp__actions">
                {actionErr && <div style={{ color: '#ef4444', fontSize: 12, width: '100%' }}>{actionErr}</div>}
                <button className="btn btn--secondary btn--sm" onClick={openEdit}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            }>
            {sel && <>
              <DPRow label="PO Number"  value={sel.poNumber}/>
              <DPRow label="Supplier"   value={sel.partnerName}/>
              <DPRow label="Status"     value={<Badge status={sel.status}/>}/>
              <DPRow label="Order Date" value={fmtDate(sel.orderDate)}/>
              <div style={{ marginTop: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', marginBottom: 4 }}>Order Lines</div>
                {(sel.lines || []).map((l, i) => {
                  const prod = products.find(p => p.productId === l.productId);
                  return (
                    <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, padding: '4px 0', borderBottom: '1px solid #f1f5f9' }}>
                      <span>{prod ? prod.productName : 'Product #'+l.productId} x {l.quantityOrdered}</span>
                      <span>{fmt$(l.lineTotal || (l.quantityOrdered * l.unitCost))}</span>
                    </div>
                  );
                })}
              </div>
              <hr className="dp__divider"/>
              <DPRow label="Total"      value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={newPO.id ? 'Edit PO' : 'Create Purchase Order'} width={760}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newPO.id ? 'Save Changes' : 'Create PO'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Supplier" value={newPO.partnerId}
          onChange={v => setNewPO(p => ({ ...p, partnerId: v }))} required
          options={suppliers.map(p => ({ value: String(p.partnerId), label: p.partnerName }))}
          placeholder="Select supplier"/>
        <FormRow>
          <FormInput label="Order Date" type="date" value={newPO.orderDate}
            onChange={v => setNewPO(p => ({ ...p, orderDate: v }))} required/>
        </FormRow>
        
        <div style={{ marginTop: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 13, fontWeight: 600 }}>Order Lines</span>
            <button className="btn btn--secondary btn--sm" onClick={addLine}>+ Add Line</button>
          </div>
          {newPO.lines.map((l, idx) => (
            <div key={idx} style={{ display: 'flex', gap: 6, marginBottom: 8, alignItems: 'flex-end' }}>
              <div style={{ flex: 2 }}>
                <FormSelect label={idx === 0 ? "Product" : ""} value={l.productId} onChange={v => updateLine(idx, 'productId', v)}
                  options={products.map(p => ({ value: String(p.productId), label: p.productName }))}/>
              </div>
              <div style={{ flex: 0.9 }}>
                <FormInput label={idx === 0 ? "Qty" : ""} type="number" value={l.quantityOrdered} onChange={v => updateLine(idx, 'quantityOrdered', v)}/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "Cost" : ""} type="number" value={l.unitCost} onChange={v => updateLine(idx, 'unitCost', v)}/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "Total" : ""} type="text" value={((parseFloat(l.quantityOrdered)||0) * (parseFloat(l.unitCost)||0)).toFixed(2)} disabled/>
              </div>
              <button
                className="btn btn--ghost btn--sm"
                onClick={() => removeLine(idx)}
                style={{ color: '#ef4444', padding: '8px', flex: '0 0 auto', alignSelf: 'center' }}
              >
                ✕
              </button>
            </div>
          ))}
        </div>
      </Modal>
    </div>
  );
}

// ===== GOODS RECEIPTS PAGE =====
function GoodsReceiptsPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newGR, setNewGR] = useState({ poId: '', notes: '' });

  const partnerMap = usePartnerMap();
  const { data: poRaw } = useLoad(() => erpApi('/api/purchase-orders'));
  const { data: grRaw, loading, error, reload } = useLoad(() => erpApi('/api/goods-receipts'));

  const pos = React.useMemo(() => (poRaw || []).map(p => ({
    ...p,
    partnerName: partnerMap[p.partnerId] || ('Partner #' + p.partnerId),
  })), [poRaw, partnerMap]);

  const receipts = React.useMemo(() => (grRaw || []).map(g => ({
    ...g,
    id:     g.receiptId,
    poRef:  g.poId ? 'PO #' + g.poId : '—',
  })), [grRaw]);

  const doSave = async () => {
    setActionErr('');
    try {
      const body = {
        poId:        Number(newGR.poId),
        receiptDate: newGR.receiptDate || new Date().toISOString().slice(0, 10),
        notes:       newGR.notes,
      };
      if (newGR.id) {
        await erpApi('/api/goods-receipts/' + newGR.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/goods-receipts', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      setNewGR({ poId: '', notes: '' });
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure?')) return;
    try {
      await erpApi('/api/goods-receipts/' + sel.receiptId, { method: 'DELETE' });
      setSel(null);
      reload();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewGR({
      id: sel.receiptId,
      poId: String(sel.poId),
      receiptDate: sel.receiptDate,
      notes: sel.notes,
    });
    setModal(true);
  };

  return (
    <div>
      <PageHeader title="Goods Receipts" subtitle="Log inventory received from suppliers">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setNewGR({ poId: '', notes: '' }); setModal(true); }}>+ Record Receipt</button>
      </PageHeader>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={GR_COLS} data={receipts} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Receipt #' + (sel?.receiptId || '')}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" onClick={openEdit}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Receipt ID"    value={sel.receiptId}/>
              <DPRow label="PO Reference"  value={sel.poRef}/>
              <DPRow label="Date Received" value={fmtDate(sel.receiptDate)}/>
              <DPRow label="Notes"         value={sel.notes || '—'}/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={newGR.id ? 'Edit Receipt' : 'Record Goods Receipt'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newGR.id ? 'Save Changes' : 'Record'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Purchase Order" value={newGR.poId}
          onChange={v => setNewGR(p => ({...p, poId: v}))} required
          options={pos.map(p => ({ value: String(p.poId), label: p.poNumber + ' — ' + p.partnerName }))}
          placeholder="Select purchase order"/>
        <FormInput label="Notes" value={newGR.notes}
          onChange={v => setNewGR(p => ({...p, notes: v}))} placeholder="Optional notes"/>
      </Modal>
    </div>
  );
}

// ===== SUPPLIER BILLS PAGE =====
function SupplierBillsPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newBill, setNewBill] = useState({ partnerId: '', totalAmount: '', dueDate: '' });

  const partnerMap = usePartnerMap();
  const { data: partnersRaw } = useLoad(() => erpApi('/api/business-partners'));
  const { data: billsRaw, loading, error, reload } = useLoad(() => erpApi('/api/supplier-bills'));

  const bills = React.useMemo(() => (billsRaw || []).map(b => ({
    ...b,
    id:          b.billId,
    partnerName: partnerMap[b.partnerId] || ('Partner #' + b.partnerId),
  })), [billsRaw, partnerMap]);

  const partners = (partnersRaw || []).filter(p => (p.type || '').toUpperCase() === 'SUPPLIER' || p.type == null);

  const doSave = async () => {
    setActionErr('');
    try {
      const today = new Date().toISOString().slice(0, 10);
      const body = {
        billNumber:  newBill.id ? newBill.billNumber : 'BILL-' + Date.now(),
        partnerId:   Number(newBill.partnerId),
        billDate:    newBill.billDate || today,
        dueDate:     newBill.dueDate  || today,
        subtotal:    parseFloat(newBill.totalAmount) || 0,
        taxAmount:   0,
        totalAmount: parseFloat(newBill.totalAmount) || 0,
        status:      newBill.status || 'draft',
      };
      if (newBill.id) {
        await erpApi('/api/supplier-bills/' + newBill.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/supplier-bills', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      setNewBill({ partnerId: '', totalAmount: '', dueDate: '' });
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure?')) return;
    try {
      await erpApi('/api/supplier-bills/' + sel.billId, { method: 'DELETE' });
      setSel(null);
      reload();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewBill({
      id: sel.billId,
      billNumber: sel.billNumber,
      partnerId: String(sel.partnerId),
      billDate: sel.billDate,
      dueDate: sel.dueDate,
      totalAmount: String(sel.totalAmount),
      status: sel.status,
    });
    setModal(true);
  };

  const recordPayment = async () => {
    if (!sel) return;
    try {
      await erpApi('/api/supplier-bills/' + sel.billId, {
        method: 'PUT',
        body: JSON.stringify({ ...sel, status: 'paid' })
      });
      setSel(null);
      reload();
    } catch(e) { alert(e.message); }
  };

  return (
    <div>
      <PageHeader title="Supplier Bills" subtitle="Manage accounts payable">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setNewBill({ partnerId: '', totalAmount: '', dueDate: '' }); setModal(true); }}>+ Create Bill</button>
      </PageHeader>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={BILL_COLS} data={bills} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Bill ' + (sel?.billNumber || '')}
            footer={sel && (
              <div className="dp__actions" style={{ flexDirection: 'column', gap: 8 }}>
                <div style={{ display: 'flex', gap: 8, width: '100%' }}>
                  <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit</button>
                  <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
                </div>
                {['draft','unpaid','overdue','pending'].includes((sel.status||'').toLowerCase()) && (
                  <button className="btn btn--primary btn--sm" style={{ width: '100%' }} onClick={recordPayment}>Record Payment</button>
                )}
              </div>
            )}>
            {sel && <>
              <DPRow label="Bill #"    value={sel.billNumber}/>
              <DPRow label="Supplier"  value={sel.partnerName}/>
              <DPRow label="Status"    value={<Badge status={sel.status}/>}/>
              <DPRow label="Bill Date" value={fmtDate(sel.billDate)}/>
              <DPRow label="Due Date"  value={fmtDate(sel.dueDate)}/>
              <hr className="dp__divider"/>
              <DPRow label="Subtotal"  value={fmt$(sel.subtotal)}/>
              <DPRow label="Tax"       value={fmt$(sel.taxAmount)}/>
              <DPRow label="Total"     value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={newBill.id ? 'Edit Bill' : 'Create Supplier Bill'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newBill.id ? 'Save Changes' : 'Create'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Supplier" value={newBill.partnerId}
          onChange={v => setNewBill(p => ({...p, partnerId: v}))} required
          options={partners.map(p => ({ value: String(p.partnerId), label: p.partnerName }))}
          placeholder="Select supplier"/>
        <FormInput label="Total Amount" type="number" value={newBill.totalAmount}
          onChange={v => setNewBill(p => ({...p, totalAmount: v}))} required placeholder="0.00"/>
        <FormInput label="Due Date" type="date" value={newBill.dueDate}
          onChange={v => setNewBill(p => ({...p, dueDate: v}))} required/>
      </Modal>
    </div>
  );
}

Object.assign(window, { PurchaseOrdersPage, GoodsReceiptsPage, SupplierBillsPage });
