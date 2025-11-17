import { apiFetch } from "./client";

// Event invitations
export async function sendInvitation(eventId: number, email: string, message?: string) {
  const res = await apiFetch(`/api/events/${eventId}/invitations`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, message }),
  });
  if (!res.ok) throw new Error("Failed to send invitation");
}

export async function listEventInvitations(eventId: number) {
  const res = await apiFetch(`/api/events/${eventId}/invitations`);
  if (!res.ok) throw new Error("Failed to load event invitations");
  return res.json() as Promise<Array<{ id: number; email: string; status: string; sentAt: string; respondedAt?: string; userName?: string }>>;
}

export async function listPendingInvitations() {
  const res = await apiFetch("/api/invitations/pending");
  if (!res.ok) throw new Error("Failed to load invitations");
  return res.json();
}

export async function acceptInvitation(id: number) {
  const res = await apiFetch(`/api/invitations/${id}/accept`, { method: "POST" });
  if (!res.ok) throw new Error("Failed to accept");
}

export async function declineInvitation(id: number) {
  const res = await apiFetch(`/api/invitations/${id}/decline`, { method: "POST" });
  if (!res.ok) throw new Error("Failed to decline");
}

export type ShareDTO = { userId: number; value?: number };
export type ExpenseCreateDTO = {
  eventId: number;
  payerId: number;
  amount: string;
  description?: string;
  splitMode: "EQUAL" | "EXACT" | "PERCENTAGE"; // changed from RATIO
  shares: ShareDTO[];
};

export async function listEvents() {
  const res = await apiFetch("/api/events");
  if (!res.ok) throw new Error("Failed to fetch events");
  return res.json();
}

export async function createEvent(data: any) {
  const res = await apiFetch("/api/events", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create event");
  return res.json();
}

export async function updateEvent(eventId: number, data: any) {
  const res = await apiFetch(`/api/events/${eventId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to update event");
  return res.json();
}

export async function deleteEvent(eventId: number) {
  const res = await apiFetch(`/api/events/${eventId}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to delete event");
}

// Change listUsers to accept eventId parameter
export async function listUsers(eventId: number) {
  const res = await apiFetch(`/api/events/${eventId}/participants`);
  if (!res.ok) throw new Error("Failed to load participants");
  return res.json() as Promise<Array<{ id: number; name: string; email: string }>>;
}

export async function createExpense(eventId: number, data: any) {
  const res = await apiFetch(`/api/events/${eventId}/expenses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  if (!res.ok) throw new Error("Failed to create expense");
  return res.json();
}

export async function listExpenses(eventId: number) {
  const res = await apiFetch(`/api/events/${eventId}/expenses`);
  if (!res.ok) throw new Error("Failed to fetch expenses");
  return res.json();
}

export async function getBalances(eventId: number) {
  const res = await apiFetch(`/api/events/${eventId}/balances`);
  if (!res.ok) throw new Error("Failed to fetch balances");
  return res.json();
}

export async function listBalances(eventId: number) {
  return getBalances(eventId);
}

export async function suggestedSettlements(eventId: number) {
  const res = await apiFetch(`/api/events/${eventId}/settlements/suggested`);
  if (!res.ok) throw new Error("Failed to fetch settlements");
  return res.json();
}

export async function postPayment(payload: { eventId: number; fromUserId: number; toUserId: number; amount: string }) {
  const res = await apiFetch(`/api/payments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error("Failed to post payment");
  return res.json();
}

export async function listPayments(eventId: number) {
  const res = await apiFetch(`/api/events/${eventId}/payments`);
  if (!res.ok) throw new Error("Failed to fetch payments");
  return res.json() as Promise<Array<{
    id: number;
    fromUserId: number;
    fromUserName: string;
    toUserId: number;
    toUserName: string;
    amount: string;
    createdAt: string;
    settled: boolean;
  }>>;
}

export async function updateExpense(eventId: number, expenseId: number, body: any) {
  const res = await apiFetch(`/api/events/${eventId}/expenses/${expenseId}`, {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  if (!res.ok) throw new Error("Failed to update expense");
  return res.json();
}

export async function deleteExpense(eventId: number, expenseId: number) {
  const res = await apiFetch(`/api/events/${eventId}/expenses/${expenseId}`, {
    method: "DELETE",
  });
  if (!res.ok) throw new Error("Failed to delete expense");
}