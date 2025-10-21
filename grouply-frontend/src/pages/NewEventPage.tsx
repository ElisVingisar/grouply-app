import { useState } from "react";
import { useNavigate } from "react-router-dom";
import "./NewEventPage.css";

const apiBase = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export default function NewEventPage() {
    const [title, setTitle] = useState("");
    const [description, setDescription] = useState("");
    const [dateTime, setDateTime] = useState(""); // YYYY-MM-DDTHH:mm
    const [location, setLocation] = useState("");
    const [capacity, setCapacity] = useState<number>(1);

    const [saving, setSaving] = useState(false);
    const [error, setError] = useState<string | null>(null);

    // image
    const [imageUrl, setImageUrl] = useState("");
    const [uploading, setUploading] = useState(false);
    const [previewAbs, setPreviewAbs] = useState<string | null>(null);
    const [dragOver, setDragOver] = useState(false);

    const navigate = useNavigate();

    const normalizeDateTime = (dt: string) =>
        dt && dt.length === 16 ? dt + ":00" : dt;

    const toAbsolute = (u?: string) =>
        !u
            ? ""
            : u.startsWith("http://") || u.startsWith("https://")
                ? u
                : `${apiBase}${u.startsWith("/") ? "" : "/"}${u}`;

    // upload (POST /api/uploads)
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

    const onSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!title.trim()) {
            setError("Title is required");
            return;
        }
        setSaving(true);
        try {
            const payload: Record<string, unknown> = {
                title: title.trim(),
                description: description.trim(),
                dateTime: normalizeDateTime(dateTime),
                location: location.trim(),
                capacity: Number(capacity) || 0,
            };
            if (imageUrl.trim()) payload.imageUrl = imageUrl.trim();

            const res = await fetch(`${apiBase}/api/events`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(payload),
            });
            if (!res.ok) throw new Error(`POST failed: ${res.status}`);
            navigate("/");
        } catch (e) {
            setError(e instanceof Error ? e.message : "Save failed");
        } finally {
            setSaving(false);
        }
    };

    return (
        <section className="page page--narrow">
            <h1 className="page-title">Add event</h1>

            <form className="form-card" onSubmit={onSubmit}>
                <div className="form-grid">
                    <label className="field">
                        <span className="field__label">Title *</span>
                        <input
                            className="input"
                            value={title}
                            onChange={(e) => setTitle(e.target.value)}
                            required
                        />
                    </label>

                    <label className="field">
                        <span className="field__label">Description</span>
                        <textarea
                            className="textarea"
                            value={description}
                            onChange={(e) => setDescription(e.target.value)}
                            rows={3}
                        />
                    </label>

                    <label className="field">
                        <span className="field__label">Date & time</span>
                        <input
                            className="input"
                            type="datetime-local"
                            value={dateTime}
                            onChange={(e) => setDateTime(e.target.value)}
                        />
                    </label>

                    <label className="field">
                        <span className="field__label">Location</span>
                        <input
                            className="input"
                            value={location}
                            onChange={(e) => setLocation(e.target.value)}
                        />
                    </label>

                    <label className="field">
                        <span className="field__label">Capacity</span>
                        <input
                            className="input"
                            type="number"
                            min={1}
                            value={capacity}
                            onChange={(e) => setCapacity(Number(e.target.value))}
                        />
                    </label>

                    {/* Image */}
                    <div className="field">
                        <span className="field__label">Image (optional)</span>

                        <div
                            className={`dropzone ${dragOver ? "dropzone--active" : ""}`}
                            onDragOver={(e) => {
                                e.preventDefault();
                                setDragOver(true);
                            }}
                            onDragLeave={() => setDragOver(false)}
                            onDrop={async (e) => {
                                e.preventDefault();
                                setDragOver(false);
                                await handleDroppedFiles(e.dataTransfer.files);
                            }}
                            onClick={() => document.getElementById("file-input-hidden")?.click()}
                            role="button"
                            tabIndex={0}
                            onKeyDown={(e) => {
                                if (e.key === "Enter" || e.key === " ") {
                                    e.preventDefault();
                                    document.getElementById("file-input-hidden")?.click();
                                }
                            }}
                        >
                            {uploading
                                ? "Uploading…"
                                : dragOver
                                    ? "Drop to upload"
                                    : "Drag image here or click"}
                        </div>

                        <input
                            id="file-input-hidden"
                            type="file"
                            accept="image/*"
                            onChange={onPickFile}
                            disabled={uploading}
                            style={{ display: "none" }}
                        />

                        {previewAbs && !uploading && (
                            <div className="image-preview">
                                <img src={previewAbs} alt="Preview" />
                            </div>
                        )}
                    </div>
                </div>

                {error && <div className="form-error">{error}</div>}

                <div className="form-actions">
                    <button className="btn btn-primary" type="submit" disabled={saving}>
                        {saving ? "Saving..." : "Create"}
                    </button>
                    <button
                        className="btn btn-muted"
                        type="button"
                        onClick={() => navigate("/")}
                    >
                        Cancel
                    </button>
                </div>
            </form>
        </section>
    );
}