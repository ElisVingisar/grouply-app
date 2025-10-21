// src/components/EventModal.tsx
import { useEffect, useState } from "react";
import type { EventItem } from "./EventCard";
import "./EventModal.css";

const apiBase = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

const toAbsolute = (u?: string) =>
    !u
        ? ""
        : u.startsWith("http://") || u.startsWith("https://")
            ? u
            : `${apiBase}${u.startsWith("/") ? "" : "/"}${u}`;

export default function EventModal({event, onClose, onSaved, onDeleted,}: {
    event: EventItem;
    onClose: () => void;
    onSaved: (updated: EventItem) => void;
    onDeleted: (id: number) => void;
    }) {
    const [title, setTitle] = useState(event.title ?? "");
    const [description, setDescription] = useState(event.description ?? "");
    const [dateTime, setDateTime] = useState(event.dateTime?.slice(0, 16) ?? "");
    const [location, setLocation] = useState(event.location ?? "");
    const [capacity, setCapacity] = useState<number>(event.capacity ?? 1);
    const [imageUrl, setImageUrl] = useState(event.imageUrl ?? "");

    const [saving, setSaving] = useState(false);
    const [deleting, setDeleting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const [previewAbs, setPreviewAbs] = useState<string | null>(
        event.imageUrl ? toAbsolute(event.imageUrl) : null
    );

    const [dragOver, setDragOver] = useState(false);
    const [uploading, setUploading] = useState(false);

    useEffect(() => {
        const onKey = (e: KeyboardEvent) => {
            if (e.key === "Escape") onClose();
        };
        window.addEventListener("keydown", onKey);
        return () => window.removeEventListener("keydown", onKey);
    }, [onClose]);

    useEffect(() => {
        const { overflow } = document.body.style;
        document.body.style.overflow = "hidden";
        return () => {
            document.body.style.overflow = overflow;
        };
    }, []);

    const normalizeDateTime = (dt: string) =>
        dt && dt.length === 16 ? dt + ":00" : dt;

    const uploadFile = async (file: File) => {
        if (!file.type.startsWith("image/")) {
            alert("Please select an image file (jpg, png, webp...)");
            return;
        }
        if (file.size > 10 * 1024 * 1024) {
            alert("File is too large (max 10MB)");
            return;
        }

        setUploading(true);
        try {
            const form = new FormData();
            form.append("file", file);

            const res = await fetch(`${apiBase}/api/uploads`, {
                method: "POST",
                body: form,
            });
            if (!res.ok) throw new Error(`Upload failed: ${res.status}`);

            const data = (await res.json()) as { url: string };
            setImageUrl(data.url);
            setPreviewAbs(toAbsolute(data.url));
        } catch (err) {
            console.error(err);
            alert("Upload failed");
        } finally {
            setUploading(false);
        }
    };

    const onPickFile = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (!file) return;
        await uploadFile(file);
    };

    const handleDroppedFiles = async (files: FileList | null) => {
        if (!files || !files[0]) return;
        await uploadFile(files[0]);
    };

    const onSave = async () => {
        setError(null);
        if (!title.trim()) {
            setError("Title is required");
            return;
        }
        setSaving(true);
        try {
            const payload: Partial<EventItem> = {
                id: event.id,
                title: title.trim(),
                description: description.trim(),
                dateTime: normalizeDateTime(dateTime),
                location: location.trim(),
                capacity: Number(capacity) || 0,
                imageUrl: imageUrl?.trim() || undefined,
            };

            const res = await fetch(`${apiBase}/api/events/${event.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });
            if (!res.ok) throw new Error(`PUT failed: ${res.status}`);

            const updated = (await res.json()) as EventItem;
            onSaved(updated);
            onClose();
        } catch (e) {
            setError(e instanceof Error ? e.message : "Save failed");
        } finally {
            setSaving(false);
        }
    };

    const onDelete = async () => {
        const ok = window.confirm("Delete this event? This action cannot be undone.");
        if (!ok) return;

        setDeleting(true);
        try {
            const res = await fetch(`${apiBase}/api/events/${event.id}`, {
                method: "DELETE",
            });
            if (!res.ok) throw new Error(`DELETE failed: ${res.status}`);

            onDeleted(event.id);
            onClose();
        } catch (e) {
            setError(e instanceof Error ? e.message : "Delete failed");
        } finally {
            setDeleting(false);
        }
    };

    return (
        <div className="modal-overlay" onClick={onClose} aria-hidden="true">
            <div
                className="modal-panel"
                role="dialog"
                aria-modal="true"
                aria-labelledby="event-modal-title"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="modal-scroller">
                    <div className="modal-content">
                        <div
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                gap: 8,
                                marginBottom: 12,
                            }}
                        >
                            <h2 id="event-modal-title" className="modal-title">
                                Edit event
                            </h2>
                            <button className="btn btn-muted" onClick={onClose} aria-label="Close">
                                ✕
                            </button>
                        </div>

                        <div className="modal-body">
                            <label>
                                <div>Title *</div>
                                <input className="input" value={title} onChange={(e) => setTitle(e.target.value)} required />
                            </label>

                            <label>
                                <div>Description</div>
                                <textarea className="textarea" value={description} onChange={(e) => setDescription(e.target.value)} rows={3} />
                            </label>

                            <label>
                                <div>Date & time</div>
                                <input className="input" type="datetime-local" value={dateTime} onChange={(e) => setDateTime(e.target.value)} />
                            </label>

                            <label>
                                <div>Location</div>
                                <input className="input" value={location} onChange={(e) => setLocation(e.target.value)} />
                            </label>

                            <label>
                                <div>Capacity</div>
                                <input className="input" type="number" min={1} value={capacity} onChange={(e) => setCapacity(Number(e.target.value))} />
                            </label>

                            <div>
                                <div style={{ marginBottom: 6 }}>Image (optional)</div>

                                <div
                                    className={`dropzone ${dragOver ? "dropzone--active" : ""}`}
                                    onDragOver={(e) => { e.preventDefault(); setDragOver(true); }}
                                    onDragLeave={() => setDragOver(false)}
                                    onDrop={async (e) => { e.preventDefault(); setDragOver(false); await handleDroppedFiles(e.dataTransfer.files); }}
                                    onClick={() => document.getElementById("modal-file-input")?.click()}
                                    role="button"
                                    tabIndex={0}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter" || e.key === " ") {
                                            e.preventDefault();
                                            document.getElementById("modal-file-input")?.click();
                                        }
                                    }}
                                >
                                    {uploading ? "Uploading..." : "Drag image here or click"}
                                </div>

                                <input
                                    id="modal-file-input"
                                    type="file"
                                    accept="image/*"
                                    onChange={onPickFile}
                                    disabled={uploading}
                                    style={{ display: "none" }}
                                />

                                {previewAbs && !uploading && (
                                    <div style={{ marginTop: 8 }}>
                                        <img src={previewAbs} alt="Preview" style={{ maxWidth: "100%", borderRadius: 8 }} />
                                    </div>
                                )}
                            </div>
                        </div>

                        {error && <div style={{ color: "tomato" }}>{error}</div>}
                        <div className="modal-actions" style={{ marginTop: 16 }}>
                            <button className="btn btn-danger" onClick={onDelete} disabled={deleting}>
                                {deleting ? "Deleting..." : "Delete"}
                            </button>
                            <button className="btn btn-primary" onClick={onSave} disabled={saving}>
                                {saving ? "Saving..." : "Save changes"}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}