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
  const { data: ordersRaw, loading, error, reload } = useLoad(() => erpApi('/api/sales-orders'));
  const orders = ordersRaw || [];

  const confirm = async () => {
    if (!sel) return;
    setActionErr('');
    try {
      await erpApi('/api/sales-orders/' + sel.salesOrderId + '/confirm', { method: 'PUT' });
      setSel(null);
      reload();
    } catch(e) { setActionErr(e.message); }
  };

  return (
    <div>
      <PageHeader title="Sales Orders" subtitle="Manage customer orders">
        <button className="btn btn--primary" onClick={() => setModal(true)}>+ Create Order</button>
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
                <button className="btn btn--secondary" onClick={() => setSel(null)}>Close</button>
                {sel && (sel.status||'').toUpperCase() === 'DRAFT' &&
                  <button className="btn btn--primary" onClick={confirm}>Confirm Order</button>}
              </div>
            }>
            {sel && <>
              <DPRow label="Order #"  value={sel.salesOrderNumber}/>
              <DPRow label="Customer" value={sel.partnerName || '—'}/>
              <DPRow label="Status"   value={<Badge status={sel.status}/>}/>
              <DPRow label="Date"     value={fmtDate(sel.orderDate)}/>
              <DPRow label="Lines"    value={sel.lines ? sel.lines.length + ' line items' : '—'}/>
              <hr className="dp__divider"/>
              <DPRow label="Subtotal" value={fmt$(sel.subtotal)}/>
              <DPRow label="Tax"      value={fmt$(sel.taxAmount)}/>
              <DPRow label="Total"    value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title="Create Sales Order" width={600}
        footer={<><button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={() => setModal(false)}>Close</button></>}>
        <div style={{ color: '#64748b', fontSize: 14, padding: '12px 0' }}>
          Sales order creation requires product and customer setup. Use the API directly or the Swagger UI at{' '}
          <code>http://localhost:8080/swagger-ui.html</code>.
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

  const reviewInvoice = async (decision) => {
    if (!sel) return;
    setActionErr('');
    try {
      const uid = Number(getUserId()) || 0;
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

  const generateInvoice = async (orderId) => {
    setActionErr('');
    try {
      await erpApi('/api/sales-invoices/generate-from-order/' + orderId, { method: 'POST' });
      setModal(false);
      reload();
    } catch(e) { setActionErr(e.message); }
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
                {sel && (sel.status||'').toUpperCase() === 'PENDING' && <>
                  <button className="btn btn--success" onClick={() => reviewInvoice('APPROVED')}>Approve</button>
                  <button className="btn btn--danger"  onClick={() => reviewInvoice('REJECTED')}>Reject</button>
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
            <div key={o.salesOrderId} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '8px 0', borderBottom: '1px solid #e2e8f0' }}>
              <span style={{ fontSize: 14 }}>{o.salesOrderNumber} — {o.partnerName} — {fmt$(o.totalAmount)}</span>
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
  const invoices = invoicesRaw || [];

  // Load payments for all invoices
  const [payments, setPayments] = useState([]);
  const [payLoading, setPayLoading] = useState(true);

  React.useEffect(() => {
    if (invL || !invoicesRaw) return;
    setPayLoading(true);
    Promise.all(
      invoices.map(inv => erpApi('/api/payments/invoice/' + inv.invoiceId).then(r => r || []).catch(() => []))
    ).then(results => {
      const flat = results.flat();
      setPayments(flat);
      setPayLoading(false);
    });
  }, [invoicesRaw]);

  const recordPayment = async () => {
    setActionErr('');
    try {
      await erpApi('/api/payments', {
        method: 'POST',
        body: JSON.stringify({
          invoiceId:     Number(newPay.invoiceId),
          amount:        parseFloat(newPay.amount),
          paymentMethod: newPay.paymentMethod,
          paymentDate:   new Date().toISOString().slice(0, 10),
        }),
      });
      setModal(false);
      setNewPay({ invoiceId: '', amount: '', paymentMethod: 'Bank Transfer' });
      invR();
    } catch(e) { setActionErr(e.message); }
  };

  const loading = invL || payLoading;

  return (
    <div>
      <PageHeader title="Payments" subtitle="Track payments and reconciliation">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Record Payment</button>
      </PageHeader>
      {invE ? <PageError message={invE} onRetry={invR}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={PAY_COLS} data={payments} selectedId={sel?.paymentId} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Payment #' + (sel?.paymentId || '')}>
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
      <Modal open={modal} onClose={() => setModal(false)} title="Record Payment" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={recordPayment}>Record</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Invoice" value={newPay.invoiceId}
          onChange={v => setNewPay(p => ({...p, invoiceId: v}))} required
          options={invoices.map(i => ({ value: String(i.invoiceId), label: i.invoiceNumber + ' — ' + fmt$(i.totalAmount) }))}
          placeholder="Select invoice"/>
        <FormInput label="Amount" type="number" value={newPay.amount}
          onChange={v => setNewPay(p => ({...p, amount: v}))} required placeholder="0.00"/>
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
  const [newRet, setNewRet] = useState({ invoiceId: '', reason: '' });

  const { data: returnsRaw, loading: retL, error: retE, reload: retR } = useLoad(() => erpApi('/api/sales-returns'));
  const { data: invoicesRaw } = useLoad(() => erpApi('/api/sales-invoices'));
  const returns  = returnsRaw  || [];
  const invoices = invoicesRaw || [];

  const approveReturn = async () => {
    if (!sel) return;
    setActionErr('');
    try {
      await erpApi('/api/sales-returns/' + sel.returnId + '/approve', { method: 'PUT' });
      setSel(null);
      retR();
    } catch(e) { setActionErr(e.message); }
  };

  const submitReturn = async () => {
    setActionErr('');
    try {
      await erpApi('/api/sales-returns', {
        method: 'POST',
        body: JSON.stringify({ invoiceId: Number(newRet.invoiceId), reason: newRet.reason, lines: [] }),
      });
      setModal(false);
      setNewRet({ invoiceId: '', reason: '' });
      retR();
    } catch(e) { setActionErr(e.message); }
  };

  return (
    <div>
      <PageHeader title="Sales Returns" subtitle="Process returned merchandise">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Process Return</button>
      </PageHeader>
      {retE ? <PageError message={retE} onRetry={retR}/> : retL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={RET_COLS} data={returns} selectedId={sel?.returnId} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => { setSel(null); setActionErr(''); }}
            title={'Return ' + (sel?.returnNumber || '')}
            footer={sel && (sel.status||'').toUpperCase() === 'PENDING'
              ? <div className="dp__actions">
                  {actionErr && <div style={{ color: '#ef4444', fontSize: 12, width: '100%' }}>{actionErr}</div>}
                  <button className="btn btn--success" onClick={approveReturn}>Approve Return</button>
                </div>
              : null}>
            {sel && <>
              <DPRow label="Return #"  value={sel.returnNumber}/>
              <DPRow label="Invoice"   value={sel.invoiceNumber || '—'}/>
              <DPRow label="Processed By" value={sel.processedByEmail || '—'}/>
              <DPRow label="Status"    value={<Badge status={sel.status}/>}/>
              <DPRow label="Date"      value={fmtDate(sel.returnDate)}/>
              <DPRow label="Reason"    value={sel.reason}/>
              <hr className="dp__divider"/>
              <DPRow label="Total"     value={fmt$(sel.totalAmount)} bold/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title="Process Return" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={submitReturn}>Submit</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormSelect label="Invoice" value={newRet.invoiceId}
          onChange={v => setNewRet(p => ({...p, invoiceId: v}))} required
          options={invoices.map(i => ({ value: String(i.invoiceId), label: i.invoiceNumber + (i.partnerName ? ' — ' + i.partnerName : '') }))}
          placeholder="Select invoice"/>
        <FormInput label="Reason for Return" value={newRet.reason}
          onChange={v => setNewRet(p => ({...p, reason: v}))} required placeholder="Describe the reason"/>
      </Modal>
    </div>
  );
}

Object.assign(window, { SalesOrdersPage, SalesInvoicesPage, PaymentsPage, SalesReturnsPage });
