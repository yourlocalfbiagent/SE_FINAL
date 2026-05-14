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
  const [sel, setSel]         = useState(null);
  const [modal, setModal]     = useState(false);
  const [actionErr, setActionErr] = useState('');

  const partnerMap = usePartnerMap();
  const { data: poRaw, loading, error, reload } = useLoad(() => erpApi('/api/purchase-orders'));
  const pos = React.useMemo(() => (poRaw || []).map(p => ({
    ...p,
    id:          p.poId,
    partnerName: partnerMap[p.partnerId] || ('Partner #' + p.partnerId),
  })), [poRaw, partnerMap]);

  return (
    <div>
      <PageHeader title="Purchase Orders" subtitle="Manage procurement orders">
        <button className="btn btn--primary" onClick={() => setModal(true)}>+ Create PO</button>
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
                <button className="btn btn--secondary" onClick={() => setSel(null)}>Close</button>
              </div>
            }>
            {sel && <>
              <DPRow label="PO Number"  value={sel.poNumber}/>
              <DPRow label="Supplier"   value={sel.partnerName}/>
              <DPRow label="Status"     value={<Badge status={sel.status}/>}/>
              <DPRow label="Order Date" value={fmtDate(sel.orderDate)}/>
              <hr className="dp__divider"/>
              <DPRow label="Total"      value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title="Create Purchase Order" width={500}
        footer={<><button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={() => setModal(false)}>Close</button></>}>
        <div style={{ color: '#64748b', fontSize: 14, padding: '12px 0' }}>
          Purchase order creation requires product and supplier configuration. Use Swagger UI at{' '}
          <code>http://localhost:8080/swagger-ui.html</code>.
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

  const recordReceipt = async () => {
    setActionErr('');
    try {
      await erpApi('/api/goods-receipts', {
        method: 'POST',
        body: JSON.stringify({
          poId:        Number(newGR.poId),
          receiptDate: new Date().toISOString().slice(0, 10),
          notes:       newGR.notes,
        }),
      });
      setModal(false);
      setNewGR({ poId: '', notes: '' });
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  return (
    <div>
      <PageHeader title="Goods Receipts" subtitle="Log inventory received from suppliers">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Record Receipt</button>
      </PageHeader>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={GR_COLS} data={receipts} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Receipt #' + (sel?.receiptId || '')}>
            {sel && <>
              <DPRow label="Receipt ID"    value={sel.receiptId}/>
              <DPRow label="PO Reference"  value={sel.poRef}/>
              <DPRow label="Date Received" value={fmtDate(sel.receiptDate)}/>
              <DPRow label="Notes"         value={sel.notes || '—'}/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title="Record Goods Receipt" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={recordReceipt}>Record</button>
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

  const createBill = async () => {
    setActionErr('');
    try {
      const today = new Date().toISOString().slice(0, 10);
      await erpApi('/api/supplier-bills', {
        method: 'POST',
        body: JSON.stringify({
          billNumber:  'BILL-' + Date.now(),
          partnerId:   Number(newBill.partnerId),
          billDate:    today,
          dueDate:     newBill.dueDate || today,
          subtotal:    parseFloat(newBill.totalAmount) || 0,
          taxAmount:   0,
          totalAmount: parseFloat(newBill.totalAmount) || 0,
          status:      'draft',
        }),
      });
      setModal(false);
      setNewBill({ partnerId: '', totalAmount: '', dueDate: '' });
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  return (
    <div>
      <PageHeader title="Supplier Bills" subtitle="Manage accounts payable">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Create Bill</button>
      </PageHeader>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={BILL_COLS} data={bills} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Bill ' + (sel?.billNumber || '')}
            footer={(sel && ['draft','unpaid','overdue'].includes((sel.status||'').toLowerCase()))
              ? <div className="dp__actions"><button className="btn btn--primary">Record Payment</button></div>
              : null}>
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
      <Modal open={modal} onClose={() => setModal(false)} title="Create Supplier Bill" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={createBill}>Create</button>
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
