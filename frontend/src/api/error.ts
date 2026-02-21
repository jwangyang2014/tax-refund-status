export interface ApiError {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
}

function isApiError(value: unknown): value is ApiError {
  if (typeof value !== 'object' || value === null) return false;
  const v = value as Record<string, unknown>;
  return typeof v.message === 'string' || typeof v.error === 'string';
}

export async function readApiError(res: Response): Promise<string> {
  const contentType = res.headers.get('content-type') ?? '';

  try {
    if (contentType.includes('application/json')) {
      const data: unknown = await res.json();

      if (isApiError(data)) {
        return data.message ?? data.error ?? `${res.status} ${res.statusText}`;
      }

      return JSON.stringify(data);
    }

    const text = await res.text();
    return text || `${res.status} ${res.statusText}`;
  } catch {
    return `${res.status} ${res.statusText}`;
  }
}