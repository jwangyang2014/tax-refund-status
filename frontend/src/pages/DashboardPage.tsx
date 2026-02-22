import React, { useCallback, useEffect, useState } from 'react';
import { getLatestRefund, simulateRefundUpdate } from '../api/refundApi';
import { askAssistant, type AssistantResponse } from '../api/assistantApi';
import type { RefundStatusResponse } from '../api/.types';
import { errorMessage } from '../utils';

function nextStatus(curr: string): string {
  const ordered = ['RECEIVED', 'PROCESSING', 'APPROVED', 'SENT', 'AVAILABLE'];
  const index = ordered.indexOf(curr);
  return index < 0 ? 'RECEIVED' : ordered[Math.min(index + 1, ordered.length - 1)];
}

export default function DashboardPage({
  onLogout,
  onError
}: {
  onLogout: () => void;
  onError: (msg: string) => void;
}) {
  const [data, setData] = useState<RefundStatusResponse | null>(null);
  const [loading, setLoading] = useState(false);

  // Assistant UI state
  const [question, setQuestion] = useState('');
  const [asking, setAsking] = useState(false);
  const [assistant, setAssistant] = useState<AssistantResponse | null>(null);

  // stable callback (no infinite rerender)
  const load = useCallback(async () => {
    setLoading(true);
    try {
      const latest = await getLatestRefund();
      setData(latest);
    } catch (err: unknown) {
      onError(errorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [onError]);

  // runs once on mount
  useEffect(() => {
    load();
  }, [load]);

  async function demoAdvanceStatus() {
    if (!data) return;

    const next = nextStatus(data.status);

    try {
      await simulateRefundUpdate({
        taxYear: data.taxYear,
        status: next,
        expectedAmount: data.expectedAmount ?? 1234.56,
        trackingId: data.trackingId ?? 'MOCK-TRACK'
      });

      await load();
    } catch (e: unknown) {
      onError(errorMessage(e));
    }
  }

  async function onAsk() {
    if (!question.trim()) return;
    setAsking(true);
    try {
      const resp = await askAssistant(question.trim());
      setAssistant(resp);
    } catch (e: unknown) {
      onError(errorMessage(e));
    } finally {
      setAsking(false);
    }
  }

  function handleAction(a: AssistantResponse['actions'][number]) {
    if (a.type === 'REFRESH') {
      load();
      return;
    }
    if (a.type === 'SHOW_TRACKING') {
      // demo: just scroll to refund section; customize later if you have tracking page
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    if (a.type === 'CONTACT_SUPPORT') {
      window.location.href = 'mailto:support@example.com?subject=Refund%20Help';
      return;
    }
  }

  return (
    <div>
      <h3>Refund Status</h3>

      <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
        <button onClick={load} disabled={loading}>
          {loading ? '...' : 'Refresh'}
        </button>

        <button onClick={demoAdvanceStatus} disabled={!data}>
          Demo: Advance Status
        </button>

        <button onClick={onLogout}>Logout</button>
      </div>

      {!data ? (
        <p>No data yet</p>
      ) : (
        <div style={{ border: '1px solid #999', padding: 12, maxWidth: 720 }}>
          <p><strong>Tax Year:</strong> {data.taxYear}</p>
          <p data-testid="refund-status"><strong>Status:</strong> {data.status}</p>
          <p><strong>Expected amount:</strong> {data.expectedAmount}</p>
          <p><strong>Tracking ID:</strong> {data.trackingId ?? 'N/A'}</p>
          <p><strong>Last updated:</strong> {new Date(data.lastUpdatedAt).toLocaleString()}</p>

          {data.availableAtEstimated && (
            <p>
              <strong>Estimated available at:</strong>{' '}
              {new Date(data.availableAtEstimated).toLocaleString()}
            </p>
          )}
        </div>
      )}

      {/* Assistant section */}
      <div style={{ marginTop: 18, border: '1px solid #999', padding: 12, maxWidth: 720 }}>
        <h4>Ask about your refund</h4>

        <div style={{ display: 'flex', gap: 8, marginBottom: 8 }}>
          <input
            style={{ flex: 1, padding: 8 }}
            placeholder="E.g., When will my refund be available? Why is it stuck in processing?"
            value={question}
            onChange={(e) => setQuestion(e.target.value)}
            onKeyDown={(e) => {
              if (e.key === 'Enter') onAsk();
            }}
          />
          <button onClick={onAsk} disabled={asking || !question.trim()}>
            {asking ? '...' : 'Ask'}
          </button>
        </div>

        {!assistant ? (
          <p style={{ margin: 0 }}>No assistant response yet.</p>
        ) : (
          <div>
            <div style={{ padding: 10, border: '1px solid #ddd' }}>
              <div style={{ marginBottom: 6 }}>
                <strong>Confidence:</strong> {assistant.confidence}
              </div>

              {/* Demo-simple markdown rendering: show as plain text */}
              <pre style={{ whiteSpace: 'pre-wrap', margin: 0 }}>
                {assistant.answerMarkdown}
              </pre>
            </div>

            {assistant.actions?.length > 0 && (
              <div style={{ marginTop: 10, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                {assistant.actions.map((a, idx) => (
                  <button key={idx} onClick={() => handleAction(a)}>
                    {a.label}
                  </button>
                ))}
              </div>
            )}

            {assistant.citations?.length > 0 && (
              <div style={{ marginTop: 10 }}>
                <strong>Citations</strong>
                <ul>
                  {assistant.citations.map((c, idx) => (
                    <li key={idx}>
                      <code>{c.docId}</code>: {c.quote}
                    </li>
                  ))}
                </ul>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
}