// ============================================================
// ERP System — Sales Pages (Orders, Invoices, Payments, Returns)
// All data from ERP main module at http://localhost:8080
// ============================================================

const SO_COLS = [
  { key: 'salesOrderNumber', label: 'Order #',  width: '120px' },
  { key: 'partnerName',      label: 'Customer',  type: 'avatar' },
  { key: 'totalAmount',      label: 'Total',     type: 'currency' },
  { key: 'status',           label: 'Status',    type: 'badge', width: '130px' },
  { key: 'orderDate',        label: 'Date',      type: 'date', width: '120px' },
];
const INV_COLS = [
  { key: 'invoiceNumber', label: 'Invoice #', width: '120px' },
  { key: 'orderRef',      label: 'Order Ref', width: '100px' },
  { key: 'partnerName',   label: 'Customer',  type: 'avatar' },
  { key: 'totalAmount',   label: 'Amount',    type: 'currency' },
  { key: 'status',        label: 'Status',    type: 'badge', width: '120px' },
  { key: 'invoiceDate',   label: 'Date',      type: 'date', width: '120px' },
];
const PAY_COLS = [
  { key: 'paymentId',     label: 'Pay ID',     width: '80px' },
  { key: 'invoiceNumber', label: 'Invoice Ref', width: '120px' },
  { key: 'paymentDate',   label: 'Date',        type: 'date', width: '120px' },
  { key: 'amount',        label: 'Amount Paid', type: 'currency' },
  { key: 'paymentMethod', label: 'Method' },
];
const RET_COLS = [
  { key: 'returnNumber',  label: 'Return #',  width: '110px' },
  { key: 'invoiceNumber', label: 'Invoice',   width: '110px' },
  { key: 'status',        label: 'Status',    type: 'badge', width: '120px' },
  { key: 'returnDate',    label: 'Date',      type: 'date', width: '120px' },
  { key: 'reason',        label: 'Reason' },
];

// ===== SALES ORDERS PAGE =====
function SalesOrdersPage() {
  const [sel, setSel]         = useState(null);
  const [modal, setModal]     = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newItem, setNewItem] = useState({ partnerId: '', lines: [] });

  const { data: ordersRaw, loading, error, reload } = useLoad(() => erpApi('/api/sales-orders'));
  const { data: partnersRaw } = useLoad(() => erpApi('/api/business-partners'));
  const { data: productsRaw } = useLoad(() => erpApi('/api/products'));

  const orders = ordersRaw || [];
  const partners = (partnersRaw || []).filter(p => p.type === 'CUSTOMER' || p.type === 'BOTH');
  const products = productsRaw || [];

  const confirmOrder = async () => {
    if (!sel) return;
    setActionErr('');
    try {
      await erpApi('/api/sales-orders/' + sel.salesOrderId + '/confirm', { method: 'PUT' });
      setSel(null);
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  const doSave = async () => {
    setActionErr('');
    if (!newItem.partnerId) return setActionErr('Please select a customer');
    if (!newItem.lines || newItem.lines.length === 0) return setActionErr('Please add at least one line item');
    
    try {
      const body = {
        partnerId:   Number(newItem.partnerId),
        createdById: Number(getUserId()) || 1,
        orderDate:   new Date().toISOString().slice(0, 10),
        status:      'DRAFT',
        lines:       newItem.lines.map(l => ({
          productId: Number(l.productId),
          quantity:  parseFloat(l.quantity),
          unitPrice: parseFloat(l.unitPrice)
        }))
      };
      if (newItem.id) {
        await erpApi('/api/sales-orders/' + newItem.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/sales-orders', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  const deleteOrder = async () => {
    if (!sel || !confirm('Are you sure you want to delete this order?')) return;
    try {
      await erpApi('/api/sales-orders/' + sel.salesOrderId, { method: 'DELETE' });
      setSel(null);
      reload();
    } catch(e) { alert(e.message); }
  };

  const openAdd = () => {
    setNewItem({ partnerId: '', lines: [{ productId: '', quantity: 1, unitPrice: 0 }] });
    setModal(true);
  };

  const openEdit = () => {
    setNewItem({
      id: sel.salesOrderId,
      partnerId: String(sel.partnerId),
      lines: sel.lines.map(l => ({
        productId: String(l.productId),
        quantity: l.quantity,
        unitPrice: l.unitPrice
      }))
    });
    setModal(true);
  };

  const addLine = () => {
    setNewItem(p => ({ ...p, lines: [...p.lines, { productId: '', quantity: 1, unitPrice: 0 }] }));
  };

  const updateLine = (idx, k, v) => {
    const lines = [...newItem.lines];
    lines[idx][k] = v;
    if (k === 'productId') {
      const prod = products.find(p => String(p.productId) === v);
      if (prod) lines[idx].unitPrice = prod.sellingPrice;
    }
    setNewItem(p => ({ ...p, lines }));
  };

  const removeLine = (idx) => {
    setNewItem(p => ({ ...p, lines: p.lines.filter((_, i) => i !== idx) }));
  };

  return (
    <div>
      <PageHeader title="Sales Orders" subtitle="Manage customer orders">
        <button className="btn btn--primary" onClick={openAdd}>+ Create Order</button>
      </PageHeader>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={SO_COLS} data={orders} selectedId={sel?.salesOrderId} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => { setSel(null); setActionErr(''); }}
            title={'Order ' + (sel?.salesOrderNumber || '')}
            footer={
              <div className="dp__actions">
                {actionErr && <div style={{ color: '#ef4444', fontSize: 12, width: '100%' }}>{actionErr}</div>}
                {sel && (sel.status||'').toUpperCase() === 'DRAFT' && (
                  <>
                    <button className="btn btn--secondary btn--sm" onClick={openEdit}>Edit</button>
                    <button className="btn btn--danger btn--sm" onClick={deleteOrder}>Delete</button>
                    <button className="btn btn--primary btn--sm" onClick={confirmOrder}>Confirm</button>
                  </>
                )}
              </div>
            }>
            {sel && <>
              <DPRow label="Order #"  value={sel.salesOrderNumber}/>
              <DPRow label="Customer" value={sel.partnerName || '—'}/>
              <DPRow label="Status"   value={<Badge status={sel.status}/>}/>
              <DPRow label="Date"     value={fmtDate(sel.orderDate)}/>
              <div style={{ marginTop: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', marginBottom: 4 }}>Order Lines</div>
                {(sel.lines || []).map((l, i) => (
                  <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, padding: '4px 0', borderBottom: '1px solid #f1f5f9' }}>
                    <span>{l.productName} x {l.quantity}</span>
                    <span>{fmt$(l.lineTotal || (l.quantity * l.unitPrice))}</span>
                  </div>
                ))}
              </div>
              <hr className="dp__divider"/>
              <DPRow label="Subtotal" value={fmt$(sel.subtotal)}/>
              <DPRow label="Tax"      value={fmt$(sel.taxAmount)}/>
              <DPRow label="Total"    value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={newItem.id ? 'Edit Sales Order' : 'Create Sales Order'} width={650}
        footer={<><button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newItem.id ? 'Save Changes' : 'Create Order'}</button></>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Customer" value={newItem.partnerId} onChange={v => setNewItem(p => ({...p, partnerId: v}))} required
          options={partners.map(p => ({ value: String(p.partnerId), label: p.partnerName }))} placeholder="Select customer"/>
        
        <div style={{ marginTop: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 13, fontWeight: 600 }}>Order Lines</span>
            <button className="btn btn--secondary btn--sm" onClick={addLine}>+ Add Line</button>
          </div>
          {newItem.lines.map((l, idx) => (
            <div key={idx} style={{ display: 'flex', gap: 8, marginBottom: 8, alignItems: 'flex-end' }}>
              <div style={{ flex: 2 }}>
                <FormSelect label={idx === 0 ? "Product" : ""} value={l.productId} onChange={v => updateLine(idx, 'productId', v)}
                  options={products.map(p => ({ value: String(p.productId), label: p.productName }))}/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "Qty" : ""} type="number" value={l.quantity} onChange={v => updateLine(idx, 'quantity', v)}/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "Price" : ""} type="number" value={l.unitPrice} onChange={v => updateLine(idx, 'unitPrice', v)}/>
              </div>
              <button className="btn btn--ghost btn--sm" onClick={() => removeLine(idx)} style={{ color: '#ef4444', padding: '8px' }}>✕</button>
            </div>
          ))}
        </div>
      </Modal>
    </div>
  );
}

// ===== INVOICES PAGE =====
function SalesInvoicesPage() {
  const [sel, setSel]         = useState(null);
  const [modal, setModal]     = useState(false);
  const [actionErr, setActionErr] = useState('');
  const { data: invoicesRaw, loading, error, reload } = useLoad(() => erpApi('/api/sales-invoices'));
  const { data: ordersRaw } = useLoad(() => erpApi('/api/sales-orders'));

  const invoices = React.useMemo(() => (invoicesRaw || []).map(i => ({
    ...i,
    orderRef: i.salesOrderId ? 'SO #' + i.salesOrderId : '—',
  })), [invoicesRaw]);

  const confirmedOrders = (ordersRaw || []).filter(o => (o.status||'').toUpperCase() === 'CONFIRMED');

  const generateInvoice = async (orderId) => {
    setActionErr('');
    try {
      await erpApi('/api/sales-invoices/generate-from-order/' + orderId, { method: 'POST' });
      setModal(false);
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  const reviewInvoice = async (decision) => {
    if (!sel) return;
    setActionErr('');
    try {
      const uid = Number(getUserId()) || 0;
      // Note: Approval flow might need backend check if it exists. 
      // Assuming /api/approvals is supported based on previous code.
      const approval = await erpApi('/api/approvals', {
        method: 'POST',
        body: JSON.stringify({ invoiceId: sel.invoiceId, requestedById: uid }),
      });
      if (approval && approval.approvalId) {
        await erpApi('/api/approvals/' + approval.approvalId + '/review', {
          method: 'PUT',
          body: JSON.stringify({ reviewedById: uid, status: decision, comments: '' }),
        });
      }
      setSel(null);
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  const deleteInvoice = async () => {
    if (!sel || !confirm('Are you sure you want to delete this invoice?')) return;
    try {
      await erpApi('/api/sales-invoices/' + sel.invoiceId, { method: 'DELETE' });
      setSel(null);
      reload();
    } catch(e) { alert(e.message); }
  };

  return (
    <div>
      <PageHeader title="Invoices" subtitle="Generate and manage sales invoices">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Generate Invoice</button>
      </PageHeader>
      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={INV_COLS} data={invoices} selectedId={sel?.invoiceId} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => { setSel(null); setActionErr(''); }}
            title={'Invoice ' + (sel?.invoiceNumber || '')}
            footer={
              <div className="dp__actions">
                {actionErr && <div style={{ color: '#ef4444', fontSize: 12, width: '100%' }}>{actionErr}</div>}
                {sel && (
                  <button className="btn btn--danger btn--sm" onClick={deleteInvoice}>Delete</button>
                )}
                {sel && (sel.status||'').toUpperCase() === 'PENDING' && <>
                  <button className="btn btn--success btn--sm" onClick={() => reviewInvoice('APPROVED')}>Approve</button>
                  <button className="btn btn--danger btn--sm"  onClick={() => reviewInvoice('REJECTED')}>Reject</button>
                </>}
              </div>
            }>
            {sel && <>
              <DPRow label="Invoice #"  value={sel.invoiceNumber}/>
              <DPRow label="Order Ref"  value={sel.orderRef}/>
              <DPRow label="Customer"   value={sel.partnerName || '—'}/>
              <DPRow label="Status"     value={<Badge status={sel.status}/>}/>
              <DPRow label="Date"       value={fmtDate(sel.invoiceDate)}/>
              <DPRow label="Due Date"   value={fmtDate(sel.dueDate)}/>
              <hr className="dp__divider"/>
              <DPRow label="Subtotal"   value={fmt$(sel.subtotal)}/>
              <DPRow label="Tax"        value={fmt$(sel.taxAmount)}/>
              <DPRow label="Total"      value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title="Generate Invoice from Order" width={500}
        footer={<><button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button></>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        {confirmedOrders.length === 0
          ? <div style={{ color: '#64748b', fontSize: 14 }}>No confirmed orders available.</div>
          : confirmedOrders.map(o => (
            <div key={o.salesOrderId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '12px 0', borderBottom: '1px solid #e2e8f0' }}>
              <div style={{ display: 'flex', flexDirection: 'column' }}>
                <span style={{ fontSize: 14, fontWeight: 600 }}>{o.salesOrderNumber}</span>
                <span style={{ fontSize: 12, color: '#64748b' }}>{o.partnerName} — {fmt$(o.totalAmount)}</span>
              </div>
              <button className="btn btn--primary btn--sm" onClick={() => generateInvoice(o.salesOrderId)}>Generate</button>
            </div>
          ))}
      </Modal>
    </div>
  );
}

// ===== PAYMENTS PAGE =====
function PaymentsPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newPay, setNewPay] = useState({ invoiceId: '', amount: '', paymentMethod: 'Bank Transfer' });

  const { data: invoicesRaw, loading: invL, error: invE, reload: invR } = useLoad(() => erpApi('/api/sales-invoices'));
  const { data: paymentsRaw, loading: payL, error: payE, reload: payR } = useLoad(() => erpApi('/api/payments'));
  
  const invoices = invoicesRaw || [];
  const payments = paymentsRaw || [];

  const doSave = async () => {
    setActionErr('');
    try {
      const body = {
        invoiceId:     Number(newPay.invoiceId),
        amount:        parseFloat(newPay.amount),
        paymentMethod: newPay.paymentMethod,
        paymentDate:   newPay.paymentDate || new Date().toISOString().slice(0, 10),
      };
      if (newPay.id) {
        await erpApi('/api/payments/' + newPay.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/payments', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      setNewPay({ invoiceId: '', amount: '', paymentMethod: 'Bank Transfer' });
      payR();
      invR();
    } catch(e) { setActionErr(e.message); }
  };

  const deletePayment = async () => {
    if (!sel || !confirm('Are you sure you want to delete this payment?')) return;
    try {
      await erpApi('/api/payments/' + sel.paymentId, { method: 'DELETE' });
      setSel(null);
      payR();
      invR();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewPay({
      id: sel.paymentId,
      invoiceId: String(sel.invoiceId),
      amount: String(sel.amount),
      paymentMethod: sel.paymentMethod,
      paymentDate: sel.paymentDate
    });
    setModal(true);
  };

  const loading = invL || payL;

  return (
    <div>
      <PageHeader title="Payments" subtitle="Track payments and reconciliation">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setNewPay({ invoiceId: '', amount: '', paymentMethod: 'Bank Transfer' }); setModal(true); }}>+ Record Payment</button>
      </PageHeader>
      {invE || payE ? <PageError message={invE || payE} onRetry={() => { invR(); payR(); }}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={PAY_COLS} data={payments} selectedId={sel?.paymentId} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Payment #' + (sel?.paymentId || '')}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={deletePayment}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Payment ID"   value={sel.paymentId}/>
              <DPRow label="Invoice Ref"  value={sel.invoiceNumber}/>
              <DPRow label="Date"         value={fmtDate(sel.paymentDate)}/>
              <DPRow label="Method"       value={sel.paymentMethod}/>
              <hr className="dp__divider"/>
              <DPRow label="Amount Paid"  value={fmt$(sel.amount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={newPay.id ? 'Edit Payment' : 'Record Payment'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newPay.id ? 'Save Changes' : 'Record'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Invoice" value={newPay.invoiceId}
          onChange={v => setNewPay(p => ({...p, invoiceId: v}))} required
          options={invoices.map(i => ({ value: String(i.invoiceId), label: i.invoiceNumber + ' — ' + fmt$(i.totalAmount) }))}
          placeholder="Select invoice"/>
        <FormRow>
          <FormInput label="Amount" type="number" value={newPay.amount}
            onChange={v => setNewPay(p => ({...p, amount: v}))} required placeholder="0.00"/>
          <FormInput label="Date" type="date" value={newPay.paymentDate || new Date().toISOString().slice(0, 10)}
            onChange={v => setNewPay(p => ({...p, paymentDate: v}))} required/>
        </FormRow>
        <FormSelect label="Payment Method" value={newPay.paymentMethod}
          onChange={v => setNewPay(p => ({...p, paymentMethod: v}))} required
          options={['Bank Transfer', 'Credit Card', 'Wire Transfer', 'Check', 'Cash']}/>
      </Modal>
    </div>
  );
}

// ===== RETURNS PAGE =====
function SalesReturnsPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [newRet, setNewRet] = useState({ invoiceId: '', reason: '', lines: [] });

  const { data: returnsRaw, loading: retL, error: retE, reload: retR } = useLoad(() => erpApi('/api/sales-returns'));
  const { data: invoicesRaw } = useLoad(() => erpApi('/api/sales-invoices'));
  const { data: prodsRaw } = useLoad(() => erpApi('/api/products'));
  const returns  = returnsRaw  || [];
  const invoices = invoicesRaw || [];
  const products = prodsRaw || [];

  const doSave = async () => {
    setActionErr('');
    if (!newRet.lines || newRet.lines.length === 0) { setActionErr('Please add at least one line item'); return; }
    try {
      const body = {
        invoiceId:   Number(newRet.invoiceId),
        processedById: Number(getUserId()) || 1,
        reason:      newRet.reason,
        returnDate:  newRet.returnDate || new Date().toISOString().slice(0, 10),
        status:      newRet.status || 'PENDING',
        lines:       newRet.lines.map(l => ({
          productId: Number(l.productId),
          quantity:  parseFloat(l.quantity),
          unitPrice: parseFloat(l.unitPrice)
        }))
      };
      if (newRet.id) {
        await erpApi('/api/sales-returns/' + newRet.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/sales-returns', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      setNewRet({ invoiceId: '', reason: '', lines: [] });
      retR();
    } catch(e) { setActionErr(e.message); }
  };

  const approveReturn = async () => {
    if (!sel) return;
    try {
      await erpApi('/api/sales-returns/' + sel.returnId + '/approve', { method: 'PUT' });
      setSel(null);
      retR();
    } catch(e) { alert(e.message); }
  };

  const deleteReturn = async () => {
    if (!sel || !confirm('Are you sure you want to delete this return?')) return;
    try {
      await erpApi('/api/sales-returns/' + sel.returnId, { method: 'DELETE' });
      setSel(null);
      retR();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewRet({
      id: sel.returnId,
      invoiceId: String(sel.invoiceId),
      reason: sel.reason,
      returnDate: sel.returnDate,
      status: sel.status,
      lines: (sel.lines || []).map(l => ({
        productId: String(l.productId),
        quantity: l.quantity,
        unitPrice: l.unitPrice
      }))
    });
    setModal(true);
  };

  const addLine = () => setNewRet(p => ({ ...p, lines: [...p.lines, { productId: '', quantity: 1, unitPrice: 0 }] }));
  
  const updateLine = (idx, k, v) => {
    const lines = [...newRet.lines];
    lines[idx][k] = v;
    if (k === 'productId') {
      const prod = products.find(p => String(p.productId) === v);
      if (prod) lines[idx].unitPrice = prod.sellingPrice;
    }
    setNewRet(p => ({ ...p, lines }));
  };

  const removeLine = (idx) => setNewRet(p => ({ ...p, lines: p.lines.filter((_, i) => i !== idx) }));

  return (
    <div>
      <PageHeader title="Sales Returns" subtitle="Process returned merchandise">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setNewRet({ invoiceId: '', reason: '', lines: [] }); setModal(true); }}>+ Process Return</button>
      </PageHeader>
      {retE ? <PageError message={retE} onRetry={retR}/> : retL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={RET_COLS} data={returns} selectedId={sel?.returnId} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => { setSel(null); setActionErr(''); }}
            title={'Return ' + (sel?.returnNumber || '')}
            footer={
              <div className="dp__actions" style={{ flexDirection: 'column', gap: 8 }}>
                {actionErr && <div style={{ color: '#ef4444', fontSize: 12, width: '100%' }}>{actionErr}</div>}
                <div style={{ display: 'flex', gap: 8, width: '100%' }}>
                  <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit</button>
                  <button className="btn btn--danger btn--sm" onClick={deleteReturn}>Delete</button>
                </div>
                {sel && (sel.status||'').toUpperCase() === 'PENDING' && (
                  <button className="btn btn--success btn--sm" style={{ width: '100%' }} onClick={approveReturn}>Approve Return</button>
                )}
              </div>
            }>
            {sel && <>
              <DPRow label="Return #"  value={sel.returnNumber}/>
              <DPRow label="Invoice"   value={sel.invoiceNumber || '—'}/>
              <DPRow label="Processed By" value={sel.processedByEmail || '—'}/>
              <DPRow label="Status"    value={<Badge status={sel.status}/>}/>
              <DPRow label="Date"      value={fmtDate(sel.returnDate)}/>
              <DPRow label="Reason"    value={sel.reason}/>
              <div style={{ marginTop: 12 }}>
                <div style={{ fontSize: 11, fontWeight: 600, color: '#94a3b8', textTransform: 'uppercase', marginBottom: 4 }}>Return Lines</div>
                {(sel.lines || []).map((l, i) => (
                  <div key={i} style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, padding: '4px 0', borderBottom: '1px solid #f1f5f9' }}>
                    <span>{l.productName} x {l.quantity}</span>
                    <span>{fmt$(l.lineTotal || (l.quantity * l.unitPrice))}</span>
                  </div>
                ))}
              </div>
              <hr className="dp__divider"/>
              <DPRow label="Total"     value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={newRet.id ? 'Edit Return' : 'Process Return'} width={650}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newRet.id ? 'Save Changes' : 'Submit'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Invoice" value={newRet.invoiceId}
          onChange={v => setNewRet(p => ({...p, invoiceId: v}))} required
          options={invoices.map(i => ({ value: String(i.invoiceId), label: i.invoiceNumber + (i.partnerName ? ' — ' + i.partnerName : '') }))}
          placeholder="Select invoice"/>
        <FormInput label="Return Date" type="date" value={newRet.returnDate || new Date().toISOString().slice(0, 10)}
          onChange={v => setNewRet(p => ({...p, returnDate: v}))} required/>
        <FormInput label="Reason for Return" value={newRet.reason}
          onChange={v => setNewRet(p => ({...p, reason: v}))} required placeholder="Describe the reason"/>
        
        <div style={{ marginTop: 16 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
            <span style={{ fontSize: 13, fontWeight: 600 }}>Return Lines</span>
            <button className="btn btn--secondary btn--sm" onClick={addLine}>+ Add Line</button>
          </div>
          {newRet.lines.map((l, idx) => {
            const selectedInv = invoices.find(i => String(i.invoiceId) === newRet.invoiceId);
            const invProducts = selectedInv && selectedInv.lines ? selectedInv.lines.map(il => ({ value: String(il.productId), label: il.productName })) : products.map(p => ({ value: String(p.productId), label: p.productName }));
            return (
            <div key={idx} style={{ display: 'flex', gap: 8, marginBottom: 8, alignItems: 'flex-end' }}>
              <div style={{ flex: 2 }}>
                <FormSelect label={idx === 0 ? "Product" : ""} value={l.productId} onChange={v => updateLine(idx, 'productId', v)}
                  options={invProducts} placeholder="Select product"/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "Qty" : ""} type="number" value={l.quantity} onChange={v => updateLine(idx, 'quantity', v)}/>
              </div>
              <div style={{ flex: 1 }}>
                <FormInput label={idx === 0 ? "Price" : ""} type="number" value={l.unitPrice} onChange={v => updateLine(idx, 'unitPrice', v)}/>
              </div>
              <button className="btn btn--ghost btn--sm" onClick={() => removeLine(idx)} style={{ color: '#ef4444', padding: '8px' }}>✕</button>
            </div>
          )})}
        </div>
      </Modal>
    </div>
  );
}

Obj

