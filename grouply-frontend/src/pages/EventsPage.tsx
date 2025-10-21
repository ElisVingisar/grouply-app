import { useEffect, useState } from "react";
import EventCard, { type EventItem } from "../components/EventCard";
import EventModal from "../components/EventModal.tsx";

const apiBase = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export default function EventsPage() {
    const [events, setEvents] = useState<EventItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [selected, setSelected] = useState<EventItem | null>(null);

    const load = () => {
        setLoading(true);
        fetch(`${apiBase}/api/events`)
            .then((r) => r.json())
            .then((data) => setEvents(data))
            .catch((e) => console.error("Fetch error:", e))
            .finally(() => setLoading(false));
    };

    useEffect(() => {
        load();
    }, []);

    const onCardClick = (e: EventItem) => setSelected(e);

    const handleSaved = (updated: EventItem) => {
        // optimistlik uuendus: vaheta listis välja
        setEvents((old) => old.map((x) => (x.id === updated.id ? updated : x)));
    };

    const handleDeleted = (id: number) => {
        setEvents((old) => old.filter((x) => x.id !== id));
    };

    return (
        <div style={{ padding: "1rem" }}>
            {loading && <p>Loading events...</p>}

            <div className="cards-grid">
                {events.map((e) => (
                    <EventCard key={e.id} event={e} onClick={() => onCardClick(e)} />
                ))}
            </div>

            {selected && (
                <EventModal
                    event={selected}
                    onClose={() => setSelected(null)}
                    onSaved={handleSaved}
                    onDeleted={handleDeleted}
                />
            )}
        </div>
    );
}