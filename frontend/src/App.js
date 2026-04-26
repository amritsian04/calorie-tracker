import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/ProtectedRoute';
import Navbar from './components/Navbar';
import Login from './pages/Login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import LogMeal from './pages/LogMeal';
import Goals from './pages/Goals';
import WeeklySummary from './pages/WeeklySummary';
import './App.css';

function AppLayout({ children }) {
  return (
    <>
      <Navbar />
      <main>{children}</main>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          {/* Public routes */}
          <Route path="/login"    element={<Login />} />
          <Route path="/register" element={<Register />} />

          {/* Protected routes — all share the Navbar */}
          <Route path="/dashboard" element={
            <ProtectedRoute>
              <AppLayout><Dashboard /></AppLayout>
            </ProtectedRoute>
          } />
          <Route path="/log-meal" element={
            <ProtectedRoute>
              <AppLayout><LogMeal /></AppLayout>
            </ProtectedRoute>
          } />
          <Route path="/goals" element={
            <ProtectedRoute>
              <AppLayout><Goals /></AppLayout>
            </ProtectedRoute>
          } />
          <Route path="/weekly-summary" element={
            <ProtectedRoute>
              <AppLayout><WeeklySummary /></AppLayout>
            </ProtectedRoute>
          } />

          {/* Default redirect */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
}
