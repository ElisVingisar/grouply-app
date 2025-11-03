export type ShareDTO = { userId: number; value?: number };
export type ExpenseCreateDTO = {
  eventId: number;
  payerId: number;
  amount: string;
  description?: string;
  splitMode: "EQUAL" | "RATIO" | "PERCENTAGE";
  shares: ShareDTO[];
};

const apiBase = import.meta.env.VITE_API_BASE ?? "http://localhost:8080";

export async function listExpenses(eventId: number) {
  const res = await fetch(`${apiBase}/api/events/${eventId}/expenses`);
  if (!res.ok) throw new Error("Failed to fetch expenses");
  return res.json();
}

export async function createExpense(dto: ExpenseCreateDTO) {
  const res = await fetch(`${apiBase}/api/expenses`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(dto),
  });
  if (!res.ok) throw new Error("Failed to create expense");
  return res.json();
}

export async function listUsers() {
  const res = await fetch(`${apiBase}/api/users`);
  if (!res.ok) throw new Error("Failed to fetch users");
  return res.json();
}

export async function listBalances(eventId: number) {
  const res = await fetch(`${apiBase}/api/events/${eventId}/balances`);
  if (!res.ok) throw new Error("Failed to fetch balances");
  return res.json();
}

export async function suggestedSettlements(eventId: number) {
  const res = await fetch(`${apiBase}/api/events/${eventId}/settlements/suggested`);
  if (!res.ok) throw new Error("Failed to fetch settlements");
  return res.json();
}

export async function postPayment(payload: { eventId: number; fromUserId: number; toUserId: number; amount: string }) {
  const res = await fetch(`${apiBase}/api/payments`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  if (!res.ok) throw new Error("Failed to post payment");
  return res.json();
}