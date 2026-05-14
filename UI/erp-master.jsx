// ============================================================
// ERP System — Master Data Pages (Products, Partners, Warehouses)
// All data from ERP main module at http://localhost:8080
// ============================================================

const PRODUCT_COLS = [
  { key: 'productName',   label: 'Product Name' },
  { key: 'sku',           label: 'SKU',           width: '120px' },
  { key: 'categoryName',  label: 'Category',      width: '150px' },
  { key: 'unitOfMeasure', label: 'UoM',           width: '80px' },
  { key: 'sellingPrice',  label: 'Price',         type: 'currency', width: '100px' },
  { key: 'status',        label: 'Status',        type: 'badge',    width: '100px' },
];

const PARTNER_COLS = [
  { key: 'partnerName', label: 'Name',    type: 'avatar' },
  { key: 'email',       label: 'Email' },
  { key: 'type',        label: 'Type',    type: 'badge', width: '100px' },
  { key: 'country',     label: 'Country', width: '120px' },
  { key: 'status',      label: 'Status',  type: 'badge', width: '100px' },
];

const WH_COLS = [
  { key: 'warehouseName', label: 'Warehouse Name' },
  { key: 'address',       label: 'Address' },
  { key: 'status',        label: 'Status', type: 'badge', width: '100px' },
];

const CAT_COLS = [
  { key: 'categoryName', label: 'Category Name' },
  { key: 'parentName',   label: 'Parent Category' },
  { key: 'status',       label: 'Status', type: 'badge', width: '100px' },
];

// ===== PRODUCT MANAGEMENT PAGE =====
function ProductManagementPage() {
  const cid = getCompanyId();
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [item, setItem] = useState({ productName: '', sku: '', unitOfMeasure: 'pcs', costPrice: '', sellingPrice: '', reorderLevel: '', categoryId: '', isActive: true });

  const { data: itemsRaw, loading: iL, error: iE, reload: iR } = useLoad(() => erpApi('/api/products/company/' + cid));
  const { data: catsRaw } = useLoad(() => erpApi('/api/product-categories'));

  const catMap = React.useMemo(() => {
    const m = {};
    (catsRaw || []).forEach(c => { m[c.categoryId] = c.categoryName; });
    return m;
  }, [catsRaw]);

  const items = React.useMemo(() => (itemsRaw || []).map(x => ({
    ...x,
    id:           x.productId,
    categoryName: catMap[x.categoryId] || 'None',
    status:       x.isActive ? 'ACTIVE' : 'INACTIVE',
  })), [itemsRaw, catMap]);

  const doSave = async () => {
    setActionErr('');
    try {
      const body = { ...item, companyId: Number(cid), categoryId: item.categoryId ? Number(item.categoryId) : null, costPrice: parseFloat(item.costPrice) || 0, sellingPrice: parseFloat(item.sellingPrice) || 0, reorderLevel: parseFloat(item.reorderLevel) || 0 };
      if (item.id) {
        await erpApi('/api/products/' + item.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/products', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      iR();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure you want to delete this product?')) return;
    try {
      await erpApi('/api/products/' + sel.id, { method: 'DELETE' });
      setSel(null);
      iR();
    } catch(e) { alert(e.message); }
  };

  const openAdd = () => {
    setItem({ productName: '', sku: '', unitOfMeasure: 'pcs', costPrice: '', sellingPrice: '', reorderLevel: '', categoryId: '', isActive: true });
    setModal(true);
  };

  const openEdit = () => {
    setItem({ ...sel, categoryId: sel.categoryId ? String(sel.categoryId) : '' });
    setModal(true);
  };

  return (
    <div>
      <PageHeader title="Product Catalog" subtitle="Manage your products and pricing">
        <button className="btn btn--primary" onClick={openAdd}>+ Add Product</button>
      </PageHeader>

      {iE ? <PageError message={iE} onRetry={iR}/> : iL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={PRODUCT_COLS} data={items} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={sel?.productName || ''}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Product Name" value={sel.productName}/>
              <DPRow label="SKU"          value={sel.sku}/>
              <DPRow label="Category"     value={sel.categoryName}/>
              <DPRow label="UoM"          value={sel.unitOfMeasure}/>
              <hr className="dp__divider"/>
              <DPRow label="Cost Price"   value={fmt$(sel.costPrice)}/>
              <DPRow label="Selling Price" value={fmt$(sel.sellingPrice)} bold/>
              <DPRow label="Reorder Level" value={sel.reorderLevel}/>
              <DPRow label="Status"       value={<Badge status={sel.status}/>}/>
            </>}
          </DetailPanel>
        </div>
      )}

      <Modal open={modal} onClose={() => setModal(false)} title={item.id ? 'Edit Product' : 'Add Product'} width={600}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{item.id ? 'Save Changes' : 'Create Product'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormRow>
          <FormInput label="Product Name" value={item.productName} onChange={v => setItem(p => ({...p, productName: v}))} required/>
          <FormInput label="SKU"          value={item.sku}         onChange={v => setItem(p => ({...p, sku: v}))}         required/>
        </FormRow>
        <FormRow>
          <FormSelect label="Category" value={item.categoryId} onChange={v => setItem(p => ({...p, categoryId: v}))}
            options={(catsRaw || []).map(c => ({ value: String(c.categoryId), label: c.categoryName }))}/>
          <FormInput label="Unit of Measure" value={item.unitOfMeasure} onChange={v => setItem(p => ({...p, unitOfMeasure: v}))} required placeholder="pcs, kg, box"/>
        </FormRow>
        <FormRow>
          <FormInput label="Cost Price" type="number" value={item.costPrice} onChange={v => setItem(p => ({...p, costPrice: v}))} required/>
          <FormInput label="Selling Price" type="number" value={item.sellingPrice} onChange={v => setItem(p => ({...p, sellingPrice: v}))} required/>
        </FormRow>
        <FormRow>
          <FormInput label="Reorder Level" type="number" value={item.reorderLevel} onChange={v => setItem(p => ({...p, reorderLevel: v}))} required/>
          <div style={{ flex: 1, display: 'flex', alignItems: 'center', paddingTop: 24 }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: 8, fontSize: 13, cursor: 'pointer' }}>
              <input type="checkbox" checked={item.isActive} onChange={e => setItem(p => ({...p, isActive: e.target.checked}))}/>
              Active
            </label>
          </div>
        </FormRow>
      </Modal>
    </div>
  );
}

// ===== BUSINESS PARTNER MANAGEMENT PAGE =====
function PartnerManagementPage() {
  const cid = getCompanyId();
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [item, setItem] = useState({ partnerName: '', email: '', phone: '', address: '', city: '', country: '', type: 'CUSTOMER', isActive: true });

  const { data: itemsRaw, loading: iL, error: iE, reload: iR } = useLoad(() => erpApi('/api/business-partners/company/' + cid));

  const items = React.useMemo(() => (itemsRaw || []).map(x => ({
    ...x,
    id:     x.partnerId,
    status: x.isActive ? 'ACTIVE' : 'INACTIVE',
  })), [itemsRaw]);

  const doSave = async () => {
    setActionErr('');
    try {
      const body = { ...item, companyId: Number(cid) };
      if (item.id) {
        await erpApi('/api/business-partners/' + item.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/business-partners', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      iR();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure you want to delete this partner?')) return;
    try {
      await erpApi('/api/business-partners/' + sel.id, { method: 'DELETE' });
      setSel(null);
      iR();
    } catch(e) { alert(e.message); }
  };

  const openAdd = () => {
    setItem({ partnerName: '', email: '', phone: '', address: '', city: '', country: '', type: 'CUSTOMER', isActive: true });
    setModal(true);
  };

  const openEdit = () => {
    setItem({ ...sel });
    setModal(true);
  };

  return (
    <div>
      <PageHeader title="Business Partners" subtitle="Manage customers and suppliers">
        <button className="btn btn--primary" onClick={openAdd}>+ Add Partner</button>
      </PageHeader>

      {iE ? <PageError message={iE} onRetry={iR}/> : iL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={PARTNER_COLS} data={items} selectedId={sel?.id} onRowClick={setSel} showCheck/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={sel?.partnerName || ''}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <div style={{ textAlign: 'center', marginBottom: 16 }}>
                <div className="avatar-lg" style={{ background: avatarBg(sel.partnerName), margin: '0 auto' }}>{initials(sel.partnerName)}</div>
              </div>
              <DPRow label="Partner Name" value={sel.partnerName}/>
              <DPRow label="Email"        value={sel.email}/>
              <DPRow label="Phone"        value={sel.phone || '—'}/>
              <DPRow label="Type"         value={<Badge status={sel.type}/>}/>
              <DPRow label="Status"       value={<Badge status={sel.status}/>}/>
              <hr className="dp__divider"/>
              <DPRow label="Address"      value={sel.address || '—'}/>
              <DPRow label="City"         value={sel.city    || '—'}/>
              <DPRow label="Country"      value={sel.country || '—'}/>
            </>}
          </DetailPanel>
        </div>
      )}

      <Modal open={modal} onClose={() => setModal(false)} title={item.id ? 'Edit Partner' : 'Add Partner'} width={600}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{item.id ? 'Save Changes' : 'Create Partner'}</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormRow>
          <FormInput label="Partner Name" value={item.partnerName} onChange={v => setItem(p => ({...p, partnerName: v}))} required/>
          <FormSelect label="Type"        value={item.type}        onChange={v => setItem(p => ({...p, type: v}))} required
            options={['CUSTOMER', 'SUPPLIER', 'BOTH']}/>
        </FormRow>
        <FormRow>
          <FormInput label="Email" type="email" value={item.email} onChange={v => setItem(p => ({...p, email: v}))} required/>
          <FormInput label="Phone" value={item.phone} onChange={v => setItem(p => ({...p, phone: v}))}/>
        </FormRow>
        <FormInput label="Address" value={item.address} onChange={v => setItem(p => ({...p, address: v}))}/>
        <FormRow>
          <FormInput label="City"    value={item.city}    onChange={v => setItem(p => ({...p, city: v}))}/>
          <FormInput label="Country" value={item.country} onChange={v => setItem(p => ({...p, country: v}))}/>
        </FormRow>
      </Modal>
    </div>
  );
}

// ===== WAREHOUSE MANAGEMENT PAGE =====
function WarehouseManagementPage() {
  const cid = getCompanyId();
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [actionErr, setActionErr] = useState('');
  const [item, setItem] = useState({ warehouseName: '', address: '', isActive: true });

  const { data: itemsRaw, loading: iL, error: iE, reload: iR } = useLoad(() => erpApi('/api/warehouses/company/' + cid));

  const items = React.useMemo(() => (itemsRaw || []).map(x => ({
    ...x,
    id:     x.warehouseId,
    status: x.isActive ? 'ACTIVE' : 'INACTIVE',
  })), [itemsRaw]);

  const doSave = async () => {
    setActionErr('');
    try {
      const body = { ...item, companyId: Number(cid) };
      if (item.id) {
        await erpApi('/api/warehouses/' + item.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/warehouses', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      iR();
    } catch(e) { setActionErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure?')) return;
    try {
      await erpApi('/api/warehouses/' + sel.id, { method: 'DELETE' });
      setSel(null);
      iR();
    } catch(e) { alert(e.message); }
  };

  return (
    <div>
      <PageHeader title="Warehouses" subtitle="Manage storage locations">
        <button className="btn btn--primary" onClick={() => { setItem({ warehouseName: '', address: '', isActive: true }); setModal(true); }}>+ Add Warehouse</button>
      </PageHeader>
      {iE ? <PageError message={iE} onRetry={iR}/> : iL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={WH_COLS} data={items} selectedId={sel?.id} onRowClick={setSel}/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={sel?.warehouseName || ''}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={() => { setItem({...sel}); setModal(true); }}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Warehouse Name" value={sel.warehouseName}/>
              <DPRow label="Address"        value={sel.address || '—'}/>
              <DPRow label="Status"         value={<Badge status={sel.status}/>}/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={item.id ? 'Edit Warehouse' : 'Add Warehouse'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{item.id ? 'Save Changes' : 'Create'}</button>
        </>}>
        <FormInput label="Warehouse Name" value={item.warehouseName} onChange={v => setItem(p => ({...p, warehouseName: v}))} required/>
        <FormInput label="Address"        value={item.address}       onChange={v => setItem(p => ({...p, address: v}))}/>
      </Modal>
    </div>
  );
}

// ===== CATEGORY MANAGEMENT PAGE =====
function CategoryManagementPage() {
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [item, setItem] = useState({ categoryName: '', parentCategoryId: '', isActive: true });

  const { data: itemsRaw, loading: iL, error: iE, reload: iR } = useLoad(() => erpApi('/api/product-categories'));

  const catMap = React.useMemo(() => {
    const m = {};
    (itemsRaw || []).forEach(c => { m[c.categoryId] = c.categoryName; });
    return m;
  }, [itemsRaw]);

  const items = React.useMemo(() => (itemsRaw || []).map(x => ({
    ...x,
    id:         x.categoryId,
    parentName: catMap[x.parentCategoryId] || 'None',
    status:     x.isActive ? 'ACTIVE' : 'INACTIVE',
  })), [itemsRaw, catMap]);

  const doSave = async () => {
    try {
      const body = { ...item, parentCategoryId: item.parentCategoryId ? Number(item.parentCategoryId) : null };
      if (item.id) {
        await erpApi('/api/product-categories/' + item.id, { method: 'PUT', body: JSON.stringify(body) });
      } else {
        await erpApi('/api/product-categories', { method: 'POST', body: JSON.stringify(body) });
      }
      setModal(false);
      iR();
    } catch(e) { alert(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure?')) return;
    try {
      await erpApi('/api/product-categories/' + sel.id, { method: 'DELETE' });
      setSel(null);
      iR();
    } catch(e) { alert(e.message); }
  };

  return (
    <div>
      <PageHeader title="Product Categories" subtitle="Organize your product hierarchy">
        <button className="btn btn--primary" onClick={() => { setItem({ categoryName: '', parentCategoryId: '', isActive: true }); setModal(true); }}>+ Add Category</button>
      </PageHeader>
      {iE ? <PageError message={iE} onRetry={iR}/> : iL ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={CAT_COLS} data={items} selectedId={sel?.id} onRowClick={setSel}/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={sel?.categoryName || ''}
            footer={sel && (
              <div className="dp__actions">
                <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={() => { setItem({...sel, parentCategoryId: sel.parentCategoryId ? String(sel.parentCategoryId) : '' }); setModal(true); }}>Edit</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Category Name"  value={sel.categoryName}/>
              <DPRow label="Parent Category" value={sel.parentName}/>
              <DPRow label="Status"          value={<Badge status={sel.status}/>}/>
            </>}
          </DetailPanel>
        </div>
      )}
      <Modal open={modal} onClose={() => setModal(false)} title={item.id ? 'Edit Category' : 'Add Category'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{item.id ? 'Save Changes' : 'Create'}</button>
        </>}>
        <FormInput label="Category Name" value={item.categoryName} onChange={v => setItem(p => ({...p, categoryName: v}))} required/>
        <FormSelect label="Parent Category" value={item.parentCategoryId} onChange={v => setItem(p => ({...p, parentCategoryId: v}))}
          options={(itemsRaw || []).filter(c => c.categoryId !== item.id).map(c => ({ value: String(c.categoryId), label: c.categoryName }))}
          placeholder="None (Root)"/>
      </Modal>
    </div>
  );
}

Object.assign(window, { ProductManagementPage, PartnerManagementPage, WarehouseManagementPage, CategoryManagementPage });
