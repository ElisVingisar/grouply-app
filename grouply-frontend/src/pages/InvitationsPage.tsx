import { useEffect, useState } from "react";
import { listPendingInvitations, acceptInvitation, declineInvitation } from "../api/expenses";

type Invitation = {
  id: number;
  eventId: number;
  eventTitle: string;
  invitedBy: string;
  message?: string;
  sentAt: string;
};

export default function InvitationsPage() {
  const [invitations, setInvitations] = useState<Invitation[]>([]);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    try {
      const data = await listPendingInvitations();
      setInvitations(data);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { load(); }, []);

  const handleAccept = async (id: number) => {
    await acceptInvitation(id);
    load();
  };
  const handleDecline = async (id: number) => {
    await declineInvitation(id);
    load();
  };

  return (
    <div style={{ maxWidth: 640, margin: "32px auto", padding: 16 }}>
      <h1>My Invitations</h1>
      {loading && <div>Loading…</div>}
      {!loading && invitations.length === 0 && <div>No pending invitations.</div>}
      <ul style={{ listStyle: "none", padding: 0, margin: 0 }}>
        {invitations.map(inv => (
          <li key={inv.id} style={{
            border: "1px solid #ddd",
            borderRadius: 8,
            padding: 12,
            marginBottom: 12,
            background: "#fff"
          }}>
            <div style={{ fontWeight: 600 }}>{inv.eventTitle}</div>
            <div style={{ fontSize: 13, color: "#555" }}>Invited by {inv.invitedBy}</div>
            {inv.message && <div style={{ marginTop: 4 }}>{inv.message}</div>}
            <div style={{ marginTop: 8, display: "flex", gap: 8 }}>
              <button className="btn btn-primary" onClick={() => handleAccept(inv.id)}>Accept</button>
              <button className="btn btn-danger" onClick={() => handleDecline(inv.id)}>Decline</button>
            </div>
          </li>
        ))}
      </ul>
    </div>
  );
}