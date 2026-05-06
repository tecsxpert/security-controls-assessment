import { useState, useEffect, useCallback } from "react";
import { Link, useSearchParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import { StatusBadge, RiskBadge } from "../components/Badges";
import { SkeletonRow, EmptyState } from "../components/Skeleton";
import { getControls, searchControls, exportCsv } from "../services/api";

/**
 * Day 2 — List view with table, skeleton, empty state
 * Day 4 — Pagination, sort by column, Spring Page response
 * Day 9 — Debounced search, status filter, URL query params
 * Day 10 — CSV export
 */
export default function ListPage() {
  const [controls,   setControls]   = useState([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalItems, setTotalItems] = useState(0);
  const [loading,    setLoading]    = useState(true);
  const [error,      setError]      = useState("");
  const [sortBy,     setSortBy]     = useState("createdAt");
  const [sortDir,    setSortDir]    = useState("desc");

  const [searchParams, setSearchParams] = useSearchParams();
  const page     = parseInt(searchParams.get("page")   || "0");
  const search   = searchParams.get("search") || "";
  const status   = searchParams.get("status") || "";

  // Debounced search
  const [searchInput, setSearchInput] = useState(search);
  useEffect(() => {
    const timer = setTimeout(() => {
      setSearchParams(prev => {
        const next = new URLSearchParams(prev);
        if (searchInput) next.set("search", searchInput);
        else next.delete("search");
        next.set("page", "0");
        return next;
      });
    }, 300);  // 300ms debounce
    return () => clearTimeout(timer);
  }, [searchInput]);

  const fetchControls = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      let res;
      if (search) {
        res = await searchControls(search, page);
      } else {
        res = await getControls(page, 10, sortBy, sortDir);
      }
      const data = res.data.data;
      setControls(data.content || []);
      setTotalPages(data.totalPages || 0);
      setTotalItems(data.totalElements || 0);
    } catch {
      setError("Failed to load controls.");
    } finally {
      setLoading(false);
    }
  }, [page, search, status, sortBy, sortDir]);

  useEffect(() => { fetchControls(); }, [fetchControls]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortDir(d => d === "asc" ? "desc" : "asc");
    } else {
      setSortBy(field);
      setSortDir("asc");
    }
  };

  const handleExportCsv = async () => {
    try {
      const res = await exportCsv();
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a   = document.createElement("a");
      a.href = url;
      a.download = "security-controls.csv";
      a.click();
      window.URL.revokeObjectURL(url);
    } catch {
      setError("Export failed. Please try again.");
    }
  };

  const SortIcon = ({ field }) => {
    if (sortBy !== field) return <span className="text-gray-300 ml-1">↕</span>;
    return <span className="ml-1">{sortDir === "asc" ? "↑" : "↓"}</span>;
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-7xl mx-auto px-4 py-8">

        {/* Header */}
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Security Controls</h1>
            {!loading && (
              <p className="text-sm text-gray-500 mt-1">{totalItems} controls found</p>
            )}
          </div>
          <div className="flex gap-3">
            <button
              onClick={handleExportCsv}
              className="border border-gray-300 text-gray-700 px-4 py-2 rounded-lg text-sm hover:bg-gray-50 min-h-touch flex items-center gap-2"
            >
              📥 Export CSV
            </button>
            <Link
              to="/controls/new"
              className="text-white px-5 py-2 rounded-lg text-sm font-medium hover:opacity-90 min-h-touch flex items-center"
              style={{ backgroundColor: "#1B4F8A" }}
            >
              + New Control
            </Link>
          </div>
        </div>

        {/* Search + Filter */}
        <div className="bg-white rounded-xl p-4 shadow-sm border border-gray-100 mb-6 flex flex-col sm:flex-row gap-3">
          <input
            type="text"
            placeholder="Search controls..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="flex-1 border border-gray-300 rounded-lg px-4 py-2 text-sm focus:outline-none focus:ring-2 min-h-touch"
            style={{ "--tw-ring-color": "#1B4F8A" }}
          />
          <select
            value={status}
            onChange={(e) => setSearchParams(prev => {
              const next = new URLSearchParams(prev);
              if (e.target.value) next.set("status", e.target.value);
              else next.delete("status");
              next.set("page", "0");
              return next;
            })}
            className="border border-gray-300 rounded-lg px-4 py-2 text-sm min-h-touch focus:outline-none"
          >
            <option value="">All Statuses</option>
            <option value="COMPLIANT">Compliant</option>
            <option value="NON_COMPLIANT">Non-Compliant</option>
            <option value="PARTIAL">Partial</option>
            <option value="NOT_ASSESSED">Not Assessed</option>
          </select>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
            {error}
          </div>
        )}

        {/* Table */}
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead style={{ backgroundColor: "#1B4F8A" }}>
                <tr>
                  {[
                    { label: "Control ID",   field: "controlId"   },
                    { label: "Control Name", field: "controlName" },
                    { label: "Category",     field: "category"    },
                    { label: "Status",       field: "status"      },
                    { label: "Risk Level",   field: "riskLevel"   },
                    { label: "Score",        field: "score"       },
                  ].map(col => (
                    <th
                      key={col.field}
                      onClick={() => handleSort(col.field)}
                      className="text-left px-4 py-3 text-white font-medium cursor-pointer hover:opacity-80 select-none"
                    >
                      {col.label}<SortIcon field={col.field} />
                    </th>
                  ))}
                  <th className="px-4 py-3 text-white font-medium text-left">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {loading
                  ? [...Array(5)].map((_, i) => <SkeletonRow key={i} />)
                  : controls.length === 0
                  ? (
                    <tr>
                      <td colSpan={7}>
                        <EmptyState
                          message="No security controls found"
                          icon="🔍"
                        />
                      </td>
                    </tr>
                  )
                  : controls.map(control => (
                    <tr key={control.id}
                      className="hover:bg-gray-50 transition-colors">
                      <td className="px-4 py-3 font-mono text-xs text-gray-600">
                        {control.controlId}
                      </td>
                      <td className="px-4 py-3 font-medium text-gray-900 max-w-xs truncate">
                        {control.controlName}
                      </td>
                      <td className="px-4 py-3 text-gray-600">{control.category}</td>
                      <td className="px-4 py-3">
                        <StatusBadge status={control.status} />
                      </td>
                      <td className="px-4 py-3">
                        <RiskBadge riskLevel={control.riskLevel} />
                      </td>
                      <td className="px-4 py-3 text-gray-700">
                        {control.score != null ? `${control.score}/100` : "N/A"}
                      </td>
                      <td className="px-4 py-3">
                        <Link
                          to={`/controls/${control.id}`}
                          className="text-blue-600 hover:underline text-xs font-medium mr-3"
                          style={{ color: "#1B4F8A" }}
                        >
                          View
                        </Link>
                        <Link
                          to={`/controls/${control.id}/edit`}
                          className="text-gray-500 hover:underline text-xs"
                        >
                          Edit
                        </Link>
                      </td>
                    </tr>
                  ))
                }
              </tbody>
            </table>
          </div>

          {/* Pagination */}
          {!loading && totalPages > 1 && (
            <div className="px-4 py-3 border-t border-gray-100 flex items-center justify-between">
              <p className="text-sm text-gray-500">
                Page {page + 1} of {totalPages}
              </p>
              <div className="flex gap-2">
                <button
                  disabled={page === 0}
                  onClick={() => setSearchParams(prev => {
                    const next = new URLSearchParams(prev);
                    next.set("page", String(page - 1));
                    return next;
                  })}
                  className="px-3 py-1 border rounded text-sm disabled:opacity-40 min-h-touch flex items-center"
                >
                  ← Prev
                </button>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => setSearchParams(prev => {
                    const next = new URLSearchParams(prev);
                    next.set("page", String(page + 1));
                    return next;
                  })}
                  className="px-3 py-1 border rounded text-sm disabled:opacity-40 min-h-touch flex items-center"
                >
                  Next →
                </button>
              </div>
            </div>
          )}
        </div>
      </main>
    </div>
  );
}
