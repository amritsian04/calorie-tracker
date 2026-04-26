const BASE_URL = 'http://localhost:8080';

export async function apiFetch(path, options = {}) {
  const { headers, body, ...rest } = options;
  const response = await fetch(`${BASE_URL}${path}`, {
    credentials: 'include',
    headers: {
      'Content-Type': 'application/json',
      ...headers,
    },
    body: body ? JSON.stringify(body) : undefined,
    ...rest,
  });
  return response;
}

export function toDateStr(date = new Date()) {
  return date.toISOString().split('T')[0];
}

export function formatDate(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' });
}
