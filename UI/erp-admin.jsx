// ============================================================
// ERP System — Admin Pages (Users, Roles, Config)
// All data from Admin module at http://localhost:8081
// ============================================================

// --- SHARED COMPONENTS ---
function PermissionSelector({ allPermissions, grantedIds, onToggle, onToggleModule, disabled }) {
  const [search, setSearch] = React.useState('');

  const filtered = React.useMemo(() => {
    if (!search) return allPermissions;
    const s = search.toLowerCase();
    return allPermissions.filter(p => 
      p.moduleName.toLowerCase().includes(s) || 
      p.actionName.toLowerCase().includes(s)
    );
  }, [allPermissions, search]);

  const byModule = React.useMemo(() => {
    const map = {};
    filtered.forEach(p => {
      if (!map[p.moduleName]) map[p.moduleName] = [];
      map[p.moduleName].push(p);
    });
    return map;
  }, [filtered]);

  return (
    <div className="perm-container">
      <div style={{ marginBottom: 12 }}>
        <input className="ff__input" style={{ height: 34, fontSize: 13, width: '100%' }}
          placeholder="Search permissions (e.g. 'sales' or 'create')..."
          value={search} onChange={e => setSearch(e.target.value)}/>
      </div>
      <div style={{ maxHeight: 400, overflowY: 'auto', paddingRight: 4 }}>
        {Object.keys(byModule).length === 0 ? (
          <div style={{ padding: '20px 0', textAlign: 'center', color: '#94a3b8', fontSize: 13 }}>No permissions found</div>
        ) : Object.entries(byModule).sort().map(([mod, perms]) => {
          const modPermIds = perms.map(p => p.permissionId);
          const allSelected = modPermIds.every(id => grantedIds.has(id));
          return (
            <div key={mod} style={{ marginBottom: 16 }}>
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 6, borderBottom: '1px solid #f1f5f9', pb: 4 }}>
                <div style={{ fontSize: 11, fontWeight: 700, color: '#475569', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  {mod}
                </div>
                <button className="btn btn--ghost btn--sm" style={{ height: 20, fontSize: 10, padding: '0 6px' }}
                  onClick={() => onToggleModule && onToggleModule(mod, !allSelected)} disabled={disabled}>
                  {allSelected ? 'Unselect All' : 'Select All'}
                </button>
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '4px 8px' }}>
                {perms.map(p => (
                  <label key={p.permissionId} className={'perm-item' + (grantedIds.has(p.permissionId) ? ' perm-item--active' : '')}
                    style={{ cursor: disabled ? 'not-allowed' : 'pointer', opacity: disabled ? 0.7 : 1 }}>
                    <input type="checkbox"
                      checked={grantedIds.has(p.permissionId)}
                      onChange={() => onToggle(p)}
                      disabled={disabled}/>
                    <span>{p.actionName}</span>
                  </label>
                ))}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}

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
      else if (action === 'delete') {
        if (!confirm('Are you sure you want to delete this user?')) return;
        await adminApi('/api/users/' + sel.id, { method: 'DELETE' });
      }
      else if (action === 'reset') {
        if (!resetPw) { setActionErr('Enter a new password'); return; }
        await adminApi('/api/users/' + sel.id + '/reset-password', { method: 'POST', body: JSON.stringify({ newPassword: resetPw }) });
        setResetPw('');
      }
      setSel(null);
      uR();
    } catch(e) { setActionErr(e.message); }
  };

  const doSave = async () => {
    setActionErr('');
    try {
      if (newUser.id) {
        // Update
        await adminApi('/api/users/' + newUser.id, {
          method: 'PUT',
          body: JSON.stringify({
            firstName: newUser.firstName, lastName: newUser.lastName,
            email: newUser.email,
            roleId: newUser.roleId ? Number(newUser.roleId) : null,
            isActive: newUser.isActive,
            mfaEnabled: newUser.mfaEnabled,
          }),
        });
      } else {
        // Create
        await adminApi('/api/companies/' + cid + '/users', {
          method: 'POST',
          body: JSON.stringify({
            firstName: newUser.firstName, lastName: newUser.lastName,
            email: newUser.email, password: newUser.password,
            roleId: newUser.roleId ? Number(newUser.roleId) : null,
            isActive: true,
          }),
        });
      }
      setModal(false);
      setNewUser({ firstName: '', lastName: '', email: '', password: '', roleId: '' });
      uR();
    } catch(e) { setActionErr(e.message); }
  };

  const openEdit = () => {
    setNewUser({
      id: sel.id,
      firstName: sel.firstName,
      lastName: sel.lastName,
      email: sel.email,
      roleId: sel.roleId ? String(sel.roleId) : '',
      isActive: sel.active !== undefined ? sel.active : sel.isActive,
      mfaEnabled: sel.mfaEnabled,
    });
    setModal(true);
  };

  const loading = uL || rL;
  const err = uE || rE;

  return (
    <div>
      <PageHeader title="User Management" subtitle="Manage employee access and accounts">
        <button className="btn btn--primary" onClick={() => { setActionErr(''); setNewUser({ firstName: '', lastName: '', email: '', password: '', roleId: '' }); setModal(true); }}>+ Create User</button>
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
                      <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit Details</button>
                      <button className="btn btn--danger btn--sm" onClick={() => doAction('delete')}>Delete</button>
                    </div>
                    <div style={{ display: 'flex', gap: 8 }}>
                      {sel?.status === 'ACTIVE'
                        ? <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={() => doAction('deactivate')}>Deactivate</button>
                        : <button className="btn btn--success btn--sm" style={{ flex: 1 }} onClick={() => doAction('activate')}>Activate</button>}
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

      <Modal open={modal} onClose={() => { setModal(false); setActionErr(''); }} title={newUser.id ? 'Edit User' : 'Create User'} width={500}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newUser.id ? 'Save Changes' : 'Create User'}</button>
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
        {!newUser.id && (
          <FormInput label="Temporary Password" type="password" value={newUser.password}
            onChange={v => setNewUser(p => ({...p, password: v}))} required placeholder="Min 8 characters"/>
        )}
      </Modal>
    </div>
  );
}

// ===== ROLE MANAGEMENT PAGE =====
function RoleManagementPage() {
  const cid = getCompanyId();
  const [sel, setSel]         = useState(null);
  const [modal, setModal]     = useState(false);
  const [grantedIds, setGrantedIds] = useState(new Set());
  const [permBusy, setPermBusy]     = useState(false);
  const [newRole, setNewRole]   = useState({ roleName: '', description: '', permissionIds: [] });
  const [createErr, setCreateErr] = useState('');

  const { data: rolesRaw, loading, error, reload } = useLoad(() => adminApi('/api/companies/' + cid + '/roles'));
  const { data: allPermsRaw } = useLoad(() => adminApi('/api/permissions'));
  const allPerms = allPermsRaw || [];

  const roles = React.useMemo(() => (rolesRaw || []).map(r => ({
    ...r,
    id: r.roleId,
    statusText: r.isActive ? 'Active' : 'Inactive',
  })), [rolesRaw]);

  const selectRole = async (role) => {
    setSel(role);
    setGrantedIds(new Set());
    try {
      const p = await adminApi('/api/roles/' + role.roleId + '/permissions');
      setGrantedIds(new Set((p || []).map(x => x.permissionId)));
    } catch {}
  };

  const togglePerm = async (perm) => {
    if (!sel || permBusy) return;
    setPermBusy(true);
    const has = grantedIds.has(perm.permissionId);
    try {
      if (has) {
        await adminApi('/api/roles/' + sel.roleId + '/permissions/' + perm.permissionId, { method: 'DELETE' });
        setGrantedIds(prev => { const s = new Set(prev); s.delete(perm.permissionId); return s; });
      } else {
        await adminApi('/api/roles/' + sel.roleId + '/permissions/' + perm.permissionId, { method: 'POST' });
        setGrantedIds(prev => new Set([...prev, perm.permissionId]));
      }
    } catch {}
    setPermBusy(false);
  };

  const toggleModuleInDetail = async (moduleName, select) => {
    if (!sel || permBusy) return;
    setPermBusy(true);
    try {
      const modPerms = allPerms.filter(p => p.moduleName === moduleName);
      const modPermIds = modPerms.map(p => p.permissionId);
      
      const newIds = new Set(grantedIds);
      if (select) modPermIds.forEach(id => newIds.add(id));
      else modPermIds.forEach(id => newIds.delete(id));

      await adminApi('/api/roles/' + sel.roleId + '/permissions', {
        method: 'PUT',
        body: JSON.stringify({ permissionIds: Array.from(newIds) })
      });
      setGrantedIds(newIds);
    } catch(e) { alert(e.message); }
    setPermBusy(false);
  };

  const doSave = async () => {
    setCreateErr('');
    try {
      let roleId = newRole.id;
      if (newRole.id) {
        await adminApi('/api/roles/' + newRole.id, {
          method: 'PUT',
          body: JSON.stringify({ roleName: newRole.roleName, description: newRole.description, isActive: newRole.isActive }),
        });
      } else {
        const res = await adminApi('/api/companies/' + cid + '/roles', {
          method: 'POST',
          body: JSON.stringify({ roleName: newRole.roleName, description: newRole.description, isActive: true }),
        });
        roleId = res.roleId;
      }

      // Bulk update permissions from modal
      await adminApi('/api/roles/' + roleId + '/permissions', {
        method: 'PUT',
        body: JSON.stringify({ permissionIds: newRole.permissionIds })
      });

      setModal(false);
      setNewRole({ roleName: '', description: '', permissionIds: [] });
      reload();
      if (sel && sel.roleId === roleId) selectRole({ ...sel, ...newRole });
    } catch(e) { setCreateErr(e.message); }
  };

  const doDelete = async () => {
    if (!sel || !confirm('Are you sure you want to delete this role?')) return;
    try {
      await adminApi('/api/roles/' + sel.roleId, { method: 'DELETE' });
      setSel(null);
      reload();
    } catch(e) { alert(e.message); }
  };

  const openEdit = () => {
    setNewRole({ 
      id: sel.roleId, 
      roleName: sel.roleName, 
      description: sel.description, 
      isActive: sel.isActive,
      permissionIds: Array.from(grantedIds)
    });
    setModal(true);
  };

  const toggleModalPerm = (perm) => {
    setNewRole(prev => {
      const s = new Set(prev.permissionIds);
      if (s.has(perm.permissionId)) s.delete(perm.permissionId);
      else s.add(perm.permissionId);
      return { ...prev, permissionIds: Array.from(s) };
    });
  };

  const toggleModalModule = (moduleName, select) => {
    setNewRole(prev => {
      const s = new Set(prev.permissionIds);
      const modPerms = allPerms.filter(p => p.moduleName === moduleName);
      if (select) modPerms.forEach(p => s.add(p.permissionId));
      else modPerms.forEach(p => s.delete(p.permissionId));
      return { ...prev, permissionIds: Array.from(s) };
    });
  };

  return (
    <div>
      <PageHeader title="Role Management" subtitle="Define roles and assign permissions">
        <button className="btn btn--primary" onClick={() => { 
          setCreateErr(''); 
          setNewRole({ roleName: '', description: '', permissionIds: [] }); 
          setModal(true); 
        }}>+ Create Role</button>
      </PageHeader>

      {error ? <PageError message={error} onRetry={reload}/> : loading ? <PageLoad/> : (
        <div className="page-split">
          <div className="page-split__main">
            <DataTable columns={ROLE_COLS} data={roles} selectedId={sel?.id} onRowClick={selectRole}/>
          </div>
          <DetailPanel open={!!sel} onClose={() => setSel(null)} title={'Role: ' + (sel?.roleName || '')}
            footer={sel && (
              <div className="dp__actions" style={{ gap: 8 }}>
                <button className="btn btn--secondary btn--sm" style={{ flex: 1 }} onClick={openEdit}>Edit Role</button>
                <button className="btn btn--danger btn--sm" onClick={doDelete}>Delete</button>
              </div>
            )}>
            {sel && <>
              <DPRow label="Role Name"   value={sel.roleName}/>
              <DPRow label="Description" value={sel.description || '—'}/>
              <DPRow label="Status"      value={sel.isActive ? 'Active' : 'Inactive'}/>
              <hr className="dp__divider"/>
              <h4 className="dp__section-title">
                Permissions ({grantedIds.size})
                {permBusy && <span style={{ fontSize: 11, color: '#94a3b8', marginLeft: 8 }}>saving…</span>}
              </h4>
              <PermissionSelector 
                allPermissions={allPerms} 
                grantedIds={grantedIds} 
                onToggle={togglePerm}
                onToggleModule={toggleModuleInDetail}
                disabled={permBusy}
              />
            </>}
          </DetailPanel>
        </div>
      )}

      <Modal open={modal} onClose={() => setModal(false)} title={newRole.id ? 'Edit Role' : 'Create Role'} width={600}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>{newRole.id ? 'Save Changes' : 'Create Role'}</button>
        </>}>
        {createErr && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{createErr}</div>}
        <div style={{ display: 'flex', gap: 20 }}>
          <div style={{ flex: 1 }}>
            <h4 className="dp__section-title">General Info</h4>
            <FormInput label="Role Name" value={newRole.roleName}
              onChange={v => setNewRole(p => ({...p, roleName: v}))} required placeholder="e.g. VIEWER"/>
            <FormInput label="Description" value={newRole.description}
              onChange={v => setNewRole(p => ({...p, description: v}))} placeholder="Brief description"/>
          </div>
          <div style={{ flex: 1.5, borderLeft: '1px solid #e2e8f0', paddingLeft: 20 }}>
            <h4 className="dp__section-title">Assign Permissions</h4>
            <PermissionSelector 
              allPermissions={allPerms} 
              grantedIds={new Set(newRole.permissionIds)} 
              onToggle={toggleModalPerm}
              onToggleModule={toggleModalModule}
            />
          </div>
        </div>
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

// ===== PERMISSION CATALOG PAGE =====
function PermissionCatalogPage() {
  const [activeTab, setActiveTab] = useState('modules');
  const [modal, setModal] = useState(false);
  const [form, setForm]   = useState({});
  const [err, setErr]     = useState('');

  const { data: mods, reload: reloadMods } = useLoad(() => adminApi('/api/modules'));
  const { data: acts, reload: reloadActs } = useLoad(() => adminApi('/api/actions'));
  const { data: perms, reload: reloadPerms } = useLoad(() => adminApi('/api/permissions'));

  const UI_MODULES = ['MASTER DATA', 'SALES', 'PURCHASING', 'INVENTORY', 'REPORTS', 'ADMIN'];
  const UI_ACTIONS = ['create', 'read', 'update', 'delete', 'approve', 'export'];

  const doSave = async () => {
    setErr('');
    try {
      if (activeTab === 'modules') {
        if (!UI_MODULES.includes(form.moduleName)) {
           throw new Error('Module must be one of the implemented UI modules');
        }
        await adminApi('/api/modules', { method: 'POST', body: JSON.stringify(form) });
        reloadMods();
      } else if (activeTab === 'actions') {
        await adminApi('/api/actions', { method: 'POST', body: JSON.stringify(form) });
        reloadActs();
      } else if (activeTab === 'permissions') {
        await adminApi('/api/permissions', { method: 'POST', body: JSON.stringify({
          moduleName: form.moduleName,
          actionName: form.actionName,
          description: form.description
        }) });
        reloadPerms();
      }
      setModal(false);
      setForm({});
    } catch(e) { setErr(e.message); }
  };

  const columns = {
    modules: [
      { key: 'moduleId',   label: 'ID', width: '60px' },
      { key: 'moduleName', label: 'Module Name', type: 'badge' },
      { key: 'description',label: 'Scope / Description' },
    ],
    actions: [
      { key: 'actionId',   label: 'ID', width: '60px' },
      { key: 'actionName', label: 'Action Name', type: 'badge' },
      { key: 'description',label: 'Scope / Description' },
    ],
    permissions: [
      { key: 'permissionId', label: 'ID', width: '60px' },
      { key: 'moduleName',   label: 'Module', type: 'badge' },
      { key: 'actionName',   label: 'Action', type: 'badge' },
      { key: 'description',  label: 'Scope / Constraint' },
    ]
  };

  const data = activeTab === 'modules' ? mods : activeTab === 'actions' ? acts : perms;

  return (
    <div>
      <PageHeader title="Permission Catalog" subtitle="Define modules, actions, and access scopes">
        <div style={{ display: 'flex', gap: 8 }}>
          <button className={'btn ' + (activeTab === 'modules' ? 'btn--secondary' : 'btn--ghost')} onClick={() => setActiveTab('modules')}>Modules</button>
          <button className={'btn ' + (activeTab === 'actions' ? 'btn--secondary' : 'btn--ghost')} onClick={() => setActiveTab('actions')}>Actions</button>
          <button className={'btn ' + (activeTab === 'permissions' ? 'btn--secondary' : 'btn--ghost')} onClick={() => setActiveTab('permissions')}>Permissions</button>
          {activeTab !== 'modules' && (
            <button className="btn btn--primary" style={{ marginLeft: 16 }} onClick={() => { setErr(''); setForm({}); setModal(true); }}>
              + Create {activeTab.slice(0, -1)}
            </button>
          )}
        </div>
      </PageHeader>

      <div className="dt-wrap">
        <DataTable columns={columns[activeTab]} data={data || []} />
      </div>

      <Modal open={modal} onClose={() => setModal(false)} title={'Create ' + activeTab.slice(0, -1)} width={450}
        footer={<>
          <button className="btn btn--ghost" onClick={() => setModal(false)}>Cancel</button>
          <button className="btn btn--primary" onClick={doSave}>Create</button>
        </>}>
        {err && <div style={{ color: '#ef4444', fontSize: 13, marginBottom: 8 }}>{err}</div>}
        
        {activeTab === 'modules' && (
          <FormSelect label="Module Name" value={form.moduleName} onChange={v => setForm(p => ({...p, moduleName: v}))} required 
            options={UI_MODULES.map(m => ({ value: m, label: m }))} placeholder="Select UI Module"/>
        )}
        
        {activeTab === 'actions' && (
          <FormInput label="Action Name" value={form.actionName} onChange={v => setForm(p => ({...p, actionName: v}))} required placeholder="e.g. audit_all"/>
        )}

        {activeTab === 'permissions' && (
          <>
            <FormSelect label="Module" value={form.moduleName} onChange={v => setForm(p => ({...p, moduleName: v}))} required
              options={UI_MODULES.map(m => ({ value: m, label: m }))} placeholder="Select Module"/>
            <FormSelect label="Action" value={form.actionName} onChange={v => setForm(p => ({...p, actionName: v}))} required
              options={UI_ACTIONS.map(a => ({ value: a, label: a }))} placeholder="Select Action"/>
          </>
        )}

        <FormInput label="Scope / Description" value={form.description} onChange={v => setForm(p => ({...p, description: v}))} 
          placeholder="Define what this permits or restricts..." multiline/>
      </Modal>
    </div>
  );
}

Object.assign(window, { PermissionCatalogPage });
