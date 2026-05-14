// ============================================================
// ERP System — Authentication Components
// Login + Register + Success screens
// ============================================================

const { useState, useEffect, useCallback } = React;

// -------------------- UTILITIES --------------------
const validateEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

const pwChecks = (pw) => ({
  length: pw.length >= 8,
  upper: /[A-Z]/.test(pw),
  lower: /[a-z]/.test(pw),
  number: /\d/.test(pw),
  special: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(pw),
});

const pwScore = (pw) => {
  if (!pw) return 0;
  return Object.values(pwChecks(pw)).filter(Boolean).length;
};

const STRENGTH_META = [
  { label: '', color: '#e2e8f0' },
  { label: 'Very Weak', color: '#ef4444' },
  { label: 'Weak', color: '#f97316' },
  { label: 'Fair', color: '#eab308' },
  { label: 'Strong', color: '#22c55e' },
  { label: 'Very Strong', color: '#10b981' },
];

// -------------------- ICONS --------------------
const EyeIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
    <circle cx="12" cy="12" r="3"/>
  </svg>
);

const EyeOffIcon = () => (
  <svg width="18" height="18" viewBox="0 0 24 24" fill="none"
    stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    <path d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94"/>
    <path d="M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19"/>
    <line x1="1" y1="1" x2="23" y2="23"/>
  </svg>
);

const CheckIcon = ({ ok }) => (
  <svg width="14" height="14" viewBox="0 0 14 14" fill="none"
    stroke={ok ? '#10b981' : '#cbd5e1'} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
    {ok
      ? <path d="M3 7l3 3 5-5"/>
      : <path d="M4 4l6 6M10 4l-6 6"/>}
  </svg>
);

const AlertIcon = () => (
  <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
    <circle cx="8" cy="8" r="7" stroke="#ef4444" strokeWidth="1.5"/>
    <line x1="8" y1="4.5" x2="8" y2="8.5" stroke="#ef4444" strokeWidth="1.5" strokeLinecap="round"/>
    <circle cx="8" cy="11" r="0.75" fill="#ef4444"/>
  </svg>
);

const SuccessCheckIcon = () => (
  <svg className="success-check-svg" width="80" height="80" viewBox="0 0 80 80" fill="none">
    <circle className="success-circle" cx="40" cy="40" r="36" stroke="#10b981" strokeWidth="3"/>
    <path className="success-path" d="M24 40l10 10 22-22" stroke="#10b981"
      strokeWidth="3.5" strokeLinecap="round" strokeLinejoin="round"/>
  </svg>
);

// -------------------- LOGO --------------------
function ERPLogo({ size = 40 }) {
  return (
    <svg width={size} height={size} viewBox="0 0 40 40" fill="none">
      <rect x="2" y="2" width="16" height="16" rx="4" fill="#f59e0b"/>
      <rect x="22" y="2" width="16" height="16" rx="4" fill="#f59e0b" opacity="0.6"/>
      <rect x="2" y="22" width="16" height="16" rx="4" fill="#f59e0b" opacity="0.6"/>
      <rect x="22" y="22" width="16" height="16" rx="4" fill="#f59e0b" opacity="0.3"/>
    </svg>
  );
}

// -------------------- INPUT FIELD --------------------
function InputField({ label, type = 'text', value, onChange, onBlur, error, placeholder, disabled }) {
  return (
    <div className={'field' + (error ? ' field--error' : '')}>
      <label className="field__label">{label}</label>
      <input className="field__input" type={type} value={value}
        onChange={e => onChange(e.target.value)} onBlur={onBlur}
        placeholder={placeholder} disabled={disabled} autoComplete="off"/>
      {error && <span className="field__error-msg"><AlertIcon/> {error}</span>}
    </div>
  );
}

// -------------------- PASSWORD FIELD --------------------
function PasswordField({ label, value, onChange, onBlur, error, placeholder, showStrength, disabled }) {
  const [visible, setVisible] = useState(false);
  const score = pwScore(value);
  const checks = pwChecks(value);
  const meta = STRENGTH_META[score];

  return (
    <div className={'field' + (error ? ' field--error' : '')}>
      <label className="field__label">{label}</label>
      <div className="field__pw-wrap">
        <input className="field__input field__input--pw" type={visible ? 'text' : 'password'}
          value={value} onChange={e => onChange(e.target.value)} onBlur={onBlur}
          placeholder={placeholder} disabled={disabled} autoComplete="new-password"/>
        <button type="button" className="field__toggle"
          onClick={() => setVisible(!visible)} tabIndex={-1} aria-label="Toggle visibility">
          {visible ? <EyeOffIcon/> : <EyeIcon/>}
        </button>
      </div>
      {error && <span className="field__error-msg"><AlertIcon/> {error}</span>}
      {showStrength && value.length > 0 && (
        <div className="strength">
          <div className="strength__bar">
            <div className="strength__fill" style={{ width: `${score * 20}%`, background: meta.color }}/>
          </div>
          <span className="strength__label" style={{ color: meta.color }}>{meta.label}</span>
          <div className="strength__checks">
            {[
              ['length', '8+ characters'],
              ['upper', 'Uppercase letter'],
              ['lower', 'Lowercase letter'],
              ['number', 'Number'],
              ['special', 'Special character'],
            ].map(([k, text]) => (
              <span key={k} className={'strength__check' + (checks[k] ? ' strength__check--ok' : '')}>
                <CheckIcon ok={checks[k]}/> {text}
              </span>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

// -------------------- BRAND PANEL --------------------
function BrandPanel() {
  return (
    <div className="brand">
      <div className="brand__circle brand__circle--1"></div>
      <div className="brand__circle brand__circle--2"></div>
      <div className="brand__content">
        <ERPLogo size={64}/>
        <h2 className="brand__name">ERP System</h2>
        <p className="brand__tagline">Enterprise Resource Planning</p>
      </div>
    </div>
  );
}

// -------------------- REGISTER FORM --------------------
function RegisterForm({ onSuccess, onLogin }) {
  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', companyName: '',
    password: '', confirmPassword: '', agreeTerms: false,
  });
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = useCallback((name, val) => {
    const v = typeof val !== 'undefined' ? val : form[name];
    switch (name) {
      case 'firstName':
        if (!v.trim()) return 'First name is required';
        if (v.trim().length < 2) return 'Min 2 characters';
        return '';
      case 'lastName':
        if (!v.trim()) return 'Last name is required';
        if (v.trim().length < 2) return 'Min 2 characters';
        return '';
      case 'email':
        if (!v.trim()) return 'Email is required';
        if (!validateEmail(v)) return 'Enter a valid email';
        return '';
      case 'companyName':
        if (!v.trim()) return 'Company name is required';
        return '';
      case 'password':
        if (!v) return 'Password is required';
        if (v.length < 8) return 'Min 8 characters';
        return '';
      case 'confirmPassword':
        if (!v) return 'Please confirm password';
        if (v !== form.password) return 'Passwords do not match';
        return '';
      case 'agreeTerms':
        if (!v) return 'You must accept the terms';
        return '';
      default: return '';
    }
  }, [form]);

  const change = (name, val) => {
    setForm(prev => ({ ...prev, [name]: val }));
    if (touched[name]) setErrors(prev => ({ ...prev, [name]: validate(name, val) }));
    if (name === 'password' && touched.confirmPassword) {
      setErrors(prev => ({ ...prev, confirmPassword: val === form.confirmPassword ? '' : 'Passwords do not match' }));
    }
  };

  const blur = (name) => {
    setTouched(prev => ({ ...prev, [name]: true }));
    setErrors(prev => ({ ...prev, [name]: validate(name) }));
  };

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    const allT = Object.keys(form).reduce((a, k) => ({ ...a, [k]: true }), {});
    setTouched(allT);
    const allE = {};
    Object.keys(form).forEach(k => { const err = validate(k); if (err) allE[k] = err; });
    setErrors(allE);
    if (Object.values(allE).some(Boolean)) return;

    setSubmitting(true);
    try {
      const res = await fetch(ADMIN_BASE + '/api/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          firstName: form.firstName, lastName: form.lastName,
          email: form.email, companyName: form.companyName, password: form.password,
        }),
      });
      if (res.ok) {
        const data = await res.json();
        saveAuth(data);
        onSuccess({
          firstName:   form.firstName,
          lastName:    form.lastName,
          email:       data.email || form.email,
          companyName: data.companyName || form.companyName,
          role:        data.role || 'ADMIN',
        });
      } else {
        let msg = 'Registration failed. Please try again.';
        try { const j = await res.json(); msg = j.detail || j.message || msg; } catch {}
        if (res.status === 409) msg = 'An account with this email already exists.';
        setServerError(msg);
      }
    } catch (err) {
      setServerError('Cannot reach the server. Make sure the Admin service is running on port 8081.');
    }
    setSubmitting(false);
  };

  return (
    <form className="auth-form" onSubmit={submit} noValidate>
      <div className="form-logo"><ERPLogo size={42}/></div>
      <h1 className="form-title">Set up your company</h1>
      <p className="form-subtitle">Create your company and admin account to get started</p>

      {serverError && (
        <div className="form-error-banner"><AlertIcon/> {serverError}</div>
      )}

      <div className="form-row">
        <InputField label="First Name" value={form.firstName}
          onChange={v => change('firstName', v)} onBlur={() => blur('firstName')}
          error={touched.firstName && errors.firstName} placeholder="John" disabled={submitting}/>
        <InputField label="Last Name" value={form.lastName}
          onChange={v => change('lastName', v)} onBlur={() => blur('lastName')}
          error={touched.lastName && errors.lastName} placeholder="Doe" disabled={submitting}/>
      </div>

      <InputField label="Email Address" type="email" value={form.email}
        onChange={v => change('email', v)} onBlur={() => blur('email')}
        error={touched.email && errors.email} placeholder="john@company.com" disabled={submitting}/>

      <InputField label="Company Name" value={form.companyName}
        onChange={v => change('companyName', v)} onBlur={() => blur('companyName')}
        error={touched.companyName && errors.companyName} placeholder="Acme Corporation" disabled={submitting}/>

      <PasswordField label="Password" value={form.password}
        onChange={v => change('password', v)} onBlur={() => blur('password')}
        error={touched.password && errors.password} placeholder="Min. 8 characters"
        showStrength disabled={submitting}/>

      <PasswordField label="Confirm Password" value={form.confirmPassword}
        onChange={v => change('confirmPassword', v)} onBlur={() => blur('confirmPassword')}
        error={touched.confirmPassword && errors.confirmPassword}
        placeholder="Re-enter your password" disabled={submitting}/>

      <label className="field-checkbox">
        <input type="checkbox" checked={form.agreeTerms}
          onChange={e => change('agreeTerms', e.target.checked)} disabled={submitting}/>
        <span>I agree to the <a href="#" onClick={e => e.preventDefault()}>Terms of Service</a> and <a href="#" onClick={e => e.preventDefault()}>Privacy Policy</a></span>
      </label>
      {touched.agreeTerms && errors.agreeTerms && (
        <span className="field__error-msg" style={{ marginTop: -8 }}><AlertIcon/> {errors.agreeTerms}</span>
      )}

      <button type="submit" className={'btn btn--primary' + (submitting ? ' btn--loading' : '')}
        disabled={submitting}>
        {submitting ? (<><span className="btn__spinner"></span>Creating Company…</>) : 'Create Company & Admin Account'}
      </button>

      <p className="form-footer">
        Already have an account?{' '}
        <button type="button" className="form-link" onClick={onLogin}>Sign in</button>
      </p>
    </form>
  );
}

// -------------------- LOGIN FORM --------------------
function LoginForm({ onRegister }) {
  const [form, setForm] = useState({ email: '', password: '' });
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [serverError, setServerError] = useState('');

  const validate = (name) => {
    const v = form[name];
    if (name === 'email') {
      if (!v.trim()) return 'Email is required';
      if (!validateEmail(v)) return 'Enter a valid email';
    }
    if (name === 'password' && !v) return 'Password is required';
    return '';
  };

  const change = (name, val) => {
    setForm(prev => ({ ...prev, [name]: val }));
    if (touched[name]) setErrors(prev => ({ ...prev, [name]: validate(name) }));
  };

  const blur = (name) => {
    setTouched(prev => ({ ...prev, [name]: true }));
    setErrors(prev => ({ ...prev, [name]: validate(name) }));
  };

  const submit = async (e) => {
    e.preventDefault();
    setServerError('');
    const allT = { email: true, password: true };
    setTouched(allT);
    const allE = { email: validate('email'), password: validate('password') };
    setErrors(allE);
    if (Object.values(allE).some(Boolean)) return;

    setSubmitting(true);
    try {
      const res = await fetch(ADMIN_BASE + '/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: form.email, password: form.password }),
      });
      if (res.ok) {
        const data = await res.json();
        saveAuth(data);
        window.location.href = 'index.html';
      } else if (res.status === 401) {
        setServerError('Invalid credentials. Please check your email and password.');
      } else if (res.status === 423) {
        setServerError('Account locked due to too many failed attempts. Please contact support.');
      } else {
        setServerError('Login failed. Please try again.');
      }
    } catch {
      // Fallback for standalone UI mode (no backend) — redirect to dashboard
      await new Promise(r => setTimeout(r, 800));
      window.location.href = 'index.html';
    }
    setSubmitting(false);
  };

  return (
    <form className="auth-form" onSubmit={submit} noValidate>
      <div className="form-logo"><ERPLogo size={42}/></div>
      <p className="form-greeting">Welcome back!</p>
      <h1 className="form-title">Login to your account</h1>

      {serverError && (
        <div className="form-error-banner"><AlertIcon/> {serverError}</div>
      )}

      <InputField label="Email or username" value={form.email}
        onChange={v => change('email', v)} onBlur={() => blur('email')}
        error={touched.email && errors.email} placeholder="you@company.com" disabled={submitting}/>

      <PasswordField label="Password" value={form.password}
        onChange={v => change('password', v)} onBlur={() => blur('password')}
        error={touched.password && errors.password} placeholder="Enter password" disabled={submitting}/>

      <div className="form-forgot">
        <a href="#" onClick={e => e.preventDefault()}>Forgot Password?</a>
      </div>

      <button type="submit" className={'btn btn--primary' + (submitting ? ' btn--loading' : '')}
        disabled={submitting}>
        {submitting ? (<><span className="btn__spinner"></span>Logging in…</>) : 'Login'}
      </button>

      <p className="form-footer">
        Don't have an account?{' '}
        <button type="button" className="form-link" onClick={onRegister}>Sign Up</button>
      </p>
    </form>
  );
}

// -------------------- SUCCESS SCREEN --------------------
function SuccessScreen({ userData }) {
  const [countdown, setCountdown] = useState(3);

  useEffect(() => {
    if (countdown <= 0) { window.location.href = 'index.html'; return; }
    const t = setTimeout(() => setCountdown(c => c - 1), 1000);
    return () => clearTimeout(t);
  }, [countdown]);

  return (
    <div className="success">
      <SuccessCheckIcon/>
      <h1 className="success__title">Company Created!</h1>
      <p className="success__subtitle">
        Welcome, <strong>{userData?.firstName}</strong>! Your company
        <strong> {userData?.companyName}</strong> is set up and you're logged in as Admin.
      </p>
      <p className="success__email">{userData?.email}</p>
      <p className="success__role">Role: {userData?.role}</p>
      <button type="button" className="btn btn--primary"
        onClick={() => { window.location.href = 'index.html'; }}>
        Go to Dashboard ({countdown}s)
      </button>
    </div>
  );
}

// -------------------- MAIN APP --------------------
function AuthApp() {
  const [view, setView] = useState('login');
  const [userData, setUserData] = useState(null);
  const [fading, setFading] = useState(false);

  const switchTo = useCallback((v) => {
    setFading(true);
    setTimeout(() => { setView(v); setFading(false); }, 280);
  }, []);

  const onRegisterSuccess = useCallback((data) => {
    setUserData(data);
    switchTo('success');
  }, [switchTo]);

  return (
    <div className="auth-wrapper">
      <div className={'auth-card' + (fading ? ' auth-card--fade' : '')}>
        {(view === 'register' || view === 'login') && (
          <>
            <div className="auth-form-panel">
              {view === 'register'
                ? <RegisterForm onSuccess={onRegisterSuccess} onLogin={() => switchTo('login')}/>
                : <LoginForm onRegister={() => switchTo('register')}/>}
            </div>
            <div className="auth-brand-panel">
              <BrandPanel/>
            </div>
          </>
        )}
        {view === 'success' && (
          <>
            <div className="auth-form-panel">
              <SuccessScreen userData={userData}/>
            </div>
            <div className="auth-brand-panel">
              <BrandPanel/>
            </div>
          </>
        )}
      </div>
    </div>
  );
}

const root = ReactDOM.createRoot(document.getElementById('root'));
root.render(<AuthApp/>);
