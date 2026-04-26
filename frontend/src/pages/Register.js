import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Register() {
  const { register, user } = useAuth();
  const navigate = useNavigate();

  const [form, setForm] = useState({
    firstName: '', lastName: '', email: '', password: '', confirm: '',
  });
  const [error,   setError]   = useState('');
  const [loading, setLoading] = useState(false);

  if (user) {
    navigate('/dashboard', { replace: true });
    return null;
  }

  function handleChange(e) {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');

    if (form.password !== form.confirm) {
      setError('Passwords do not match');
      return;
    }
    if (form.password.length < 6) {
      setError('Password must be at least 6 characters');
      return;
    }

    setLoading(true);
    try {
      await register(form.firstName, form.lastName, form.email, form.password);
      navigate('/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="auth-wrapper">
      <div className="auth-card">
        <div className="auth-logo">🥗</div>
        <h1 className="auth-title">Create account</h1>
        <p className="auth-subtitle">Start tracking your nutrition today</p>

        {error && <div className="error-msg">{error}</div>}

        <form onSubmit={handleSubmit}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="firstName">First name</label>
              <input
                id="firstName"
                name="firstName"
                type="text"
                className="form-control"
                value={form.firstName}
                onChange={handleChange}
                required
                autoFocus
              />
            </div>
            <div className="form-group" style={{ marginBottom: 0 }}>
              <label htmlFor="lastName">Last name</label>
              <input
                id="lastName"
                name="lastName"
                type="text"
                className="form-control"
                value={form.lastName}
                onChange={handleChange}
                required
              />
            </div>
          </div>

          <div className="form-group" style={{ marginTop: '16px' }}>
            <label htmlFor="email">Email</label>
            <input
              id="email"
              name="email"
              type="email"
              className="form-control"
              value={form.email}
              onChange={handleChange}
              placeholder="you@example.com"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <input
              id="password"
              name="password"
              type="password"
              className="form-control"
              value={form.password}
              onChange={handleChange}
              placeholder="Min. 6 characters"
              required
            />
          </div>

          <div className="form-group">
            <label htmlFor="confirm">Confirm password</label>
            <input
              id="confirm"
              name="confirm"
              type="password"
              className="form-control"
              value={form.confirm}
              onChange={handleChange}
              required
            />
          </div>

          <button
            type="submit"
            className="btn btn-primary btn-block"
            disabled={loading}
          >
            {loading ? 'Creating account…' : 'Create Account'}
          </button>
        </form>

        <p className="auth-footer">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  );
}
