export type EventItem = {
    id: number;
    title: string;
    description: string;
    dateTime: string;
    location: string;
    capacity?: number;
    imageUrl?: string;
};

const fmt = (iso: string) =>
    new Date(iso).toLocaleString([], {dateStyle: "medium", timeStyle: "short"});

const apiBase = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

function toAbsolute(u?: string) {
    if (!u) return undefined;
    if (u.startsWith("http://") || u.startsWith("https://")) return u;
    return `${apiBase}${u.startsWith("/") ? "" : "/"}${u}`;
}

export default function EventCard({event, onClick,}: {
    event: EventItem;
    onClick?: () => void;
}) {
    const absoluteImg = toAbsolute(event.imageUrl);
    const img =
        absoluteImg && absoluteImg.trim().length > 0
            ? absoluteImg
            : `https://picsum.photos/seed/${event.id}/600/300`;

    return (
        <article
            className="card"
            onClick={onClick}
            style={{ cursor: onClick ? "pointer" : "default" }}
        >
            <div className="card-image">
                <img src={img} alt={event.title} loading="lazy" />
            </div>
            <div className="card-body">
                <h3 className="card-title">{event.title}</h3>
                <p className="card-meta">
                    <span>{fmt(event.dateTime)}</span> • <span>{event.location}</span> •{" "}
                    <span>Capacity: {event.capacity}</span>
                </p>
                <p className="card-desc">{event.description}</p>
            </div>
        </article>
    );
}
