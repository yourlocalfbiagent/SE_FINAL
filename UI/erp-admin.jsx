// ============================================================
// ERP System — Admin Pages (Users, Roles, Config)
// All data from Admin module at http://localhost:8081
// ============================================================

const USER_COLS = [
  { key: 'name',     label: 'Name',       type: 'avatar' },
  { key: 'email',    label: 'Email' },
  { key: 'roleName', label: 'Role',       type: 'badge', width: '120px' },
  { key: 'status',   label: 'Status',     type: 'badge', width: '100px' },
  { key: 'createdAt',label: 'Created',    type: 'datetime', width: '150px' },
];

const ROLE_COLS = [
  { key: 'roleName',    label: 'Role Name',   type: 'badge' },
  { key: 'description', label: 'Description' },
  { key: 'statusText',  label: 'Status',      width: '90px' },
];

// ===== USER MANAGEMENT PAGE =====
function UserManagementPage() {
  const cid = getCompanyId();
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [filterVals, setFilterVals] = useState({});
  const [actionErr, setActionErr]   = useState('');
  const [resetPw, setResetPw]       = useState('');
  const [newUser, setNewUser]       = useState({ firstName: '', lastName: '', email: '', password: '', roleId: '' });

  const { data: usersRaw, loading: uL, error: uE, reload: uR } = useLoad(() => adminApi('/api/companies/' + cid + '/users'));
  const { data: rolesRaw, loading: rL, error: rE, reload: rR } = useLoad(() => adminApi('/api/companies/' + cid + '/roles'));

  const roles = rolesRaw || [];
  const roleMap = React.useMemo(() => {
    const m = {};
    roles.forEach(r => { m[r.roleId] = r.roleName; });
    return m;
  }, [roles]);

  const users = React.useMemo(() => (usersRaw || []).map(u => ({
    ...u,
    id:       u.userId,
    name:     (u.firstName || '') + ' ' + (u.lastName || ''),
    roleName: roleMap[u.roleId] || (u.roleId ? 'Role #' + u.roleId : 'No Role'),
    status:   (u.active !== undefined ? u.active : u.isActive) ? 'ACTIVE' : 'INACTIVE',
  })), [usersRaw, roleMap]);

  const filters = [
    { key: 'status',   label: 'Status',   options: ['ACTIVE', 'INACTIVE'] },
  ];
  const filtered = users.filter(u => {
    if (filterVals.status && u.status !== filterVals.status) return false;
    return true;
  });

  const doAction = async (action) => {
    if (!sel) return;
    setActionErr('');
    try {
      if (action === 'deactivate') await adminApi('/api/users/' + sel.id + '/deactivate', { method: 'POST' });
      else if (action === 'activate')  await adminApi('/api/users/' + sel.id + '/activate', { method: 'POST' });
      else if (action === 'reset') {
        if (!resetPw) { setActionErr('Enter a new password'); return; }
        await adminApi('/api/users/' + sel.id + '/reset-password', { method: 'POST', body: JSON.stringify({ newPassword: resetPw }) });
        setResetPw('');
      }
      setSel(null);
      uR();
    } catch(e) { setActionErr(e.message); }
  };

  const doCreate = async () => {
    setActionErr('');
    try {
      await adminApi('/api/companies/' + cid + '/users', {
        method: 'POST',
        body: JSON.stringify({
          firstName: newUser.firstName, lastName: newUser.lastName,
          email: newUser.email, password: newUser.password,
          roleId: newUser.roleId ? Number(newUser.roleId) : null,
          isActive: true,
        }),
      });
      setModal(false);
      setNewUser({ firstName: '', lastName: '', email: '', password: '', roleId: '' });
      uR();
    } catch(e) { setActionErr(e.message); }
  };

  const loading = uL || rL;
  const err = uE || rE;

  return (
    <div>
      <PageHeader title="User Management" subtitle="Manage employee access and accounts">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setModal(true); }}>+ Create User</button>
      </PageHeader>

      {err ? <PageError message={err} onRetry={() => { uR(); rR(); }}/> : loading ? <PageLoad/> : (
        <>
          <FilterBar filters={filters} values={filterVals} onChange={setFilterVals}/>
          <div className="page-split">
            <div className="page-split__main">
              <DataTable columns={USER_COLS} data={filtered} selectedId={sel?.id} onRowClick={setSel} showCheck/>
            </div>
            <DetailPanel open={!!sel} onClose={() => { setSel(null); setActionErr(''); setResetPw(''); }}
              title={sel?.name || ''}
              footer={
                <div className="dp__actions">
                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8, width: '100%' }}>
                    {actionErr && <div style={{ color: '#ef4444', fontSize: 12 }}>{actionErr}</div>}
                    <div style={{ display: 'flex', gap: 8 }}>
                      <input className="field__input" placeholder="New password" type="password"
                        value={resetPw} onChange={e => setResetPw(e.target.value)}
                        style={{ flex: 1, height: 36, fontSize: 13 }}/>
                      <button className="btn btn--secondary btn--sm" onClick={() => doAction('reset')}>Reset</button>
                    </div>
                    <div style={{ display: 'flex', gap: 8 }}>
                      {sel?.status === 'ACTIVE'
                        ? <button className="btn btn--danger btn--sm" onClick={() => doAction('deactivate')}>Deactivate</button>
                        : <button className="btn btn--success btn--sm" onClick={() => doAction('activate')}>Activate</button>}
                    </div>
                  </div>
                </div>
              }>
              {sel && <>
                <div style={{ textAlign: 'center', marginBottom: 16 }}>
                  <div className="avatar-lg" style={{ background: avatarBg(sel.name), margin: '0 auto' }}>
                    {initials(sel.name)}
                  </div>
                </div>
                <DPRow label="Name"    value={sel.name}/>
                <DPRow label="Email"   value={sel.email}/>
                <DPRow label="Role"    value={<Badge status={sel.roleName}/>}/>
                <DPRow label="Status"  value={<Badge status={sel.status}/>}/>
                <DPRow label="Created" value={fmtDateTime(sel.createdAt)}/>
              </>}
            </DetailPanel>
          </div>
        </>
      )}

      <Modal open={modal} onClose={() => { setModal(false); setActionErr(''); }} title="Create User" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doCreate}>Create User</button>
        </>}>
        {actionErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{actionErr}</div>}
        <FormRow>
          <FormInput label="First Name" value={newUser.firstName} onChange={v => setNewUser(p => ({...p, firstName: v}))} required placeholder="John"/>
          <FormInput label="Last Name"  value={newUser.lastName}  onChange={v => setNewUser(p => ({...p, lastName: v}))}  required placeholder="Doe"/>
        </FormRow>
        <FormInput label="Email" type="email" value={newUser.email} onChange={v => setNewUser(p => ({...p, email: v}))} required placeholder="john@company.com"/>
        <FormSelect label="Role" value={newUser.roleId} onChange={v => setNewUser(p => ({...p, roleId: v}))}
          options={roles.map(r => ({ value: String(r.roleId), label: r.roleName }))}
          placeholder="Select role"/>
        <FormInput label="Temporary Password" type="password" value={newUser.password}
          onChange={v => setNewUser(p => ({...p, password: v}))} required placeholder="Min 8 characters"/>
      </Modal>
    </div>
  );
}

// ===== ROLE MANAGEMENT PAGE =====
function RoleManagementPage() {
  const cid = getCompanyId();
  const [sel, setSel]     = useState(null);
  const [modal, setModal] = useState(false);
  const [perms, setPerms] = useState([]);
  const [newRole, setNewRole] = useState({ roleName: '', description: '' });
  const [createErr, setCreateErr] = useState('');

  const { data: rolesRaw, loading, error, reload } = useLoad(() => adminApi('/api/companies/' + cid + '/roles'));
  const roles = React.useMemo(() => (rolesRaw || []).map(r => ({
    ...r,
    id: r.roleId,
    statusText: r.isActive ? 'Active' : 'Inactive',
  })), [rolesRaw]);

  const selectRole = async (role) => {
    setSel(role);
    setPerms([]);
    try {
      const p = await adminApi('/api/roles/' + role.roleId + '/permissions');
      setPerms(p || []);
    } catch {}
  };

  const doCreate = async () => {
    setCreateErr('');
    try {
      await adminApi('/api/companies/' + cid + '/roles', {
        method: 'POST',
        body: JSON.stringify({ roleName: newRole.roleName, description: newRole.description, isActive: true }),
      });
      setModal(false);
      setNewRole({ roleName: '', description: '' });
      reload();
    } catch(e) { setCreateErr(e.message); }
  };

  return (
    <div>
      <PageHeader title="Role Management" subtitle="Define roles and map permissions">
        <button className="btn btn--primary" onClick={() => { setCreateErr(''); setModal(true); }}>+ Create Role</button>
      </PageHeader>

      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={ROLE_COLS} data={roles} selectedId={sel?.id} onRowClick={selectRole}/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Role: ' + (sel?.roleName || '')}>
            {sel && <>
              <DPRow label="Role Name"  value={sel.roleName}/>
              <DPRow label="Description" value={sel.description || '—'}/>
              <DPRow label="Status"     value={sel.isActive ? 'Active' : 'Inactive'}/>
              <hr className="dp__divider"/>
              <h4 className="dp__section-title">Permissions ({perms.length})</h4>
              {perms.length === 0
                ? <div style={{ fontSize: 13, color: '#94a3b8', padding: '8px 0' }}>No permissions assigned</div>
                : (
                  <div className="perm-list">
                    {perms.map(p => (
                      <label key={p.permissionId} className="perm-item">
                        <input type="checkbox" checked readOnly/>
                        <span>{p.moduleName}.{p.actionName}</span>
                      </label>
                    ))}
                  </div>
                )}
            </>}
          </DetailPanel>
        </div>
      )}

      <Modal open={modal} onClose={() => setModal(false)} title="Create Role" width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doCreate}>Create Role</button>
        </>}>
        {createErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{createErr}</div>}
        <FormInput label="Role Name" value={newRole.roleName}
          onChange={v => setNewRole(p => ({...p, roleName: v}))} required placeholder="e.g. VIEWER"/>
        <FormInput label="Description" value={newRole.description}
          onChange={v => setNewRole(p => ({...p, description: v}))} placeholder="Brief description"/>
      </Modal>
    </div>
  );
}

// ===== SYSTEM CONFIG PAGE =====
function SystemConfigPage() {
  const cid = getCompanyId();
  const { data: company, loading, error, reload } = useLoad(() => adminApi('/api/companies/' + cid));

  const [form, setForm]   = useState(null);
  const [saved, setSaved] = useState(false);
  const [saveErr, setSaveErr] = useState('');

  React.useEffect(() => {
    if (company && !form) {
      setForm({
        companyName: company.companyName || '',
        currency:    company.currency    || 'USD',
        taxDefault:  company.taxDefault  != null ? String(company.taxDefault) : '0',
        locale:      company.locale      || 'en-US',
      });
    }
  }, [company]);

  const save = async () => {
    setSaveErr('');
    try {
      await adminApi('/api/companies/' + cid, {
        method: 'PUT',
        body: JSON.stringify({
          companyName: form.companyName,
          currency:    form.currency,
          taxDefault:  parseFloat(form.taxDefault) || 0,
          locale:      form.locale,
          isActive:    true,
        }),
      });
      setSaved(true);
      setTimeout(() => setSaved(false), 2000);
    } catch(e) { setSaveErr(e.message); }
  };

  if (loading) return <PageLoad/>;
  if (error)   return <PageError message={error} onRetry={reload}/>;
  if (!form)   return null;

  const set = (k, v) => setForm(p => ({ ...p, [k]: v }));

  return (
    <div>
      <PageHeader title="System Configuration" subtitle="Manage global settings and company profile">
        {saveErr && <span style={{ color: '#ef4444', fontSize: 13 }}>{saveErr}</span>}
        <button className="btn btn--primary" onClick={save}>
          {saved ? '✓ Saved' : 'Save Changes'}
        </button>
      </PageHeader>
      <div className="config-grid">
        <div className="config-card">
          <h3 className="config-card__title">Company Profile</h3>
          <FormInput label="Company Name" value={form.companyName} onChange={v => set('companyName', v)} required/>
          <FormInput label="Company ID"   value={String(cid)} onChange={() => {}} disabled/>
        </div>
        <div className="config-card">
          <h3 className="config-card__title">Localization</h3>
          <FormSelect label="Base Currency" value={form.currency} onChange={v => set('currency', v)} required
            options={[{ value: 'USD', label: 'USD — US Dollar' }, { value: 'EUR', label: 'EUR — Euro' }, { value: 'LBP', label: 'LBP — Lebanese Pound' }]}/>
          <FormSelect label="Locale" value={form.locale} onChange={v => set('locale', v)} required
            options={['en-US','en-GB','fr-FR','ar-LB']}/>
        </div>
        <div className="config-card">
          <h3 className="config-card__title">Defaults</h3>
          <FormInput label="Default Tax Rate (%)" type="number" value={form.taxDefault} onChange={v => set('taxDefault', v)} required/>
        </div>
      </div>
    </div>
  );
}

// ===== ERROR PAGE =====
function ErrorPage({ code, onNavigate }) {
  const isNotFound = code === 404;
  return (
    <div className="error-page">
      <h1 className="error-page__code">{code || 404}</h1>
      <h2 className="error-page__title">{isNotFound ? 'Page Not Found' : 'Server Error'}</h2>
      <p className="error-page__msg">
        {isNotFound
          ? 'The resource you are looking for does not exist.'
          : 'Something went wrong on our end. Please try again later.'}
      </p>
      <button className="btn btn--primary" onClick={() => onNavigate && onNavigate('/')}>
        Return to Dashboard
      </button>
    </div>
  );
}

Object.assign(window, { UserManagementPage, RoleManagementPage, SystemConfigPage, ErrorPage });
