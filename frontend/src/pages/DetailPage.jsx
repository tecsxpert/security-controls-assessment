import { useState, useEffect } from "react";
import { useParams, useNavigate, Link } from "react-router-dom";
import Navbar from "../components/Navbar";
import { StatusBadge, RiskBadge, ScoreBadge } from "../components/Badges";
import { SkeletonDetail } from "../components/Skeleton";
import { getControl, deleteControl } from "../services/api";
import api from "../services/api";

/**
 * Day 7 — Detail page with all fields, colour-coded badges, Edit/Delete
 * Day 8 — AI panel with Ask AI button, loading spinner, retry on error
 */
export default function DetailPage() {
  const { id }   = useParams();
  const navigate = useNavigate();

  const [control,  setControl]  = useState(null);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState("");
  const [deleting, setDeleting] = useState(false);

  // AI panel state
  const [aiLoading,  setAiLoading]  = useState(false);
  const [aiResult,   setAiResult]   = useState(null);
  const [aiError,    setAiError]    = useState("");
  const [activeTab,  setActiveTab]  = useState("describe");

  useEffect(() => {
    fetchControl();
  }, [id]);

  const fetchControl = async () => {
    setLoading(true);
    try {
      const res = await getControl(id);
      setControl(res.data.data);
    } catch {
      setError("Failed to load control.");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm("Are you sure you want to delete this control?")) return;
    setDeleting(true);
    try {
      await deleteControl(id);
      navigate("/controls");
    } catch {
      setError("Failed to delete control.");
      setDeleting(false);
    }
  };

  const handleAskAI = async (tab) => {
    setActiveTab(tab);
    setAiLoading(true);
    setAiError("");
    setAiResult(null);
    try {
      let res;
      if (tab === "describe") {
        res = await api.post(`${import.meta.env.VITE_API_URL || "http://localhost:8080"}/api/ai/describe`, {
          control_name: control.controlName,
          category:     control.category,
          risk_level:   control.riskLevel,
        });
        setAiResult({ type: "describe", content: res.data.description });
      } else if (tab === "recommend") {
        res = await api.post(`${import.meta.env.VITE_API_URL || "http://localhost:8080"}/api/ai/recommend`, {
          control_name: control.controlName,
          status:       control.status,
          risk_level:   control.riskLevel,
          description:  control.description,
        });
        setAiResult({ type: "recommend", content: res.data.recommendations });
      }
    } catch {
      setAiError("AI service is temporarily unavailable. Please try again.");
    } finally {
      setAiLoading(false);
    }
  };

  const Field = ({ label, value }) => (
    <div className="flex flex-col sm:flex-row sm:gap-4 py-3 border-b border-gray-100 last:border-0">
      <span className="text-sm font-medium text-gray-500 sm:w-48 shrink-0">{label}</span>
      <span className="text-sm text-gray-900 mt-1 sm:mt-0">{value || <span className="text-gray-400">—</span>}</span>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />

      <main className="max-w-4xl mx-auto px-4 py-8">

        {/* Back */}
        <div className="flex items-center gap-3 mb-6">
          <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-600 text-lg">←</button>
          <h1 className="text-2xl font-bold text-gray-900">Control Detail</h1>
        </div>

        {error && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm">
            {error}
          </div>
        )}

        {loading ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
            <SkeletonDetail />
          </div>
        ) : control ? (
          <div className="space-y-6">

            {/* Header card */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
              <div className="flex flex-col sm:flex-row sm:items-start justify-between gap-4 mb-4">
                <div>
                  <span className="font-mono text-xs text-gray-400">{control.controlId}</span>
                  <h2 className="text-xl font-bold text-gray-900 mt-1">{control.controlName}</h2>
                  <p className="text-sm text-gray-500 mt-1">{control.category}</p>
                </div>
                <div className="flex items-center gap-2">
                  <Link
                    to={`/controls/${id}/edit`}
                    className="px-4 py-2 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 min-h-touch flex items-center"
                  >
                    ✏️ Edit
                  </Link>
                  <button
                    onClick={handleDelete}
                    disabled={deleting}
                    className="px-4 py-2 bg-red-600 text-white rounded-lg text-sm hover:bg-red-700 min-h-touch flex items-center disabled:opacity-50"
                  >
                    {deleting ? "Deleting..." : "🗑️ Delete"}
                  </button>
                </div>
              </div>

              {/* Badges */}
              <div className="flex flex-wrap gap-3">
                <StatusBadge status={control.status} />
                <RiskBadge riskLevel={control.riskLevel} />
                <ScoreBadge score={control.score} />
              </div>
            </div>

            {/* Details */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
              <h3 className="font-semibold text-gray-800 mb-4">Details</h3>
              <Field label="Description"      value={control.description} />
              <Field label="Owner"            value={control.owner} />
              <Field label="Department"       value={control.department} />
              <Field label="Assessment Date"  value={control.assessmentDate?.substring(0, 10)} />
              <Field label="Next Review Date" value={control.nextReviewDate?.substring(0, 10)} />
              <Field label="Evidence"         value={control.evidence} />
              <Field label="Remediation Plan" value={control.remediationPlan} />
              <Field label="Created By"       value={control.createdBy} />
              <Field label="Last Updated"     value={control.updatedAt?.substring(0, 10)} />
            </div>

            {/* AI Analysis Panel */}
            <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
              <h3 className="font-semibold text-gray-800 mb-4">🤖 AI Analysis</h3>

              {/* AI Tab buttons */}
              <div className="flex gap-3 mb-4">
                {[
                  { key: "describe",   label: "AI Description"      },
                  { key: "recommend",  label: "AI Recommendations"  },
                ].map(tab => (
                  <button
                    key={tab.key}
                    onClick={() => handleAskAI(tab.key)}
                    disabled={aiLoading}
                    className="px-4 py-2 rounded-lg text-sm font-medium border min-h-touch disabled:opacity-50 transition-colors"
                    style={activeTab === tab.key && aiResult
                      ? { backgroundColor: "#1B4F8A", color: "white", borderColor: "#1B4F8A" }
                      : { borderColor: "#1B4F8A", color: "#1B4F8A" }}
                  >
                    {tab.label}
                  </button>
                ))}
              </div>

              {/* AI Loading spinner */}
              {aiLoading && (
                <div className="flex items-center gap-3 text-gray-500 py-6">
                  <div className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
                  <span className="text-sm">Analysing with AI...</span>
                </div>
              )}

              {/* AI Error with retry */}
              {aiError && !aiLoading && (
                <div className="bg-red-50 border border-red-200 rounded-lg p-4">
                  <p className="text-red-700 text-sm mb-3">{aiError}</p>
                  <button
                    onClick={() => handleAskAI(activeTab)}
                    className="text-sm text-red-600 border border-red-300 px-4 py-2 rounded-lg hover:bg-red-50 min-h-touch flex items-center gap-2"
                  >
                    🔄 Retry
                  </button>
                </div>
              )}

              {/* AI Result */}
              {aiResult && !aiLoading && (
                <div className="bg-blue-50 border border-blue-100 rounded-lg p-4">
                  {aiResult.type === "describe" && (
                    <p className="text-sm text-gray-700 leading-relaxed">{aiResult.content}</p>
                  )}
                  {aiResult.type === "recommend" && Array.isArray(aiResult.content) && (
                    <div className="space-y-3">
                      {aiResult.content.map((rec, i) => (
                        <div key={i} className="bg-white rounded-lg p-3 border border-blue-100">
                          <div className="flex items-center gap-2 mb-1">
                            <span className="text-xs font-bold px-2 py-0.5 rounded"
                              style={{ backgroundColor: "#1B4F8A", color: "white" }}>
                              {rec.action_type}
                            </span>
                            <span className={`text-xs font-medium ${
                              rec.priority === "HIGH"   ? "text-red-600"
                            : rec.priority === "MEDIUM" ? "text-yellow-600"
                            : "text-green-600"}`}>
                              {rec.priority}
                            </span>
                          </div>
                          <p className="text-sm text-gray-700">{rec.description}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}

              {/* Stored AI fields */}
              {!aiResult && !aiLoading && !aiError && (
                <>
                  {control.aiDescription && (
                    <div className="mb-4">
                      <p className="text-xs font-medium text-gray-500 mb-2">Previous AI Description</p>
                      <p className="text-sm text-gray-700 bg-gray-50 rounded-lg p-3">{control.aiDescription}</p>
                    </div>
                  )}
                  {!control.aiDescription && (
                    <p className="text-sm text-gray-400 py-4">
                      Click a button above to get AI analysis for this control.
                    </p>
                  )}
                </>
              )}
            </div>
          </div>
        ) : null}
      </main>
    </div>
  );
}
