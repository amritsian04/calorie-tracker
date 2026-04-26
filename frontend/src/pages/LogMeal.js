import { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { apiFetch, toDateStr, formatDate } from '../api';
import './LogMeal.css';

const MEAL_TYPES = ['breakfast', 'lunch', 'dinner', 'snack'];

function round1(n) { return Math.round((n || 0) * 10) / 10; }

export default function LogMeal() {
  const [date,      setDate]      = useState(toDateStr());
  const [mealType,  setMealType]  = useState('breakfast');
  const [mealId,    setMealId]    = useState(null);  // active meal being built
  const [logged,    setLogged]    = useState([]);    // entries added this session

  const [query,     setQuery]     = useState('');
  const [results,   setResults]   = useState([]);
  const [searching, setSearching] = useState(false);

  const [selected,  setSelected]  = useState(null);  // chosen food
  const [quantity,  setQuantity]  = useState('');

  const [status,    setStatus]    = useState('');
  const [error,     setError]     = useState('');
  const [submitting, setSubmitting] = useState(false);

  const debounceRef = useRef(null);

  // Reset active meal when date or type changes
  useEffect(() => {
    setMealId(null);
    setLogged([]);
    setSelected(null);
    setQuantity('');
  }, [date, mealType]);

  // Debounced food search
  useEffect(() => {
    if (!query.trim()) { setResults([]); return; }
    clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(async () => {
      setSearching(true);
      try {
        const res = await apiFetch(`/api/foods/search?q=${encodeURIComponent(query.trim())}`);
        if (res.ok) setResults(await res.json());
      } finally {
        setSearching(false);
      }
    }, 300);
    return () => clearTimeout(debounceRef.current);
  }, [query]);

  function pickFood(food) {
    setSelected(food);
    setQuery(food.foodName);
    setResults([]);
    setQuantity(String(food.servingSize));
  }

  async function getOrCreateMeal() {
    if (mealId) return mealId;

    // Check if a meal of this type already exists for the selected date
    const mealsRes = await apiFetch(`/api/meals?date=${date}`);
    if (mealsRes.ok) {
      const existing = await mealsRes.json();
      const match = existing.find(m => m.mealType === mealType);
      if (match) {
        setMealId(match.mealId);
        return match.mealId;
      }
    }

    // Create a new meal
    const res = await apiFetch('/api/meals', {
      method: 'POST',
      body: { mealType, mealDate: date },
    });
    if (!res.ok) throw new Error('Failed to create meal');
    const meal = await res.json();
    setMealId(meal.mealId);
    return meal.mealId;
  }

  async function handleAddFood(e) {
    e.preventDefault();
    if (!selected) { setError('Please select a food from the search results'); return; }
    const qty = parseFloat(quantity);
    if (!qty || qty <= 0) { setError('Enter a valid quantity'); return; }

    setError('');
    setSubmitting(true);
    try {
      const id = await getOrCreateMeal();
      const res = await apiFetch(`/api/meals/${id}/entries`, {
        method: 'POST',
        body: { foodId: selected.foodId, quantity: qty },
      });
      if (!res.ok) throw new Error('Failed to add food');
      const entry = await res.json();

      const factor = qty / selected.servingSize;
      setLogged(prev => [...prev, {
        entryId:   entry.mealEntryId,
        foodName:  selected.foodName,
        quantity:  qty,
        unit:      'g',
        calories:  round1(selected.calories * factor),
        protein:   round1(selected.protein  * factor),
        carbs:     round1(selected.carbs    * factor),
        fat:       round1(selected.fat      * factor),
      }]);

      setStatus(`Added ${selected.foodName}!`);
      setTimeout(() => setStatus(''), 2500);

      // Clear food selection for next entry
      setSelected(null);
      setQuery('');
      setQuantity('');
    } catch (err) {
      setError(err.message);
    } finally {
      setSubmitting(false);
    }
  }

  async function removeEntry(entryId) {
    if (!mealId) return;
    await apiFetch(`/api/meals/${mealId}/entries/${entryId}`, { method: 'DELETE' });
    setLogged(prev => prev.filter(e => e.entryId !== entryId));
  }

  const totalCal = logged.reduce((s, e) => s + e.calories, 0);

  return (
    <div className="page">
      <div className="log-header">
        <h1>Log a Meal</h1>
        <Link to="/dashboard" className="btn btn-secondary btn-sm">← Back to Dashboard</Link>
      </div>

      {/* Meal type + date selector */}
      <div className="card log-meta-card">
        <div className="log-meta-row">
          <div className="form-group" style={{ margin: 0 }}>
            <label>Date</label>
            <input
              type="date"
              className="form-control"
              value={date}
              onChange={e => setDate(e.target.value)}
            />
          </div>
          <div className="form-group" style={{ margin: 0 }}>
            <label>Meal type</label>
            <select
              className="form-control"
              value={mealType}
              onChange={e => setMealType(e.target.value)}
            >
              {MEAL_TYPES.map(t => (
                <option key={t} value={t}>
                  {t.charAt(0).toUpperCase() + t.slice(1)}
                </option>
              ))}
            </select>
          </div>
        </div>
        <p className="log-meta-hint">
          Logging for <strong>{formatDate(date)}</strong> — <strong>{mealType}</strong>
        </p>
      </div>

      {/* Food search + add form */}
      <div className="card log-add-card">
        <h2 className="log-section-title">Add a Food</h2>

        {error  && <div className="error-msg">{error}</div>}
        {status && <div className="success-msg">{status}</div>}

        <form onSubmit={handleAddFood}>
          <div className="search-wrapper">
            <div className="form-group">
              <label>Search food</label>
              <input
                type="text"
                className="form-control"
                value={query}
                onChange={e => { setQuery(e.target.value); setSelected(null); }}
                placeholder="e.g. Chicken Breast, Banana…"
                autoComplete="off"
              />
            </div>

            {/* Search results dropdown */}
            {results.length > 0 && (
              <ul className="search-dropdown">
                {results.map(food => (
                  <li
                    key={food.foodId}
                    className="search-item"
                    onClick={() => pickFood(food)}
                  >
                    <span className="search-food-name">{food.foodName}</span>
                    <span className="search-food-meta">
                      {food.category} · {food.calories} kcal / {food.servingSize}g
                    </span>
                  </li>
                ))}
              </ul>
            )}
            {searching && <p className="search-hint">Searching…</p>}
            {!searching && query.trim() && results.length === 0 && !selected && (
              <p className="search-hint">No results for "{query}"</p>
            )}
          </div>

          {/* Selected food nutrition preview */}
          {selected && (
            <div className="food-preview">
              <p className="food-preview-name">{selected.foodName}</p>
              <div className="food-preview-macros">
                <span>{selected.calories} kcal</span>
                <span>P: {selected.protein}g</span>
                <span>C: {selected.carbs}g</span>
                <span>F: {selected.fat}g</span>
                <span className="preview-serving">per {selected.servingSize}g serving</span>
              </div>
            </div>
          )}

          <div className="quantity-row">
            <div className="form-group" style={{ margin: 0, flex: 1 }}>
              <label>Quantity (grams)</label>
              <input
                type="number"
                className="form-control"
                value={quantity}
                onChange={e => setQuantity(e.target.value)}
                min="0.1"
                step="0.1"
                placeholder="100"
              />
            </div>
            <button
              type="submit"
              className="btn btn-primary add-btn"
              disabled={submitting || !selected}
            >
              {submitting ? 'Adding…' : '+ Add Food'}
            </button>
          </div>
        </form>
      </div>

      {/* Category quick search */}
      <div className="card">
        <h2 className="log-section-title">Browse by Category</h2>
        <CategoryBrowser onPick={pickFood} />
      </div>

      {/* Logged entries this session */}
      {logged.length > 0 && (
        <div className="card logged-card">
          <h2 className="log-section-title">
            Added to {mealType.charAt(0).toUpperCase() + mealType.slice(1)}
            <span className="total-cal-badge">{round1(totalCal)} kcal total</span>
          </h2>
          <table className="entries-log-table">
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
              {logged.map(entry => (
                <tr key={entry.entryId}>
                  <td className="food-name-cell">{entry.foodName}</td>
                  <td>{entry.quantity}g</td>
                  <td>{entry.calories}</td>
                  <td>{entry.protein}g</td>
                  <td>{entry.carbs}g</td>
                  <td>{entry.fat}g</td>
                  <td>
                    <button
                      className="btn btn-danger btn-sm"
                      onClick={() => removeEntry(entry.entryId)}
                    >
                      ✕
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div style={{ marginTop: 16 }}>
            <Link to="/dashboard" className="btn btn-primary">View Dashboard</Link>
          </div>
        </div>
      )}
    </div>
  );
}

function CategoryBrowser({ onPick }) {
  const CATEGORIES = ['Protein', 'Grain', 'Vegetable', 'Fruit', 'Dairy', 'Nuts'];
  const [active,  setActive]  = useState(null);
  const [results, setResults] = useState([]);

  async function fetchCategory(cat) {
    if (active === cat) { setActive(null); setResults([]); return; }
    setActive(cat);
    const res = await apiFetch(`/api/foods/search?category=${encodeURIComponent(cat)}`);
    if (res.ok) setResults(await res.json());
  }

  return (
    <div>
      <div className="category-chips">
        {CATEGORIES.map(cat => (
          <button
            key={cat}
            className={`chip${active === cat ? ' chip-active' : ''}`}
            onClick={() => fetchCategory(cat)}
            type="button"
          >
            {cat}
          </button>
        ))}
      </div>
      {results.length > 0 && active && (
        <ul className="category-results">
          {results.map(food => (
            <li
              key={food.foodId}
              className="search-item"
              onClick={() => onPick(food)}
            >
              <span className="search-food-name">{food.foodName}</span>
              <span className="search-food-meta">
                {food.calories} kcal · P:{food.protein}g · C:{food.carbs}g · F:{food.fat}g
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
