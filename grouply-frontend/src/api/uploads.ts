import { apiFetch } from "./client";

type UploadResponse = { url?: string; path?: string };

export async function uploadImage(file: File): Promise<string> {
  const fd = new FormData();
  fd.append("file", file);
  const res = await apiFetch("/api/upload", { method: "POST", body: fd });
  if (!res.ok) throw new Error(`Upload failed (${res.status})`);
  const data = (await res.json()) as UploadResponse;
  const url = data.url ?? data.path;
  if (!url) throw new Error("Upload failed: missing url");
  return url;
}