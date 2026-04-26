import { useState, useEffect, useCallback } from 'react';
import { apiFetch, toDateStr, formatDate } from '../api';
import './WeeklySummary.css';

function round1(n) { return Math.round((n || 0) * 10) / 10; }

function addDays(dateStr, days) {
  const d = new Date(dateStr + 'T12:00:00');
  d.setDate(d.getDate() + days);
  return toDateStr(d);
}

export default function WeeklySummary() {
  const [endDate,  setEndDate]  = useState(toDateStr());
  const [data,     setData]     = useState([]);
  const [goals,    setGoals]    = useState({});
  const [loading,  setLoading]  = useState(false);
  const [error,    setError]    = useState('');

  const startDate = addDays(endDate, -6);

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      const [weekRes, goalsRes] = await Promise.all([
        apiFetch(`/api/summary/weekly?start=${startDate}&end=${endDate}`),
        apiFetch(`/api/goals/all`),
      ]);

      const weekData  = weekRes.ok  ? await weekRes.json()  : [];
      const goalsData = goalsRes.ok ? await goalsRes.json() : [];

      // Build a lookup of goal by date
      const goalMap = {};
      for (const g of goalsData) goalMap[g.goalDate] = g;

      setData(weekData);
      setGoals(goalMap);
    } catch {
      setError('Failed to load weekly data');
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate]);

  useEffect(() => { load(); }, [load]);

  // Fill in missing days as zeros so the chart always shows 7 bars
  const allDays = Array.from({ length: 7 }, (_, i) => addDays(startDate, i));
  const dayMap  = Object.fromEntries(data.map(d => [d.mealDate, d]));
  const rows    = allDays.map(date => ({ date, ...(dayMap[date] || {}) }));

  const maxCal = Math.max(...rows.map(r => round1(r.totalCalories ?? 0)), 1);

  const totals = {
    calories: round1(data.reduce((s, d) => s + (d.totalCalories ?? 0), 0)),
    protein:  round1(data.reduce((s, d) => s + (d.totalProtein  ?? 0), 0)),
    carbs:    round1(data.reduce((s, d) => s + (d.totalCarbs    ?? 0), 0)),
    fat:      round1(data.reduce((s, d) => s + (d.totalFat      ?? 0), 0)),
    days:     data.length,
  };
  const avgCal = totals.days > 0 ? round1(totals.calories / 7) : 0;

  return (
    <div className="page">
      <div className="weekly-header">
        <div>
          <h1 className="weekly-title">Weekly Summary</h1>
          <p className="weekly-range">
            {formatDate(startDate)} — {formatDate(endDate)}
          </p>
        </div>
        <div className="form-group" style={{ margin: 0 }}>
          <label style={{ fontSize: '12px', color: '#6b8c6b' }}>Week ending</label>
          <input
            type="date"
            className="form-control"
            value={endDate}
            onChange={e => setEndDate(e.target.value)}
            style={{ maxWidth: '160px' }}
          />
        </div>
      </div>

      {error && <div className="error-msg">{error}</div>}

      {loading ? (
        <div className="loading-screen"><div className="spinner" /></div>
      ) : (
        <>
          {/* Summary cards */}
          <div className="weekly-stats">
            <div className="card stat-card">
              <p className="stat-value">{totals.calories.toLocaleString()}</p>
              <p className="stat-label">Total kcal (7 days)</p>
            </div>
            <div className="card stat-card">
              <p className="stat-value">{avgCal.toLocaleString()}</p>
              <p className="stat-label">Daily avg kcal</p>
            </div>
            <div className="card stat-card">
              <p className="stat-value">{totals.protein}g</p>
              <p className="stat-label">Total protein</p>
            </div>
            <div className="card stat-card">
              <p className="stat-value">{totals.days}</p>
              <p className="stat-label">Days with meals logged</p>
            </div>
          </div>

          {/* Bar chart */}
          <div className="card chart-card">
            <h2 className="chart-title">Daily Calories</h2>
            <div className="bar-chart">
              {rows.map(row => {
                const cal     = round1(row.totalCalories ?? 0);
                const goal    = goals[row.date]?.dailyCalorieGoal ?? 0;
                const pctBar  = (cal / maxCal) * 100;
                const isOver  = goal > 0 && cal > goal;
                const noData  = cal === 0;

                return (
                  <div key={row.date} className="bar-col">
                    <div className="bar-wrapper" title={`${cal} kcal`}>
                      {goal > 0 && (
                        <div
                          className="bar-goal-line"
                          style={{ bottom: `${(goal / maxCal) * 100}%` }}
                          title={`Goal: ${goal} kcal`}
                        />
                      )}
                      <div
                        className={`bar${noData ? ' bar-empty' : isOver ? ' bar-over' : ''}`}
                        style={{ height: noData ? '4px' : `${pctBar}%` }}
                      />
                    </div>
                    <p className="bar-cal">{noData ? '—' : cal.toLocaleString()}</p>
                    <p className="bar-label">{formatDate(row.date).split(',')[0]}</p>
                  </div>
                );
              })}
            </div>
            <p className="chart-legend">
              <span className="legend-dot green" /> Calories consumed
              <span className="legend-dot red"   /> Over goal
              <span className="legend-line"      /> Daily goal
            </p>
          </div>

          {/* Detailed table */}
          <div className="card table-card">
            <h2 className="chart-title">Day-by-Day Breakdown</h2>
            <div className="table-scroll">
              <table className="weekly-table">
                <thead>
                  <tr>
                    <th>Date</th>
                    <th>Calories</th>
                    <th>Goal</th>
                    <th>Difference</th>
                    <th>Protein</th>
                    <th>Carbs</th>
                    <th>Fat</th>
                    <th>Meals</th>
                  </tr>
                </thead>
                <tbody>
                  {rows.map(row => {
                    const cal  = round1(row.totalCalories ?? 0);
                    const goal = goals[row.date]?.dailyCalorieGoal ?? null;
                    const diff = goal ? round1(cal - goal) : null;

                    return (
                      <tr key={row.date} className={cal === 0 ? 'row-empty' : ''}>
                        <td className="date-cell">{formatDate(row.date)}</td>
                        <td className="num-cell">{cal > 0 ? cal.toLocaleString() : '—'}</td>
                        <td className="num-cell">{goal ? goal.toLocaleString() : '—'}</td>
                        <td className={`num-cell diff-cell${diff !== null ? (diff > 0 ? ' over' : diff < 0 ? ' under' : '') : ''}`}>
                          {diff !== null
                            ? (diff > 0 ? `+${diff}` : diff === 0 ? '✓' : diff)
                            : '—'}
                        </td>
                        <td className="num-cell">{round1(row.totalProtein) > 0 ? `${round1(row.totalProtein)}g` : '—'}</td>
                        <td className="num-cell">{round1(row.totalCarbs)   > 0 ? `${round1(row.totalCarbs)}g`   : '—'}</td>
                        <td className="num-cell">{round1(row.totalFat)     > 0 ? `${round1(row.totalFat)}g`     : '—'}</td>
                        <td className="num-cell">{row.mealCount ?? '—'}</td>
                      </tr>
                    );
                  })}
                </tbody>
                <tfoot>
                  <tr className="totals-row">
                    <td><strong>7-Day Total</strong></td>
                    <td className="num-cell"><strong>{totals.calories.toLocaleString()}</strong></td>
                    <td className="num-cell">—</td>
                    <td className="num-cell">—</td>
                    <td className="num-cell"><strong>{totals.protein}g</strong></td>
                    <td className="num-cell"><strong>{totals.carbs}g</strong></td>
                    <td className="num-cell"><strong>{totals.fat}g</strong></td>
                    <td className="num-cell">—</td>
                  </tr>
                </tfoot>
              </table>
            </div>
          </div>
        </>
      )}
    </div>
  );
}
