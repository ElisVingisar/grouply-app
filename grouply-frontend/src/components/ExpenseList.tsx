import { useEffect, useState } from "react";
import { listExpenses } from "../api/expenses";
import ExpenseModal from "./ExpenseModal";
import { deleteExpense } from "../api/expenses";

type Props = {
  eventId: number;
  onExpenseAdded?: () => void;
};

export default function ExpenseList({ eventId, onExpenseAdded }: Props) {
  const [expenses, setExpenses] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [editingExpense, setEditingExpense] = useState<any>(null);

  const load = () => {
    setLoading(true);
    listExpenses(eventId)
      .then((d) => setExpenses(d ?? []))
      .catch((e) => {
        console.error("Failed to load expenses:", e);
        setExpenses([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load();
  }, [eventId]);

  const handleDelete = async (expenseId: number) => {
    if (!confirm("Delete this expense?")) return;
    try {
      await deleteExpense(eventId, expenseId);
      load();
    } catch (e) {
      console.error("Failed to delete:", e);
      alert("Failed to delete expense");
    }
  };

  // Helper to get badge class based on split mode
  const getBadgeClass = (mode: string) => {
    switch (mode?.toUpperCase()) {
      case "EQUAL": return "badge badge-equal";
      case "PERCENTAGE": return "badge badge-percentage";
      case "EXACT": return "badge badge-exact";
      default: return "badge badge-equal";
    }
  };

  // Helper to format split mode text
  const formatSplitMode = (mode: string) => {
    if (!mode) return "";
    return mode.toLowerCase().replace(/_/g, " ");
  };

  return (
    <div className="expenses-list" style={{ marginTop: 16 }}>
      <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginBottom: 12 }}>
        <h3 style={{ margin: 0 }}>Expenses</h3>
        <button className="btn btn-primary" onClick={() => setShowAdd(true)}>
          Add Expense
        </button>
      </div>

      {loading && <p>Loading expenses...</p>}
      {!loading && expenses.length === 0 && <p style={{ color: "var(--muted-fg)" }}>No expenses yet</p>}

      <div style={{ display: "grid", gap: 12 }}>
        {expenses.map((exp) => (
          <div key={exp.id} className="expense-card">
            <div className="expense-header">
              <div>
                <div className="expense-payer">
                  {exp.payerName}
                  <span className={getBadgeClass(exp.splitMode)}>
                    {formatSplitMode(exp.splitMode)}
                  </span>
                </div>
                {exp.description && <div className="expense-desc">{exp.description}</div>}
              </div>
              <div className="expense-amount">{Number(exp.amount).toFixed(2)} €</div>
            </div>

            {/* Thin divider line */}
            <div className="expense-divider"></div>

            <div className="share-list">
              {Array.isArray(exp.shares) &&
                exp.shares.map((s: any) => (
                  <div key={s.userId} className="share-row">
                    <div>
                      {s.userName}
                      {exp.splitMode === "PERCENTAGE" && s.value != null && (
                        <span style={{ 
                          marginLeft: "var(--space-xs)", 
                          color: "var(--color-text-muted)",
                          fontSize: "0.85rem" 
                        }}>
                          ({s.value}%)
                        </span>
                      )}
                    </div>
                    <div>{Number(s.amount).toFixed(2)} €</div>
                  </div>
                ))}
            </div>

            <div style={{ display: "flex", gap: 8, marginTop: 8 }}>
              <button className="btn btn-sm btn-secondary" onClick={() => setEditingExpense(exp)}>
                Edit
              </button>
              <button className="btn btn-sm btn-danger" onClick={() => handleDelete(exp.id)}>
                Delete
              </button>
            </div>
          </div>
        ))}
      </div>

      {showAdd && (
        <ExpenseModal
          eventId={eventId}
          onClose={() => setShowAdd(false)}
          onSaved={() => {
            setShowAdd(false);
            load();
            onExpenseAdded?.();
          }}
        />
      )}

      {editingExpense && (
        <ExpenseModal
          eventId={eventId}
          expense={editingExpense}
          onClose={() => setEditingExpense(null)}
          onSaved={() => {
            setEditingExpense(null);
            load();
            onExpenseAdded?.();
          }}
        />
      )}
    </div>
  );
}