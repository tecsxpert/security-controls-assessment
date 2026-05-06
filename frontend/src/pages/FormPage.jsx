import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import Navbar from "../components/Navbar";
import { createControl, updateControl, getControl } from "../services/api";

/**
 * Day 3 — Create/Edit Form
 * Controlled inputs, client-side validation, inline error messages.
 */
const INITIAL_FORM = {
  controlName: "", controlId: "", description: "",
  category: "", status: "", riskLevel: "",
  score: "", owner: "", department: "",
  assessmentDate: "", nextReviewDate: "",
  evidence: "", remediationPlan: "",
};

export default function FormPage() {
  const { id }      = useParams();
  const isEdit      = !!id;
  const navigate    = useNavigate();

  const [form,    setForm]    = useState(INITIAL_FORM);
  const [errors,  setErrors]  = useState({});
  const [loading, setLoading] = useState(false);
  const [fetching,setFetching]= useState(isEdit);
  const [apiError,setApiError]= useState("");

  // Load existing data for edit
  useEffect(() => {
    if (isEdit) {
      getControl(id)
        .then(res => {
          const d = res.data.data;
          setForm({
            controlName:    d.controlName    || "",
            controlId:      d.controlId      || "",
            description:    d.description    || "",
            category:       d.category       || "",
            status:         d.status         || "",
            riskLevel:      d.riskLevel      || "",
            score:          d.score          != null ? String(d.score) : "",
            owner:          d.owner          || "",
            department:     d.department     || "",
            assessmentDate: d.assessmentDate ? d.assessmentDate.substring(0, 10) : "",
            nextReviewDate: d.nextReviewDate ? d.nextReviewDate.substring(0, 10) : "",
            evidence:       d.evidence       || "",
            remediationPlan:d.remediationPlan|| "",
          });
        })
        .catch(() => setApiError("Failed to load control data."))
        .finally(() => setFetching(false));
    }
  }, [id, isEdit]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    // Clear inline error on change
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: "" }));
  };

  const validate = () => {
    const errs = {};
    if (!form.controlName.trim())    errs.controlName = "Control name is required";
    if (!form.controlId.trim())      errs.controlId   = "Control ID is required";
    if (!/^[A-Z]{2}-\d{3}$/.test(form.controlId))
                                     errs.controlId   = "Format must be: AC-001";
    if (!form.description.trim())    errs.description = "Description is required";
    if (!form.category.trim())       errs.category    = "Category is required";
    if (!form.status)                errs.status      = "Status is required";
    if (!form.riskLevel)             errs.riskLevel   = "Risk level is required";
    if (form.score && (isNaN(form.score) || +form.score < 0 || +form.score > 100))
                                     errs.score       = "Score must be 0-100";
    return errs;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length > 0) {
      setErrors(errs);
      return;
    }

    setLoading(true);
    setApiError("");
    try {
      const payload = {
        ...form,
        score: form.score ? parseInt(form.score) : null,
        assessmentDate: form.assessmentDate ? `${form.assessmentDate}T00:00:00` : null,
        nextReviewDate: form.nextReviewDate  ? `${form.nextReviewDate}T00:00:00`  : null,
      };
      if (isEdit) {
        await updateControl(id, payload);
      } else {
        await createControl(payload);
      }
      navigate("/controls");
    } catch (err) {
      setApiError(err.response?.data?.message || "Failed to save. Please try again.");
    } finally {
      setLoading(false);
    }
  };

  if (fetching) {
    return (
      <div className="min-h-screen bg-gray-50">
        <Navbar />
        <div className="max-w-3xl mx-auto px-4 py-8">
          <div className="skeleton h-8 w-48 mb-6" />
          {[...Array(6)].map((_, i) => (
            <div key={i} className="mb-4">
              <div className="skeleton h-4 w-32 mb-2" />
              <div className="skeleton h-11 w-full" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  const InputField = ({ label, name, type = "text", required, placeholder, helpText }) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
      <input
        type={type}
        name={name}
        value={form[name]}
        onChange={handleChange}
        placeholder={placeholder}
        className={`w-full border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 min-h-touch ${
          errors[name] ? "border-red-400 focus:ring-red-300" : "border-gray-300 focus:ring-blue-300"
        }`}
      />
      {errors[name]  && <p className="text-red-500 text-xs mt-1">{errors[name]}</p>}
      {helpText && !errors[name] && <p className="text-gray-400 text-xs mt-1">{helpText}</p>}
    </div>
  );

  const SelectField = ({ label, name, options, required }) => (
    <div>
      <label className="block text-sm font-medium text-gray-700 mb-1">
        {label} {required && <span className="text-red-500">*</span>}
      </label>
      <select
        name={name}
        value={form[name]}
        onChange={handleChange}
        className={`w-full border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 min-h-touch ${
          errors[name] ? "border-red-400" : "border-gray-300"
        }`}
      >
        <option value="">Select...</option>
        {options.map(o => (
          <option key={o.value} value={o.value}>{o.label}</option>
        ))}
      </select>
      {errors[name] && <p className="text-red-500 text-xs mt-1">{errors[name]}</p>}
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-50">
      <Navbar />
      <main className="max-w-3xl mx-auto px-4 py-8">
        <div className="flex items-center gap-3 mb-6">
          <button onClick={() => navigate(-1)} className="text-gray-400 hover:text-gray-600">←</button>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEdit ? "Edit Control" : "New Security Control"}
          </h1>
        </div>

        {apiError && (
          <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-6 text-sm">
            {apiError}
          </div>
        )}

        <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 space-y-5">

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <InputField label="Control Name" name="controlName" required placeholder="Access Control Policy" />
            <InputField label="Control ID" name="controlId" required placeholder="AC-001" helpText="Format: XX-000 (e.g. AC-001)" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Description <span className="text-red-500">*</span>
            </label>
            <textarea
              name="description"
              value={form.description}
              onChange={handleChange}
              rows={3}
              placeholder="Describe what this control does..."
              className={`w-full border rounded-lg px-4 py-3 text-sm focus:outline-none focus:ring-2 resize-none ${
                errors.description ? "border-red-400" : "border-gray-300"
              }`}
            />
            {errors.description && <p className="text-red-500 text-xs mt-1">{errors.description}</p>}
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <InputField label="Category" name="category" required placeholder="Access Control" />
            <InputField label="Score (0-100)" name="score" type="number" placeholder="85" />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <SelectField label="Status" name="status" required options={[
              { value: "COMPLIANT",     label: "Compliant"     },
              { value: "NON_COMPLIANT", label: "Non-Compliant" },
              { value: "PARTIAL",       label: "Partial"       },
              { value: "NOT_ASSESSED",  label: "Not Assessed"  },
            ]} />
            <SelectField label="Risk Level" name="riskLevel" required options={[
              { value: "CRITICAL", label: "Critical" },
              { value: "HIGH",     label: "High"     },
              { value: "MEDIUM",   label: "Medium"   },
              { value: "LOW",      label: "Low"      },
            ]} />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <InputField label="Owner" name="owner" placeholder="John Smith" />
            <InputField label="Department" name="department" placeholder="IT Security" />
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
            <InputField label="Assessment Date" name="assessmentDate" type="date" />
            <InputField label="Next Review Date" name="nextReviewDate" type="date" />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Evidence</label>
            <textarea
              name="evidence"
              value={form.evidence}
              onChange={handleChange}
              rows={2}
              placeholder="Evidence of compliance..."
              className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none resize-none"
            />
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Remediation Plan</label>
            <textarea
              name="remediationPlan"
              value={form.remediationPlan}
              onChange={handleChange}
              rows={2}
              placeholder="Steps to remediate if non-compliant..."
              className="w-full border border-gray-300 rounded-lg px-4 py-3 text-sm focus:outline-none resize-none"
            />
          </div>

          <div className="flex gap-3 pt-2">
            <button
              type="submit"
              disabled={loading}
              className="flex-1 text-white py-3 rounded-lg font-medium text-sm hover:opacity-90 disabled:opacity-50 min-h-touch"
              style={{ backgroundColor: "#1B4F8A" }}
            >
              {loading ? "Saving..." : isEdit ? "Update Control" : "Create Control"}
            </button>
            <button
              type="button"
              onClick={() => navigate(-1)}
              className="px-6 py-3 border border-gray-300 rounded-lg text-sm text-gray-700 hover:bg-gray-50 min-h-touch"
            >
              Cancel
            </button>
          </div>
        </form>
      </main>
    </div>
  );
}
