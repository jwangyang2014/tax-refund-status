import { apiFetch } from './http'; // adjust if your project uses a different helper

export type AssistantCitation = {
  docId: string;
  quote: string;
};

export type AssistantAction =
  | { type: 'REFRESH'; label: string }
  | { type: 'CONTACT_SUPPORT'; label: string }
  | { type: 'SHOW_TRACKING'; label: string };

export type AssistantResponse = {
  answerMarkdown: string;
  citations: AssistantCitation[];
  actions: AssistantAction[];
  confidence: 'LOW' | 'MEDIUM' | 'HIGH';
};

export async function askAssistant(question: string): Promise<AssistantResponse> {
  const res = await apiFetch('/api/assistant/chat', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    credentials: 'include',
    body: JSON.stringify({ question })
  });
  if (!res.ok) throw new Error(await res.text());
  return res.json();
}