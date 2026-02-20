import { describe, expect, it, vi, beforeEach } from 'vitest';
import { apiFetch, setAccessToken } from '../api/http';

describe('apiFetch', () => {
  beforeEach(() => {
    setAccessToken(null);
    vi.restoreAllMocks();
  });

  it('adds Authorization header when token exists', async () => {
    setAccessToken('abc');
    const fetchSpy = vi.spyOn(globalThis, 'fetch' as any).mockResolvedValueOnce(new Response(null, { status: 200 }));

    await apiFetch('/api/refund/latest');

    expect(fetchSpy).toHaveBeenCalled();
    const call = fetchSpy.mock.calls[0];
    const init = call[1] as RequestInit;
    const headers = init.headers as Headers;
    expect(headers.get('Authorization')).toBe('Bearer abc');
  });

  it('refreshes once on 401 and retries', async () => {
    // first call 401
    const first = new Response(null, { status: 401 });

    // refresh returns new token
    const refresh = new Response(JSON.stringify({ accessToken: 'newtoken' }), { status: 200 });

    // retry succeeds
    const second = new Response(JSON.stringify({ ok: true }), { status: 200 });

    const fetchSpy = vi.spyOn(globalThis, 'fetch' as any)
      .mockResolvedValueOnce(first)
      .mockResolvedValueOnce(refresh)
      .mockResolvedValueOnce(second);

    const res = await apiFetch('/api/refund/latest');
    expect(res.status).toBe(200);

    // Called 3 times: original, refresh, retry
    expect(fetchSpy).toHaveBeenCalledTimes(3);

    // verify refresh endpoint used
    expect(fetchSpy.mock.calls[1][0]).toBe('/api/auth/refresh');

    // verify retry used new token
    const retryInit = fetchSpy.mock.calls[2][1] as RequestInit;
    const retryHeaders = retryInit.headers as Headers;
    expect(retryHeaders.get('Authorization')).toBe('Bearer newtoken');
  });
});
