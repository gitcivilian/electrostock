export async function request(path, options = {}) {
  const response = await fetch(path, options);
  const body = await response.json();
  if (!response.ok) {
    throw new Error(body.errors?.join('; ') || body.detail || `Request failed (${response.status})`);
  }
  return body;
}
