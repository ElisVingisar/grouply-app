import { useEffect, useState } from "react";
import { createExpense, updateExpense, listUsers } from "../api/expenses";

export default function ExpenseModal({
  eventId,
  expense,
  onClose,
  onSaved,
}: {
  eventId: number;
  expense?: any;
  onClose: () => void;
  onSaved: () => void;
}) {
  const isEdit = !!expense;

  const [users, setUsers] = useState<{ id: number; name: string }[]>([]);
  const [payerId, setPayerId] = useState<number | null>(expense?.payerId || null);
  const [amount, setAmount] = useState(expense?.amount || "");
  const [description, setDescription] = useState(expense?.description || "");
  const [splitMode, setSplitMode] = useState(expense?.splitMode || "EQUAL");
  const [shares, setShares] = useState<{ userId: number; value: number | undefined; selected: boolean }[]>([]);

  useEffect(() => {
    let cancel = false;
    listUsers(eventId).then((data) => {
      if (cancel) return;
      setUsers(data);

      if (isEdit && expense.shares) {
        // Edit mode: load existing shares (only users in the expense)
        const existingUserIds = expense.shares.map((s: any) => s.userId);
        setShares(
          data.map((u) => ({
            userId: u.id,
            value: expense.shares.find((s: any) => s.userId === u.id)?.value,
            selected: existingUserIds.includes(u.id), // Only select users who were in the original expense
          }))
        );
      } else {
        // Create mode: auto-select first user as payer, all users selected by default
        if (data.length > 0 && !payerId) {
          setPayerId(data[0].id);
        }
        setShares(data.map((u) => ({ userId: u.id, value: undefined, selected: true })));
      }
    });
    return () => {
      cancel = true;
    };
  }, [eventId, isEdit, expense]);

  const handleSave = async () => {
    if (!payerId || !amount) {
      alert("Please fill payer and amount");
      return;
    }

    const selectedShares = shares.filter((s) => s.selected);
    
    if (selectedShares.length === 0) {
      alert("Please select at least one person to split the expense with");
      return;
    }

    // Validate shares based on split mode
    if (splitMode === "PERCENTAGE") {
      const total = selectedShares.reduce((sum, s) => sum + (s.value || 0), 0);
      if (Math.abs(total - 100) > 0.01) {
        alert("Percentages must add up to 100%");
        return;
      }
    }

    if (splitMode === "EXACT") {
      const total = selectedShares.reduce((sum, s) => sum + (s.value || 0), 0);
      if (Math.abs(total - parseFloat(amount)) > 0.01) {
        alert(`Exact amounts must add up to ${amount} €`);
        return;
      }
    }

    const body = {
      payerId,
      amount: parseFloat(amount),
      description: description || "",
      splitMode,
      shares: selectedShares.map((s) => ({ userId: s.userId, value: s.value || 0 })),
    };

    try {
      if (isEdit) {
        await updateExpense(eventId, expense.id, body);
      } else {
        await createExpense(eventId, body);
      }
      onSaved();
      onClose();
    } catch (e) {
      console.error("Failed to save expense:", e);
      alert("Failed to save expense");
    }
  };

  const toggleUserSelection = (userId: number) => {
    setShares((prev) =>
      prev.map((s) => (s.userId === userId ? { ...s, selected: !s.selected } : s))
    );
  };

  const selectedCount = shares.filter((s) => s.selected).length;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <h2 style={{ margin: 0, marginBottom: "var(--space-md)" }}>
          {isEdit ? "Edit Expense" : "Add Expense"}
        </h2>

        <div className="modal-body" style={{ paddingTop: 0, paddingBottom: 0 }}>
          <div style={{ display: "grid", gap: "var(--space-xs)" }}>
            <div className="field">
              <label>Payer</label>
              <select className="input" value={payerId || ""} onChange={(e) => setPayerId(Number(e.target.value))}>
                <option value="">Choose payer</option>
                {users.map((u) => (
                  <option key={u.id} value={u.id}>
                    {u.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="field">
              <label>Amount (€)</label>
              <input
                className="input"
                type="number"
                step="0.01"
                value={amount}
                onChange={(e) => setAmount(e.target.value)}
                placeholder="0.00"
              />
            </div>

            <div className="field">
              <label>Description (optional)</label>
              <input
                className="input"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="What was this expense for?"
              />
            </div>

            <div className="field">
              <label>Split Mode</label>
              <select className="input" value={splitMode} onChange={(e) => setSplitMode(e.target.value)}>
                <option value="EQUAL">Equal split</option>
                <option value="PERCENTAGE">By percentage</option>
                <option value="EXACT">Exact amounts</option>
              </select>
            </div>

            {/* User Selection */}
            <div className="field">
              <label>Split between ({selectedCount} selected)</label>
              <div style={{ 
                background: "var(--color-surface-alt)", 
                borderRadius: "var(--radius-md)", 
                padding: "var(--space-sm)",
                display: "grid",
                gap: "var(--space-sm)"
              }}>
                {shares.map((s) => {
                  const user = users.find((u) => u.id === s.userId);
                  return (
                    <label
                      key={s.userId}
                      style={{
                        display: "flex",
                        alignItems: "center",
                        gap: "var(--space-sm)",
                        padding: "var(--space-xs)",
                        paddingLeft: "var(--space-sm)",
                        background: s.selected ? "var(--color-primary-subtle)" : "transparent",
                        borderRadius: "var(--radius-sm)",
                        cursor: "pointer",
                        transition: "var(--transition)",
                      }}
                    >
                      <input
                        type="checkbox"
                        checked={s.selected}
                        onChange={() => toggleUserSelection(s.userId)}
                        style={{ cursor: "pointer" }}
                      />
                      <span style={{ fontWeight: s.selected ? 500 : 400 }}>{user?.name}</span>
                    </label>
                  );
                })}
              </div>
            </div>

            {/* Share Values (only for selected users) */}
            {splitMode !== "EQUAL" && (
              <div className="field">
                <label>
                  {splitMode === "PERCENTAGE" ? "Percentage for each person" : "Exact amount for each person"}
                </label>
                <div style={{ display: "grid", gap: "var(--space-sm)" }}>
                  {shares
                    .filter((s) => s.selected)
                    .map((s) => { // removed unused idx
                      const user = users.find((u) => u.id === s.userId);
                      return (
                        <div
                          key={s.userId}
                          style={{
                            display: "grid",
                            gridTemplateColumns: "1fr auto",
                            gap: "var(--space-md)",
                            alignItems: "center",
                          }}
                        >
                          <span style={{ fontWeight: 500 }}>{user?.name}</span>
                          <div style={{ display: "flex", alignItems: "center", gap: "var(--space-xs)" }}>
                            <input
                              className="input"
                              style={{ width: "100px" }}
                              type="number"
                              step="0.01"
                              value={s.value || ""}
                              placeholder="0"
                              onChange={(e) => {
                                const newShares = [...shares];
                                const shareIdx = shares.findIndex((share) => share.userId === s.userId);
                                newShares[shareIdx].value = e.target.value ? parseFloat(e.target.value) : undefined;
                                setShares(newShares);
                              }}
                            />
                            <span style={{ fontSize: "0.875rem", color: "var(--color-text-muted)" }}>
                              {splitMode === "PERCENTAGE" ? "%" : "€"}
                            </span>
                          </div>
                        </div>
                      );
                    })}
                </div>
                
                {/* Show total for validation */}
                {splitMode === "PERCENTAGE" && (
                  <div style={{ 
                    marginTop: "var(--space-sm)", 
                    fontSize: "0.875rem", 
                    color: "var(--color-text-muted)",
                    textAlign: "right"
                  }}>
                    Total: {shares.filter(s => s.selected).reduce((sum, s) => sum + (s.value || 0), 0).toFixed(1)}%
                    {Math.abs(shares.filter(s => s.selected).reduce((sum, s) => sum + (s.value || 0), 0) - 100) > 0.01 && 
                      <span style={{ color: "var(--color-danger)", marginLeft: "var(--space-xs)" }}>
                        (must equal 100%)
                      </span>
                    }
                  </div>
                )}
                
                {splitMode === "EXACT" && (
                  <div style={{ 
                    marginTop: "var(--space-sm)", 
                    fontSize: "0.875rem", 
                    color: "var(--color-text-muted)",
                    textAlign: "right"
                  }}>
                    Total: {shares.filter(s => s.selected).reduce((sum, s) => sum + (s.value || 0), 0).toFixed(2)} €
                    {Math.abs(shares.filter(s => s.selected).reduce((sum, s) => sum + (s.value || 0), 0) - parseFloat(amount || "0")) > 0.01 && 
                      <span style={{ color: "var(--color-danger)", marginLeft: "var(--space-xs)" }}>
                        (must equal {amount} €)
                      </span>
                    }
                  </div>
                )}
              </div>
            )}
          </div>
        </div>

        <div className="modal-actions" style={{ paddingTop: "var(--space-sm)", marginTop: 0 }}>
          <button className="btn btn-secondary" onClick={onClose}>
            Cancel
          </button>
          <button className="btn btn-primary" onClick={handleSave}>
            {isEdit ? "Save Changes" : "Add Expense"}
          </button>
        </div>
      </div>
    </div>
  );
}