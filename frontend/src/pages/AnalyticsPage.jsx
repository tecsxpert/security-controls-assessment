import { useState, useEffect, useRef } from "react";
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  PieChart, Pie, Cell, Legend, LineChart, Line
} from "recharts";
import Navbar from "../components/Navbar";
import { getStats, exportCsv } from "../services/api";

/**
 * Day 10 — CSV export, SSE streaming report
 * Day 11 — BarChart by category, LineChart over time, PieChart by status
 */

const PIE_COLORS = ["#16a34a", "#dc2626", "#d97706", "#6b7280"];

// Mock monthly trend data (replace with real API when available)
const MONTHLY_TREND = [
  { month: "Nov", compliant: 20, nonCompliant: 15 },
  { month: "Dec", compliant: 24, nonCompliant: 12 },
  { month: "Jan", compliant: 28, nonCompliant: 10 },
  { month: "Feb", compliant: 30, nonCompliant: 8  },
  { month: "Mar", compliant: 27, nonCompliant: 9  },
  { month: "Apr", compliant: 32, nonCompliant: 6  },
];

export default function AnalyticsPage() {
  const [stats,          setStats]          = useState(null);
  const [loading,        setLoading]        = useState(true);
  const [error,          setError]          = useState("");
  const [reportText,     setReportText]     = useState("");
  const [reportStreaming,setReportStreaming] = useState(false);
  const [period,         setPeriod]         = useState("6m");
  const eventSourceRef = useRef(null);

  useEffect(() => {
    fetchStats();
    return () => eventSourceRef.current?.close();
  }, []);

  const fetchStats = async () => {
    try {
      const res = await getStats();
      setStats(res.data.data);
    } catch {
      setError("Failed to load analytics.");
    } finally {
      setLoading(false);
    }
  };

  const handleExportCsv = async () => {
    try {
      const res = await exportCsv();
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a   = document.createElement("a");
      a.href     = `${url}`;
      a.download = "security-controls.csv";
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      setError("Export failed.");
    }
  };

  // SSE streaming report
  const handleGenerateReport = () => {
    setReportText("");
    setReportStreaming(true);
    eventSourceRef.current?.close();

    const apiUrl = import.meta.env.VITE_API_URL || "http://localhost:8080";
    const token  = localStorage.getItem("token");

    // Use fetch with ReadableStream for SSE with auth header
    fetch(`${apiUrl}/api/ai/generate-report`, {
      method: "POST",
      headers: {
        "Content-Type":  "application/json",
        "Authorization": `Bearer ${token}`,
      },
      body: JSON.stringify({ title: "Security Controls Assessment Report", controls: [] }),
    }).then(res => {
      const reader = res.body.getReader();
      const decoder = new TextDecoder();
      const read = () => {
        reader.read().then(({ done, value }) => {
          if (done) { setReportStreaming(false); return; }
          const chunk = decoder.decode(value);
          const lines = chunk.split("\n");
          lines.forEach(line => {
            if (line.startsWith("data: ")) {
              const text = line.replace("data: ", "");
              if (text !== "[DONE]") {
                setReportText(prev => prev + text);
              } else {
                setReportStreaming(false);
              }
            }
          });
          read();
        });
      };
      read();
    }).catch(() => setReportStreaming(false));
  };

  const pieData = stats ? [
    { name: "Compliant",     value: stats.compliant    || 0 },
    { name: "Non-Compliant", value: stats.nonCompliant || 0 },
    { name: "Partial",       value: stats.partial      || 0 },
    { name: "Not Assessed",  value: stats.notAssessed  || 0 },
  ] : [];

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">

        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <h1 className="text-2xl font-bold text-gray-900">Analytics</h1>
          <div className="flex gap-3">
            <select
              value={period}
              onChange={e => setPeriod(e.target.value)}
              className="border border-gray-300 rounded-lg px-3 py-2 text-sm min-h-touch"
            >
              <option value="3m">Last 3 months</option>
              <option value="6m">Last 6 months</option>
              <option value="12m">Last 12 months</option>
            </select>
            <button
              onClick={handleExportCsv}
              className="border border-gray-300 text-gray-700 px-4 py-2 rounded-lg text-sm hover:bg-gray-50 min-h-touch"
            >
              📥 Export CSV
            </button>
          </div>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
            {error}
          </div>
        )}

        {loading ? (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <div className="skeleton h-6 w-48 mb-4" />
                <div className="skeleton h-64 w-full" />
              </div>
            ))}
          </div>
        ) : (
          <div className="space-y-6">

            {/* Row 1: PieChart + LineChart */}
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">

              {/* PieChart — controls by status */}
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <h2 className="font-semibold text-gray-800 mb-4">Controls by Status</h2>
                <ResponsiveContainer width="100%" height={260}>
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      outerRadius={90}
                      dataKey="value"
                      label={({ name, percent }) =>
                        `${name} ${(percent * 100).toFixed(0)}%`}
                      labelLine={false}
                    >
                      {pieData.map((_, i) => (
                        <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                      ))}
                    </Pie>
                    <Legend />
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>

              {/* LineChart — compliance trend over time */}
              <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
                <h2 className="font-semibold text-gray-800 mb-4">Compliance Trend (6 months)</h2>
                <ResponsiveContainer width="100%" height={260}>
                  <LineChart data={MONTHLY_TREND}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="month" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} />
                    <Tooltip />
                    <Legend />
                    <Line type="monotone" dataKey="compliant"    stroke="#16a34a" strokeWidth={2} dot />
                    <Line type="monotone" dataKey="nonCompliant" stroke="#dc2626" strokeWidth={2} dot />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* Row 2: BarChart by risk */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
              <h2 className="font-semibold text-gray-800 mb-4">Controls by Risk Level</h2>
              <ResponsiveContainer width="100%" height={220}>
                <BarChart data={[
                  { name: "Critical", value: stats?.critical || 0, fill: "#dc2626" },
                  { name: "High",     value: stats?.high     || 0, fill: "#ea580c" },
                ]}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                  <YAxis allowDecimals={false} />
                  <Tooltip />
                  <Bar dataKey="value" radius={[4, 4, 0, 0]} fill="#1B4F8A"
                    label={{ position: "top", fontSize: 12 }} />
                </BarChart>
              </ResponsiveContainer>
            </div>

            {/* SSE Report Streaming */}
            <div className="bg-white rounded-xl p-6 shadow-sm border border-gray-100">
              <div className="flex items-center justify-between mb-4">
                <h2 className="font-semibold text-gray-800">AI Report Generator</h2>
                <button
                  onClick={handleGenerateReport}
                  disabled={reportStreaming}
                  className="text-white px-5 py-2 rounded-lg text-sm font-medium hover:opacity-90 min-h-touch disabled:opacity-50"
                  style={{ backgroundColor: "#1B4F8A" }}
                >
                  {reportStreaming ? "⏳ Generating..." : "Generate Report"}
                </button>
              </div>

              {reportStreaming && (
                <div className="flex items-center gap-2 text-sm text-gray-500 mb-3">
                  <div className="w-4 h-4 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
                  Streaming report...
                </div>
              )}

              {reportText && (
                <div className="bg-gray-50 rounded-lg p-4 text-sm text-gray-700 leading-relaxed whitespace-pre-wrap max-h-64 overflow-y-auto">
                  {reportText}
                  {reportStreaming && <span className="animate-pulse">▌</span>}
                </div>
              )}

              {!reportText && !reportStreaming && (
                <p className="text-sm text-gray-400">
                  Click "Generate Report" to create an AI-powered assessment report.
                </p>
              )}
            </div>
          </div>
        )}
      </main>
    </div>
  );
}
