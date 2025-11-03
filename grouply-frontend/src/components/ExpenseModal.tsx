import { useEffect, useState } from "react";
import { listUsers, createExpense } from "../api/expenses";

export default function ExpenseModal({ eventId, onClose, onSaved }: { eventId: number; onClose: () => void; onSaved: () => void; }) {
  const [users, setUsers] = useState<{ id: number; name: string }[]>([]);
  const [payerId, setPayerId] = useState<number | null>(null);
  const [amount, setAmount] = useState("");
  const [description, setDescription] = useState("");
  const [splitMode, setSplitMode] = useState<"EQUAL" | "RATIO" | "PERCENTAGE">("EQUAL");
  const [shares, setShares] = useState<{ userId: number; value?: string }[]>([]);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    listUsers().then(setUsers).catch((e) => console.error(e));
  }, []);

  useEffect(() => {
    // initialize participants to all users if empty
    if (users.length && shares.length === 0) {
      setShares(users.map(u => ({ userId: u.id })));
      setPayerId(users[0].id);
    }
  }, [users]);

  const toggleParticipant = (userId: number) => {
    setShares(s => s.some(x => x.userId === userId) ? s.filter(x => x.userId !== userId) : [...s, { userId }]);
  };

  const onSave = async () => {
    setError(null);
    if (!payerId) { setError("Choose payer"); return; }
    if (!amount || Number(amount) <= 0) { setError("Invalid amount"); return; }
    if (shares.length === 0) { setError("Add participants"); return; }
    try {
      setSaving(true);
      const payload = {
        eventId,
        payerId,
        amount: amount,
        description,
        splitMode,
        shares: shares.map(s => ({ userId: s.userId, value: s.value ? Number(s.value) : undefined })),
      };
      await createExpense(payload as any);
      onSaved();
      onClose();
    } catch (e) {
      setError((e as Error).message);
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-panel" onClick={(e) => e.stopPropagation()}>
        <div className="modal-content">
          <h3 className="modal-title">Add expense</h3>

          <label>
            <div>Payer</div>
            <select className="input" value={payerId ?? undefined} onChange={(e) => setPayerId(Number(e.target.value))}>
              {users.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
            </select>
          </label>

          <label>
            <div>Amount</div>
            <input className="input" value={amount} onChange={(e) => setAmount(e.target.value)} placeholder="12.50" />
          </label>

          <label>
            <div>Description</div>
            <input className="input" value={description} onChange={(e) => setDescription(e.target.value)} />
          </label>

          <label>
            <div>Split type</div>
            <select className="input" value={splitMode} onChange={(e) => setSplitMode(e.target.value as any)}>
              <option value="EQUAL">Equal</option>
              <option value="PERCENTAGE">Percentage</option>
              <option value="RATIO">Ratio</option>
            </select>
          </label>

          <div style={{ marginTop: 8 }}>
            <div style={{ marginBottom: 6 }}>Participants</div>
            <div style={{ display: "grid", gap: 8 }}>
              {users.map(u => {
                const sel = shares.find(s => s.userId === u.id);
                return (
                  <label key={u.id} style={{ display: "flex", gap: 8, alignItems: "center" }}>
                    <input type="checkbox" checked={!!sel} onChange={() => toggleParticipant(u.id)} />
                    <span style={{ flex: 1 }}>{u.name}</span>
                    {(splitMode === "PERCENTAGE" || splitMode === "RATIO") && sel && (
                      <input className="input" style={{ width: 120 }} placeholder={splitMode === "PERCENTAGE" ? "percent" : "ratio"}
                        value={sel.value ?? ""} onChange={(e) => setShares(s => s.map(x => x.userId === u.id ? { ...x, value: e.target.value } : x))} />
                    )}
                  </label>
                );
              })}
            </div>
          </div>

          {error && <div style={{ color: "tomato" }}>{error}</div>}

          <div className="modal-actions" style={{ marginTop: 12 }}>
            <button className="btn btn-muted" onClick={onClose}>Cancel</button>
            <button className="btn btn-primary" onClick={onSave} disabled={saving}>{saving ? "Saving..." : "Add Expense"}</button>
          </div>
        </div>
      </div>
    </div>
  );
}