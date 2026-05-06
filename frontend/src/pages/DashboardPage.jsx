import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import Navbar from "../components/Navbar";
import { SkeletonCard } from "../components/Skeleton";
import { getStats } from "../services/api";

/**
 * Day 6 — Dashboard Page
 * 4 KPI cards from GET /stats + Recharts BarChart by status.
 */
export default function DashboardPage() {
  const [stats,   setStats]   = useState(null);
  const [loading, setLoading] = useState(true);
  const [error,   setError]   = useState("");

  useEffect(() => {
    fetchStats();
  }, []);

  const fetchStats = async () => {
    try {
      const res = await getStats();
      setStats(res.data.data);
    } catch {
      setError("Failed to load dashboard data.");
    } finally {
      setLoading(false);
    }
  };

  const kpiCards = stats ? [
    { label: "Total Controls",  value: stats.total,       icon: "📋", color: "#1B4F8A" },
    { label: "Compliant",       value: stats.compliant,   icon: "✅", color: "#16a34a" },
    { label: "Non-Compliant",   value: stats.nonCompliant,icon: "❌", color: "#dc2626" },
    { label: "Average Score",   value: stats.averageScore ? `${Math.round(stats.averageScore)}%` : "N/A", icon: "📊", color: "#d97706" },
  ] : [];

  const chartData = stats ? [
    { name: "Compliant",    value: stats.compliant    || 0, fill: "#16a34a" },
    { name: "Non-Compliant",value: stats.nonCompliant || 0, fill: "#dc2626" },
    { name: "Partial",      value: stats.partial      || 0, fill: "#d97706" },
    { name: "Not Assessed", value: stats.notAssessed  || 0, fill: "#6b7280" },
  ] : [];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
          <Link
            to="/controls/new"
            className="text-white px-5 py-2 rounded-lg text-sm font-medium hover:opacity-90 min-h-touch flex items-center"
            style={{ backgroundColor: "#1B4F8A" }}
          >
            + New Control
          </Link>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
            {error}
          </div>
        )}

        {/* KPI Cards */}
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {loading
            ? [...Array(4)].map((_, i) => <SkeletonCard key={i} />)
            : kpiCards.map((card) => (
              <div key={card.label}
                className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <div className="flex items-center justify-between mb-3">
                  <span className="text-sm text-gray-500 font-medium">{card.label}</span>
                  <span className="text-2xl">{card.icon}</span>
                </div>
                <p className="text-3xl font-bold" style={{ color: card.color }}>
                  {card.value}
                </p>
              </div>
            ))
          }
        </div>

        {/* Bar Chart */}
        <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100 mb-8">
          <h2 className="text-lg font-semibold text-gray-800 mb-4">
            Controls by Status
          </h2>
          {loading ? (
            <div className="skeleton h-64 w-full rounded-lg" />
          ) : (
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#1B4F8A"
                  radius={[4, 4, 0, 0]}
                  label={{ position: "top", fontSize: 12 }} />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Risk summary */}
        {stats && (
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
              <h2 className="text-lg font-semibold text-gray-800 mb-4">Risk Summary</h2>
              <div className="space-y-3">
                {[
                  { label: "Critical", value: stats.critical, color: "#dc2626" },
                  { label: "High",     value: stats.high,     color: "#ea580c" },
                ].map(item => (
                  <div key={item.label} className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">{item.label}</span>
                    <span className="font-bold text-lg" style={{ color: item.color }}>
                      {item.value || 0}
                    </span>
                  </div>
                ))}
              </div>
            </div>

            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
              <h2 className="text-lg font-semibold text-gray-800 mb-4">Quick Actions</h2>
              <div className="space-y-3">
                <Link to="/controls"
                  className="block w-full text-center border border-gray-200 rounded-lg py-3 text-sm text-gray-700 hover:bg-gray-50 min-h-touch flex items-center justify-center">
                  View All Controls
                </Link>
                <Link to="/analytics"
                  className="block w-full text-center border border-gray-200 rounded-lg py-3 text-sm text-gray-700 hover:bg-gray-50 min-h-touch flex items-center justify-center">
                  View Analytics
                </Link>
              </div>
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
