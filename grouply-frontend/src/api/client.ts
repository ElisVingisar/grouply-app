const API_BASE = import.meta.env.VITE_API_BASE || "http://localhost:8080";

export function getToken(): string | null {
  return localStorage.getItem("authToken");
}

function joinUrl(base: string, path: string) {
  if (path.startsWith("http")) return path;
  return `${base.replace(/\/+$/,"")}/${path.replace(/^\/+/, "")}`;
}

export async function apiFetch(input: string, init: RequestInit = {}) {
  const token = getToken();
  const headers: Record<string, string> = {
    ...(init.headers as Record<string, string> | undefined),
  };
  if (token) headers.Authorization = `Bearer ${token}`;

  const url = joinUrl(API_BASE, input);
  const res = await fetch(url, { ...init, headers });

  if (!res.ok) {
    console.warn("apiFetch non-OK", { url, status: res.status });
  }
  return res;
}