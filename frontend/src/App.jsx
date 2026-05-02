import React, { useState } from 'react';
import { AlertCircle, CheckCircle2, FileText, Lightbulb, ListChecks, ShieldCheck } from 'lucide-react';
import { createRoot } from 'react-dom/client';
import './styles.css';

const API_BASE = import.meta.env.VITE_AI_API_BASE_URL || 'http://localhost:5000';

const initialForms = {
  describe: { control: 'MFA is required for all privileged accounts.' },
  categorise: { text: 'Quarterly access review evidence is missing for production databases.' },
  recommend: { finding: 'Backup encryption is not enforced for customer data stores.', context: { system: 'Customer platform' } },
  report: { assessment: { name: 'Q2 security controls review', controls: [{ id: 'IAM-01', status: 'gap', evidence: 'No review attached' }] } },
};

function callApi(path, payload) {
  return fetch(`${API_BASE}${path}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  }).then(async (response) => {
    const data = await response.json();
    if (!response.ok) {
      throw new Error(data.details || data.error || `Request failed: ${response.status}`);
    }
    return data;
  });
}

function useEndpoint(path, payloadBuilder) {
  const [state, setState] = useState({ loading: false, error: '', data: null });
  const run = async () => {
    setState({ loading: true, error: '', data: null });
    try {
      const data = await callApi(path, payloadBuilder());
      setState({ loading: false, error: '', data });
    } catch (error) {
      setState({ loading: false, error: error.message, data: null });
    }
  };
  return [state, run];
}

function JsonBlock({ data }) {
  if (!data) return null;
  return <pre className="result">{JSON.stringify(data, null, 2)}</pre>;
}

function Panel({ icon: Icon, title, children }) {
  return (
    <section className="panel">
      <header>
        <Icon size={20} />
        <h2>{title}</h2>
      </header>
      {children}
    </section>
  );
}

function EndpointButton({ state, onClick, label }) {
  return (
    <button onClick={onClick} disabled={state.loading}>
      {state.loading ? 'Running...' : label}
    </button>
  );
}

function App() {
  const [forms, setForms] = useState(initialForms);
  const [describeState, runDescribe] = useEndpoint('/describe', () => forms.describe);
  const [categoriseState, runCategorise] = useEndpoint('/categorise', () => forms.categorise);
  const [recommendState, runRecommend] = useEndpoint('/recommend', () => forms.recommend);
  const [reportState, runReport] = useEndpoint('/generate-report', () => forms.report);

  const updateText = (section, key, value) => setForms((current) => ({ ...current, [section]: { ...current[section], [key]: value } }));
  const updateJson = (section, key, value) => {
    try {
      updateText(section, key, JSON.parse(value));
    } catch {
      updateText(section, key, value);
    }
  };

  return (
    <main>
      <div className="topbar">
        <ShieldCheck size={28} />
        <div>
          <h1>Security Controls Assessment</h1>
          <p>AI-assisted control analysis connected directly to the Flask AI service.</p>
        </div>
      </div>

      <div className="grid">
        <Panel icon={FileText} title="Describe">
          <textarea value={forms.describe.control} onChange={(event) => updateText('describe', 'control', event.target.value)} />
          <EndpointButton state={describeState} onClick={runDescribe} label="Describe control" />
          {describeState.error && <p className="error"><AlertCircle size={16} />{describeState.error}</p>}
          <JsonBlock data={describeState.data} />
        </Panel>

        <Panel icon={ListChecks} title="Categorise">
          <textarea value={forms.categorise.text} onChange={(event) => updateText('categorise', 'text', event.target.value)} />
          <EndpointButton state={categoriseState} onClick={runCategorise} label="Categorise finding" />
          {categoriseState.error && <p className="error"><AlertCircle size={16} />{categoriseState.error}</p>}
          <JsonBlock data={categoriseState.data} />
        </Panel>

        <Panel icon={Lightbulb} title="Recommend">
          <textarea value={forms.recommend.finding} onChange={(event) => updateText('recommend', 'finding', event.target.value)} />
          <textarea
            value={JSON.stringify(forms.recommend.context, null, 2)}
            onChange={(event) => updateJson('recommend', 'context', event.target.value)}
          />
          <EndpointButton state={recommendState} onClick={runRecommend} label="Generate recommendations" />
          {recommendState.error && <p className="error"><AlertCircle size={16} />{recommendState.error}</p>}
          <JsonBlock data={recommendState.data} />
        </Panel>

        <Panel icon={CheckCircle2} title="Report">
          <textarea
            value={JSON.stringify(forms.report.assessment, null, 2)}
            onChange={(event) => updateJson('report', 'assessment', event.target.value)}
          />
          <EndpointButton state={reportState} onClick={runReport} label="Generate report" />
          {reportState.error && <p className="error"><AlertCircle size={16} />{reportState.error}</p>}
          <JsonBlock data={reportState.data} />
        </Panel>
      </div>
    </main>
  );
}

createRoot(document.getElementById('root')).render(<App />);
