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
          <div style={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            marginBottom: 24
          }}>
            <h2 className="modal-title">Add expense</h2>
            <button className="btn btn-icon" onClick={onClose} aria-label="Close">
              ✕
            </button>
          </div>

          <div className="form-grid">
            <div className="field">
              <label className="field__label">Payer</label>
              <select 
                className="input" 
                value={payerId ?? undefined} 
                onChange={(e) => setPayerId(Number(e.target.value))}
              >
                {users.map(u => (
                  <option key={u.id} value={u.id}>{u.name}</option>
                ))}
              </select>
            </div>

            <div className="field">
              <label className="field__label">Amount</label>
              <input 
                className="input" 
                value={amount} 
                onChange={(e) => setAmount(e.target.value)} 
                placeholder="12.50"
                type="number"
                step="0.01"
                min="0"
              />
            </div>

            <div className="field">
              <label className="field__label">Description</label>
              <input 
                className="input" 
                value={description} 
                onChange={(e) => setDescription(e.target.value)}
                placeholder="What was this expense for?"
              />
            </div>

            <div className="field">
              <label className="field__label">Split type</label>
              <select 
                className="input" 
                value={splitMode} 
                onChange={(e) => setSplitMode(e.target.value as any)}
              >
                <option value="EQUAL">Equal split</option>
                <option value="PERCENTAGE">Split by percentage</option>
                <option value="RATIO">Split by ratio</option>
              </select>
            </div>

            <div className="field">
              <label className="field__label">Participants</label>
              <div className="participants-grid">
                {users.map(u => {
                  const sel = shares.find(s => s.userId === u.id);
                  return (
                    <label 
                      key={u.id} 
                      className="participant-row"
                      style={{
                        display: "flex",
                        gap: 12,
                        alignItems: "center",
                        padding: "8px 12px",
                        background: "var(--card-bg)",
                        borderRadius: "8px",
                        border: "1px solid var(--card-border-color)"
                      }}
                    >
                      <input 
                        type="checkbox" 
                        checked={!!sel} 
                        onChange={() => toggleParticipant(u.id)}
                      />
                      <span style={{ flex: 1 }}>{u.name}</span>
                      {(splitMode === "PERCENTAGE" || splitMode === "RATIO") && sel && (
                        <input 
                          className="input" 
                          style={{ width: 100 }} 
                          placeholder={splitMode === "PERCENTAGE" ? "%" : "ratio"}
                          value={sel.value ?? ""} 
                          onChange={(e) => setShares(s => 
                            s.map(x => x.userId === u.id ? 
                              { ...x, value: e.target.value } : x
                            )
                          )} 
                        />
                      )}
                    </label>
                  );
                })}
              </div>
            </div>
          </div>

          {error && (
            <div className="form-error" style={{ marginTop: 16 }}>
              {error}
            </div>
          )}

          <div className="form-actions">
            <button 
              className="btn" 
              onClick={onClose}
            >
              Cancel
            </button>
            <button 
              className="btn btn-primary" 
              onClick={onSave} 
              disabled={saving}
            >
              {saving ? "Saving..." : "Add Expense"}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}