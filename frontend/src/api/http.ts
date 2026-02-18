let accessToken: string | null = null;

export function setAccessToken(token: string | null) {
  accessToken = token;
}

async function refreshAccessToken(): Promise<string> {
  const res = await fetch('/api/auth/refresh', {
    method: 'POST',
    credentials: 'include'
  });

  if (!res.ok) throw new Error('Refresh access token failed');

  const data = (await res.json()) as { accessToken: string};
  setAccessToken(data.accessToken);

  return data.accessToken;
}

export async function apiFetch(url: string, init: RequestInit = {}): Promise<Response> {
  let res = await doFetch(url, init);

  // If unauthorized, attempt to refresh once
  if (res.status === 401) {
    try {
      await refreshAccessToken();
      res = await doFetch(url, init);
    } catch {
      // fallthrough
    }
  }

  return res;
}

async function doFetch(url: string, init: RequestInit = {}): Promise<Response> {
  const headers = new Headers(init.headers || {});

  if (accessToken) headers.set('Authorization', `Bearer ${accessToken}`);
  headers.set('Content-Type', 'application/json');

  return await fetch(url, {
    ...init,
    headers,
    credentials: 'include'
  });
}
