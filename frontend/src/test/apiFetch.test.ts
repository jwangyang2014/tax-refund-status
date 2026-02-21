import { describe, expect, it, beforeEach, vi, type MockedFunction } from 'vitest';
import { apiFetch, setAccessToken } from '../api/http';

describe('apiFetch', () => {
  beforeEach(() => {
    setAccessToken(null);
    vi.restoreAllMocks();
  });

  it('adds Authorization header when token exists', async () => {
    setAccessToken('abc');

    const fetchSpy = vi.spyOn(globalThis, 'fetch') as unknown as MockedFunction<typeof fetch>;
    
    fetchSpy.mockResolvedValueOnce(new Response(null, { status: 200 }));

    await apiFetch('/api/refund/latest');

    expect(fetchSpy).toHaveBeenCalled();

    const init = fetchSpy.mock.calls[0][1] as RequestInit;
    const headers = init.headers as Headers;

    expect(headers.get('Authorization')).toBe('Bearer abc');
  });

  it('refreshes once on 401 and retries', async () => {
    const first = new Response(null, { status: 401 });

    const refresh = new Response(
      JSON.stringify({ accessToken: 'newtoken' }),
      { status: 200 }
    );

    const second = new Response(JSON.stringify({ ok: true }), { status: 200 });

    const fetchSpy = vi.spyOn(globalThis, 'fetch') as unknown as MockedFunction<typeof fetch>;
    
    fetchSpy
      .mockResolvedValueOnce(first)
      .mockResolvedValueOnce(refresh)
      .mockResolvedValueOnce(second);

    const res = await apiFetch('/api/refund/latest');
    expect(res.status).toBe(200);

    expect(fetchSpy).toHaveBeenCalledTimes(3);

    // NOTE: because your implementation prepends SERVER_URL,
    // we only verify the path suffix instead of exact match
    expect(String(fetchSpy.mock.calls[1][0])).toContain('/api/auth/refresh');

    const retryInit = fetchSpy.mock.calls[2][1] as RequestInit;
    const retryHeaders = retryInit.headers as Headers;

    expect(retryHeaders.get('Authorization')).toBe('Bearer newtoken');
  });
});