/**
 * Colour-coded badges for Status and Risk Level.
 * Day 7 — Detail page badges
 * Day 14 — Brand polish
 */

const STATUS_STYLES = {
  COMPLIANT:     "bg-green-100 text-green-800",
  NON_COMPLIANT: "bg-red-100 text-red-800",
  PARTIAL:       "bg-yellow-100 text-yellow-800",
  NOT_ASSESSED:  "bg-gray-100 text-gray-600",
};

const RISK_STYLES = {
  CRITICAL: "bg-red-600 text-white",
  HIGH:     "bg-orange-500 text-white",
  MEDIUM:   "bg-yellow-400 text-gray-900",
  LOW:      "bg-green-500 text-white",
};

export function StatusBadge({ status }) {
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-semibold ${STATUS_STYLES[status] || "bg-gray-100 text-gray-600"}`}>
      {status?.replace("_", " ")}
    </span>
  );
}

export function RiskBadge({ riskLevel }) {
  return (
    <span className={`px-2 py-1 rounded-full text-xs font-bold ${RISK_STYLES[riskLevel] || "bg-gray-100 text-gray-600"}`}>
      {riskLevel}
    </span>
  );
}

export function ScoreBadge({ score }) {
  if (score == null) return <span className="text-gray-400">N/A</span>;
  const color = score >= 80 ? "text-green-600"
    : score >= 50 ? "text-yellow-600"
    : "text-red-600";
  return (
    <span className={`font-bold text-lg ${color}`}>{score}/100</span>
  );
}
