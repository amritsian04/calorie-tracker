import { useState, useEffect, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiFetch, toDateStr } from '../api';
import './Dashboard.css';

const MEAL_ORDER = ['breakfast', 'lunch', 'dinner', 'snack'];
const MEAL_EMOJI = { breakfast: '🌅', lunch: '☀️', dinner: '🌙', snack: '🍎' };

function round1(n) {
  return Math.round((n || 0) * 10) / 10;
}

export default function Dashboard() {
  const { user } = useAuth();
  const today = toDateStr();

  const [summary, setSummary] = useState(null);
  const [goal,    setGoal]    = useState(null);
  const [meals,   setMeals]   = useState([]);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState('');

  const load = useCallback(async () => {
    setLoading(true);
    setError('');
    try {
      // Fetch daily summary, goal, and meals in parallel
      const [summaryRes, goalRes, mealsRes] = await Promise.all([
        apiFetch(`/api/summary/daily?date=${today}`),
        apiFetch(`/api/goals?date=${today}`),
        apiFetch(`/api/meals?date=${today}`),
      ]);

      const summaryData = summaryRes.ok ? await summaryRes.json() : null;
      const goalData    = goalRes.ok    ? await goalRes.json()    : null;
      const mealsData   = mealsRes.ok   ? await mealsRes.json()  : [];

      // Fetch entries for every meal in parallel
      const mealsWithEntries = await Promise.all(
        mealsData.map(async meal => {
          const entriesRes = await apiFetch(`/api/meals/${meal.mealId}/entries`);
          const entries    = entriesRes.ok ? await entriesRes.json() : [];
          return { ...meal, entries };
        })
      );

      setSummary(summaryData);
      setGoal(goalData);
      setMeals(mealsWithEntries);
    } catch {
      setError('Failed to load data. Make sure the backend is running.');
    } finally {
      setLoading(false);
    }
  }, [today]);

  useEffect(() => { load(); }, [load]);

  async function handleDeleteEntry(mealId, entryId) {
    await apiFetch(`/api/meals/${mealId}/entries/${entryId}`, { method: 'DELETE' });
    load();
  }

  async function handleDeleteMeal(mealId) {
    await apiFetch(`/api/meals/${mealId}`, { method: 'DELETE' });
    load();
  }

  if (loading) {
    return <div className="loading-screen"><div className="spinner" /></div>;
  }

  const consumed  = round1(summary?.totalCalories ?? 0);
  const target    = goal?.dailyCalorieGoal ?? 0;
  const pct       = target > 0 ? Math.min((consumed / target) * 100, 100) : 0;
  const isOver    = target > 0 && consumed > target;

  // Group meals by type
  const byType = {};
  for (const meal of meals) {
    const type = meal.mealType;
    if (!byType[type]) byType[type] = [];
    byType[type].push(meal);
  }

  const displayDate = new Date(today + 'T12:00:00').toLocaleDateString('en-US', {
    weekday: 'long', month: 'long', day: 'numeric',
  });

  return (
    <div className="page">
      {/* Header */}
      <div className="dashboard-header">
        <div>
          <h1 className="dashboard-greeting">Hello, {user.firstName} 👋</h1>
          <p className="dashboard-date">{displayDate}</p>
        </div>
        <Link to="/log-meal" className="btn btn-primary">+ Log a Meal</Link>
      </div>

      {error && <div className="error-msg">{error}</div>}

      {/* Calorie Summary Card */}
      <div className="card calorie-card">
        <div className="calorie-header">
          <span className="calorie-label">Calories Today</span>
          {!goal && (
            <Link to="/goals" className="set-goal-link">Set a goal →</Link>
          )}
        </div>
        <div className="calorie-numbers">
          <span className="calorie-consumed">{consumed.toLocaleString()}</span>
          {target > 0 && (
            <span className="calorie-target"> / {target.toLocaleString()} kcal</span>
          )}
          {!target && <span className="calorie-target"> kcal</span>}
        </div>
        {target > 0 && (
          <>
            <div className="progress-track" style={{ marginTop: '12px' }}>
              <div
                className={`progress-fill${isOver ? ' over' : ''}`}
                style={{ width: `${pct}%` }}
              />
            </div>
            <p className="calorie-status">
              {isOver
                ? `${round1(consumed - target)} kcal over goal`
                : `${round1(target - consumed)} kcal remaining`}
            </p>
          </>
        )}

        {/* Macros */}
        {summary && (
          <div className="macro-grid" style={{ marginTop: '20px' }}>
            <div className="macro-pill">
              <div className="macro-value">{round1(summary.totalProtein)}g</div>
              <div className="macro-label">
                Protein
                {goal?.proteinGoal > 0 && <span className="macro-goal"> / {goal.proteinGoal}g</span>}
              </div>
            </div>
            <div className="macro-pill">
              <div className="macro-value">{round1(summary.totalCarbs)}g</div>
              <div className="macro-label">
                Carbs
                {goal?.carbGoal > 0 && <span className="macro-goal"> / {goal.carbGoal}g</span>}
              </div>
            </div>
            <div className="macro-pill">
              <div className="macro-value">{round1(summary.totalFat)}g</div>
              <div className="macro-label">
                Fat
                {goal?.fatGoal > 0 && <span className="macro-goal"> / {goal.fatGoal}g</span>}
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Meal Breakdown */}
      <h2 className="section-title">Today's Meals</h2>

      {meals.length === 0 ? (
        <div className="card empty-state">
          <p>No meals logged yet.</p>
          <Link to="/log-meal" className="btn btn-primary" style={{ marginTop: '12px' }}>
            Log your first meal
          </Link>
        </div>
      ) : (
        <div className="meal-sections">
          {MEAL_ORDER.map(type => {
            const group = byType[type];
            if (!group) return null;
            return (
              <div key={type} className="card meal-section">
                <h3 className="meal-type-heading">
                  {MEAL_EMOJI[type]} {type.charAt(0).toUpperCase() + type.slice(1)}
                </h3>
                {group.map(meal => (
                  <div key={meal.mealId} className="meal-block">
                    {meal.mealName && (
                      <p className="meal-name">{meal.mealName}</p>
                    )}
                    {meal.entries.length === 0 ? (
                      <p className="no-entries">No foods logged</p>
                    ) : (
                      <table className="entries-table">
                        <thead>
                          <tr>
                            <th>Food</th>
                            <th>Qty</th>
                            <th>Cal</th>
                            <th>P</th>
                            <th>C</th>
                            <th>F</th>
                            <th></th>
                          </tr>
                        </thead>
                        <tbody>
                          {meal.entries.map(entry => {
                            const factor = entry.quantity / entry.servingSize;
                            return (
                              <tr key={entry.mealEntryId}>
                                <td className="food-name-cell">{entry.foodName}</td>
                                <td>{entry.quantity}g</td>
                                <td>{round1(entry.calories * factor)}</td>
                                <td>{round1(entry.protein * factor)}g</td>
                                <td>{round1(entry.carbs * factor)}g</td>
                                <td>{round1(entry.fat * factor)}g</td>
                                <td>
                                  <button
                                    className="btn btn-danger btn-sm"
                                    onClick={() => handleDeleteEntry(meal.mealId, entry.mealEntryId)}
                                    title="Remove"
                                  >
                                    ✕
                                  </button>
                                </td>
                              </tr>
                            );
                          })}
                        </tbody>
                      </table>
                    )}
                    <button
                      className="btn btn-danger btn-sm delete-meal-btn"
                      onClick={() => handleDeleteMeal(meal.mealId)}
                    >
                      Delete meal
                    </button>
                  </div>
                ))}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
