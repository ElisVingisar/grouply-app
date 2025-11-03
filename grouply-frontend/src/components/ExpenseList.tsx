import { useEffect, useState } from "react";
import { listExpenses } from "../api/expenses";
import ExpenseModal from "./ExpenseModal";

type Props = {
  eventId: number;
  onExpenseAdded?: () => void; // optional callback to notify parent to refresh
};

export default function ExpenseList({ eventId, onExpenseAdded }: Props) {
  const [expenses, setExpenses] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);

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
                  <span 
                    style={{ 
                      marginLeft: 8,
                      fontSize: '0.85em',
                      color: 'var(--muted-fg)',
                      backgroundColor: 'var(--card-border-color)',
                      padding: '2px 8px',
                      borderRadius: '12px',
                      display: 'inline-block',
                      verticalAlign: 'middle'
                    }}
                  >
                    {String(exp.splitMode ?? "").toLowerCase()}
                  </span>
                </div>
                {exp.description && <div className="expense-desc">{exp.description}</div>}
              </div>
              <div className="expense-amount">{Number(exp.amount).toFixed(2)} €</div>
            </div>

            <div className="share-list">
              {Array.isArray(exp.shares) &&
                exp.shares.map((s: any) => (
                  <div key={s.userId} className="share-row">
                    <div>{s.userName}</div>
                    <div>{Number(s.amount).toFixed(2)} €</div>
                  </div>
                ))}
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
    </div>
  );
}