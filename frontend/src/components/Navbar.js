import { NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './Navbar.css';

export default function Navbar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  async function handleLogout() {
    await logout();
    navigate('/login');
  }

  if (!user) return null;

  return (
    <nav className="navbar">
      <div className="navbar-brand">
        <span className="navbar-logo">🥗</span>
        <span className="navbar-title">CalorieTracker</span>
      </div>
      <div className="navbar-links">
        <NavLink to="/dashboard"       className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Dashboard</NavLink>
        <NavLink to="/log-meal"        className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Log Meal</NavLink>
        <NavLink to="/goals"           className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Goals</NavLink>
        <NavLink to="/weekly-summary"  className={({ isActive }) => isActive ? 'nav-link active' : 'nav-link'}>Weekly</NavLink>
      </div>
      <div className="navbar-user">
        <span className="navbar-username">Hi, {user.firstName}</span>
        <button className="btn-logout" onClick={handleLogout}>Logout</button>
      </div>
    </nav>
  );
}
