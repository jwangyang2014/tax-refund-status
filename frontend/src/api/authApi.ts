import { apiFetch, setAccessToken } from "./http";
import { readApiError } from "./error";

export type RegisterPayload = {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  address: string | null; // optional
  city: string;
  state: string;
  phone: string | null;
};

export async function register(payload: RegisterPayload): Promise<void> {
  const res = await apiFetch('/api/auth/register', {
    method: 'POST',
    body: JSON.stringify(payload),
  });

  if (!res.ok) throw new Error(await readApiError(res));
}

export async function login(email: string, password: string): Promise<void> {
  const res = await apiFetch('/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  });

  if (!res.ok) throw new Error(await readApiError(res));

  const data = (await res.json()) as { accessToken: string };
  setAccessToken(data.accessToken);
}

export async function logout(): Promise<void> {
  await apiFetch('/api/auth/logout', { method: 'POST' });
  setAccessToken(null);
}

export async function me(): Promise<{ userId: number; email: string; role: string }> {
  const res = await apiFetch('/api/profile/me');
  if (!res.ok) throw new Error(await readApiError(res));
  return (await res.json()) as { userId: number; email: string; role: string };
}