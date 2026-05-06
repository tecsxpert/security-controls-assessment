import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider, useAuth } from "./context/AuthContext";
import React from "react";

// Pages
import LoginPage      from "./pages/LoginPage";
import DashboardPage  from "./pages/DashboardPage";
import ListPage       from "./pages/ListPage";
import DetailPage     from "./pages/DetailPage";
import FormPage       from "./pages/FormPage";
import AnalyticsPage  from "./pages/AnalyticsPage";

// Error Boundary — catches JS errors in any child component
class ErrorBoundary extends React.Component {
  constructor(props) {
    super(props);
    this.state = { hasError: false };
  }
  static getDerivedStateFromError() {
    return { hasError: true };
  }
  componentDidCatch(error, info) {
    console.error("Error caught by boundary:", error, info);
  }
  render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
          <div className="text-center p-8">
            <div className="text-6xl mb-4">⚠️</div>
            <h1 className="text-2xl font-bold text-gray-800 mb-2">Something went wrong</h1>
            <p className="text-gray-500 mb-6">Please refresh the page to continue.</p>
            <button
              onClick={() => window.location.reload()}
              className="bg-primary text-white px-6 py-3 rounded-lg hover:bg-primary-dark min-h-touch"
              style={{ backgroundColor: "#1B4F8A" }}
            >
              Refresh Page
            </button>
          </div>
        </div>
      );
    }
    return this.props.children;
  }
}

// Protected Route — redirects to login if not authenticated
function ProtectedRoute({ children }) {
  const { isAuthenticated } = useAuth();
  return isAuthenticated ? children : <Navigate to="/login" replace />;
}

export default function App() {
  return (
    <AuthProvider>
      <ErrorBoundary>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/" element={
              <ProtectedRoute><DashboardPage /></ProtectedRoute>} />
            <Route path="/controls" element={
              <ProtectedRoute><ListPage /></ProtectedRoute>} />
            <Route path="/controls/new" element={
              <ProtectedRoute><FormPage /></ProtectedRoute>} />
            <Route path="/controls/:id" element={
              <ProtectedRoute><DetailPage /></ProtectedRoute>} />
            <Route path="/controls/:id/edit" element={
              <ProtectedRoute><FormPage /></ProtectedRoute>} />
            <Route path="/analytics" element={
              <ProtectedRoute><AnalyticsPage /></ProtectedRoute>} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </ErrorBoundary>
    </AuthProvider>
  );
}
