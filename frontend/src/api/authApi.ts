import { apiFetch, setAccessToken } from "./http";

export async function register(email: string, password: string): Promise<void> {
  const res = await apiFetch('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify({email, password}),
  });

  if (!res.ok) throw new Error(await res.text());
}

export async function login(email: string, password: string): Promise<void> {
  const res = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({email, password}),
  });

  if (!res.ok) throw new Error(await res.text());
  const data = (await res.json()) as { accessToken: string }
  setAccessToken(data.accessToken);
}

export async function logout(): Promise<void> {
  await apiFetch('/api/auth/logout', { method: 'POST' });
  setAccessToken(null);
}

export async function me(): Promise<{ userId: number; email: string, role:string }> {
  const res = await apiFetch('/api/auth/me');

  if (!res.ok) throw new Error('Not logged in');
  return (await res.json()) as { userId: number, email: string, role: string };
}