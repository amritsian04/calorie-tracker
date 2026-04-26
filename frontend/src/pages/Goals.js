import { useState, useEffect, useCallback } from 'react';
import { apiFetch, toDateStr, formatDate } from '../api';
import './Goals.css';

export default function Goals() {
  const [date,    setDate]    = useState(toDateStr());
  const [form,    setForm]    = useState({ dailyCalorieGoal: '', proteinGoal: '', carbGoal: '', fatGoal: '' });
  const [current, setCurrent] = useState(null);
  const [loading, setLoading] = useState(false);
  const [saving,  setSaving]  = useState(false);
  const [error,   setError]   = useState('');
  const [success, setSuccess] = useState('');

  const loadGoal = useCallback(async () => {
    setLoading(true);
    setError('');
    setCurrent(null);
    try {
      const res = await apiFetch(`/api/goals?date=${date}`);
      if (res.ok) {
        const data = await res.json();
        setCurrent(data);
        setForm({
          dailyCalorieGoal: String(data.dailyCalorieGoal),
          proteinGoal:      String(data.proteinGoal),
          carbGoal:         String(data.carbGoal),
          fatGoal:          String(data.fatGoal),
        });
      } else {
        setForm({ dailyCalorieGoal: '', proteinGoal: '', carbGoal: '', fatGoal: '' });
      }
    } catch {
      setError('Could not load goal');
    } finally {
      setLoading(false);
    }
  }, [date]);

  useEffect(() => { loadGoal(); }, [loadGoal]);

  function handleChange(e) {
    setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSuccess('');

    const calories = parseInt(form.dailyCalorieGoal, 10);
    if (!calories || calories <= 0) {
      setError('Daily calorie goal must be a positive number');
      return;
    }

    setSaving(true);
    try {
      const payload = {
        goalDate:          date,
        dailyCalorieGoal:  calories,
        proteinGoal:       parseInt(form.proteinGoal, 10)  || 0,
        carbGoal:          parseInt(form.carbGoal,    10)  || 0,
        fatGoal:           parseInt(form.fatGoal,     10)  || 0,
      };

      const res = await apiFetch('/api/goals', { method: 'POST', body: payload });
      if (!res.ok) throw new Error((await res.json()).error || 'Failed to save goal');

      const saved = await res.json();
      setCurrent(saved);
      setSuccess(`Goal saved for ${formatDate(date)}!`);
      setTimeout(() => setSuccess(''), 3000);
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  }

  async function handleDelete() {
    if (!current) return;
    setSaving(true);
    try {
      await apiFetch(`/api/goals/${current.goalId}`, { method: 'DELETE' });
      setCurrent(null);
      setForm({ dailyCalorieGoal: '', proteinGoal: '', carbGoal: '', fatGoal: '' });
      setSuccess('Goal removed');
      setTimeout(() => setSuccess(''), 2500);
    } catch {
      setError('Failed to delete goal');
    } finally {
      setSaving(false);
    }
  }

  // Simple macro ratio hint
  const cal = parseInt(form.dailyCalorieGoal, 10) || 0;
  const proteinCal = (parseInt(form.proteinGoal, 10) || 0) * 4;
  const carbCal    = (parseInt(form.carbGoal,    10) || 0) * 4;
  const fatCal     = (parseInt(form.fatGoal,     10) || 0) * 9;
  const macroCal   = proteinCal + carbCal + fatCal;

  return (
    <div className="page">
      <h1 className="goals-title">Daily Goals</h1>
      <p className="goals-subtitle">Set your calorie and macro targets for any date.</p>

      <div className="card goals-card">
        {/* Date selector */}
        <div className="form-group goals-date-group">
          <label>Date</label>
          <input
            type="date"
            className="form-control"
            style={{ maxWidth: '200px' }}
            value={date}
            onChange={e => setDate(e.target.value)}
          />
        </div>

        {loading && <div className="loading-screen" style={{ height: 80 }}><div className="spinner" /></div>}

        {!loading && (
          <>
            {current && (
              <div className="goal-existing-banner">
                ✅ A goal is set for {formatDate(date)} — editing it below.
              </div>
            )}
            {!current && (
              <div className="goal-empty-banner">
                No goal set for {formatDate(date)} yet.
              </div>
            )}

            {error   && <div className="error-msg">{error}</div>}
            {success && <div className="success-msg">{success}</div>}

            <form onSubmit={handleSubmit}>
              {/* Calories */}
              <div className="form-group">
                <label htmlFor="dailyCalorieGoal">
                  Daily Calorie Goal <span className="label-required">*</span>
                </label>
                <div className="input-with-unit">
                  <input
                    id="dailyCalorieGoal"
                    name="dailyCalorieGoal"
                    type="number"
                    className="form-control"
                    value={form.dailyCalorieGoal}
                    onChange={handleChange}
                    placeholder="e.g. 2000"
                    min="1"
                    required
                  />
                  <span className="input-unit">kcal</span>
                </div>
              </div>

              {/* Macros */}
              <p className="macros-heading">Macro Goals <span className="optional">(optional)</span></p>
              <div className="macros-grid">
                <div className="form-group">
                  <label htmlFor="proteinGoal">Protein</label>
                  <div className="input-with-unit">
                    <input
                      id="proteinGoal"
                      name="proteinGoal"
                      type="number"
                      className="form-control"
                      value={form.proteinGoal}
                      onChange={handleChange}
                      placeholder="150"
                      min="0"
                    />
                    <span className="input-unit">g</span>
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="carbGoal">Carbs</label>
                  <div className="input-with-unit">
                    <input
                      id="carbGoal"
                      name="carbGoal"
                      type="number"
                      className="form-control"
                      value={form.carbGoal}
                      onChange={handleChange}
                      placeholder="200"
                      min="0"
                    />
                    <span className="input-unit">g</span>
                  </div>
                </div>
                <div className="form-group">
                  <label htmlFor="fatGoal">Fat</label>
                  <div className="input-with-unit">
                    <input
                      id="fatGoal"
                      name="fatGoal"
                      type="number"
                      className="form-control"
                      value={form.fatGoal}
                      onChange={handleChange}
                      placeholder="65"
                      min="0"
                    />
                    <span className="input-unit">g</span>
                  </div>
                </div>
              </div>

              {/* Macro calorie hint */}
              {macroCal > 0 && cal > 0 && (
                <p className="macro-hint">
                  Macros account for <strong>{macroCal}</strong> of <strong>{cal}</strong> kcal
                  {macroCal > cal && <span className="macro-hint-warn"> — over calorie goal</span>}
                </p>
              )}

              <div className="goals-actions">
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : current ? 'Update Goal' : 'Set Goal'}
                </button>
                {current && (
                  <button
                    type="button"
                    className="btn btn-danger"
                    onClick={handleDelete}
                    disabled={saving}
                  >
                    Remove Goal
                  </button>
                )}
              </div>
            </form>
          </>
        )}
      </div>

      {/* Quick reference */}
      <div className="card goals-reference">
        <h3 className="ref-title">Common Calorie Targets</h3>
        <div className="ref-grid">
          {[
            { label: 'Weight Loss',     cal: 1500, p: 130, c: 150, f: 50  },
            { label: 'Maintenance',     cal: 2000, p: 150, c: 225, f: 65  },
            { label: 'Muscle Gain',     cal: 2500, p: 180, c: 300, f: 80  },
            { label: 'Active Athlete',  cal: 3000, p: 200, c: 380, f: 90  },
          ].map(preset => (
            <button
              key={preset.label}
              type="button"
              className="preset-btn"
              onClick={() => setForm({
                dailyCalorieGoal: String(preset.cal),
                proteinGoal:      String(preset.p),
                carbGoal:         String(preset.c),
                fatGoal:          String(preset.f),
              })}
            >
              <span className="preset-name">{preset.label}</span>
              <span className="preset-cal">{preset.cal} kcal</span>
              <span className="preset-macros">P:{preset.p}g C:{preset.c}g F:{preset.f}g</span>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
}
