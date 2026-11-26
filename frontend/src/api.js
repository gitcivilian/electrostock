export async function request(path, options = {}) {
  const response = await fetch(path, options);
  const text = await response.text();
  const body = text ? JSON.parse(text) : null;
  if (!response.ok) {
    throw new Error(body?.errors?.join('; ') || body?.detail || `Request failed (${response.status})`);
  }
  return body;
}

function jsonRequest(path, method, payload) {
  return request(path, {
    method,
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  });
}

function buildQuery(params) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && String(value).trim() !== '') {
      searchParams.set(key, String(value).trim());
    }
  });
  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export function listCategories({ search = '', status = 'ALL' } = {}) {
  return request(`/api/categories${buildQuery({ search, status })}`);
}

export function createCategory(payload) {
  return jsonRequest('/api/categories', 'POST', payload);
}

export function updateCategory(id, payload) {
  return jsonRequest(`/api/categories/${encodeURIComponent(id)}`, 'PUT', payload);
}

export function deactivateCategory(id) {
  return request(`/api/categories/${encodeURIComponent(id)}/deactivate`, { method: 'PATCH' });
}

export function activateCategory(id) {
  return request(`/api/categories/${encodeURIComponent(id)}/activate`, { method: 'PATCH' });
}

export function listComponents({ search = '', categoryId = '', status = 'ALL' } = {}) {
  return request(`/api/components${buildQuery({ search, categoryId, status })}`);
}

export function createComponent(payload) {
  return jsonRequest('/api/components', 'POST', payload);
}

export function updateComponent(id, payload) {
  return jsonRequest(`/api/components/${encodeURIComponent(id)}`, 'PUT', payload);
}

export function deactivateComponent(id) {
  return request(`/api/components/${encodeURIComponent(id)}/deactivate`, { method: 'PATCH' });
}

export function activateComponent(id) {
  return request(`/api/components/${encodeURIComponent(id)}/activate`, { method: 'PATCH' });
}
