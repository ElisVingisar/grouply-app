import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { listEvents } from "../api/expenses";
import EventModal from "../components/EventModal";
import "./EventsPage.css";

type EventDTO = {
  id: number;
  title: string;
  description?: string;
  dateTime?: string;
  location?: string;
  capacity?: number;
  imageUrl?: string | null;
};

const apiBase = (import.meta.env.VITE_API_BASE || "http://localhost:8080").replace(/\/+$/,"");

export default function EventsPage() {
  const [events, setEvents] = useState<EventDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedEvent, setSelectedEvent] = useState<EventDTO | null>(null);
  const navigate = useNavigate();

  const loadEvents = async () => {
    try {
      setLoading(true);
      const data = await listEvents();
      setEvents(data);
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadEvents();
  }, []);

  const handleEventClick = (event: EventDTO) => {
    setSelectedEvent(event);
  };

  const handleEventSaved = (updated: any) => {
    setEvents(prev => prev.map(e => e.id === updated.id ? { ...e, ...updated } : e));
    setSelectedEvent(null);
  };

  const handleEventDeleted = (id: number) => {
    setEvents(prev => prev.filter(e => e.id !== id));
    setSelectedEvent(null);
  };

  if (loading) return <div className="page-loading">Loading events...</div>;
  if (error) return <div className="page-error">Error: {error}</div>;

  return (
    <>
      <div className="events-page">
        <div className="events-header">
          <h1>My Events</h1>
          <button 
            className="btn btn-accent" 
            onClick={() => navigate("/events/new")}
          >
            + Add Event
          </button>
        </div>

        {events.length === 0 ? (
          <div className="empty-state">
            <p>No events yet. Create your first event!</p>
            <button 
              className="btn btn-accent" 
              onClick={() => navigate("/events/new")}
            >
              Create Event
            </button>
          </div>
        ) : (
          <div className="events-grid">
            {events.map(event => (
              <div 
                key={event.id} 
                className="event-card"
                onClick={() => handleEventClick(event)}
              >
                {event.imageUrl && (
                  <div
                    className="event-card__image"
                    style={{ backgroundImage: `url(${event.imageUrl.startsWith("http") ? event.imageUrl : apiBase + event.imageUrl})` }}
                  />
                )}
                <div className="event-card__content">
                  <h3 className="event-card__title">{event.title}</h3>
                  {event.description && (
                    <p className="event-card__description">{event.description}</p>
                  )}
                  <div className="event-card__meta">
                    {event.dateTime && (
                      <div className="event-card__date">
                        📅 {new Date(event.dateTime).toLocaleDateString()} at{" "}
                        {new Date(event.dateTime).toLocaleTimeString([], { 
                          hour: "2-digit", 
                          minute: "2-digit" 
                        })}
                      </div>
                    )}
                    {event.location && (
                      <div className="event-card__location">📍 {event.location}</div>
                    )}
                    {event.capacity && (
                      <div className="event-card__capacity">👥 Max {event.capacity} people</div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {selectedEvent && (
        <EventModal
          event={selectedEvent as any}
          onClose={() => setSelectedEvent(null)}
          onSaved={handleEventSaved}
          onDeleted={handleEventDeleted}
        />
      )}
    </>
  );
}