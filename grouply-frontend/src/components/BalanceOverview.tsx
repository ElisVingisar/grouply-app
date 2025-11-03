import { useEffect, useState } from "react";
import { listBalances, suggestedSettlements, postPayment, listUsers } from "../api/expenses";

export default function BalanceOverview({ eventId }: { eventId: number }) {
  const [balances, setBalances] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [settlements, setSettlements] = useState<any[]>([]);
  const [showSettlements, setShowSettlements] = useState(false);
  const [users, setUsers] = useState<{ [key: number]: string }>({});

  const load = () => {
    setLoading(true);
    listBalances(eventId)
      .then((d) => setBalances(Array.isArray(d) ? d : []))
      .catch((e) => {
        console.error("Failed to load balances:", e);
        setBalances([]);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => { load(); }, [eventId]);

  useEffect(() => {
    listUsers()
      .then((u) => {
        const map: any = {};
        (u || []).forEach((x: any) => (map[x.id] = x.name));
        setUsers(map);
      })
      .catch((e) => console.error("Failed to load users:", e));
  }, []);

  const loadSuggested = () => {
    suggestedSettlements(eventId)
      .then((d) => setSettlements(Array.isArray(d) ? d : []))
      .catch((e) => {
        console.error("Failed to fetch settlements:", e);
        setSettlements([]);
      });
    setShowSettlements(true);
  };

  const settleOne = async (t: any) => {
    try {
      await postPayment({
        eventId,
        fromUserId: t.fromUserId,
        toUserId: t.toUserId,
        amount: String(t.amount),
      });
      await listBalances(eventId).then(setBalances);
      setSettlements((s) =>
        s.filter(
          (x: any) =>
            !(
              x.fromUserId === t.fromUserId &&
              x.toUserId === t.toUserId &&
              Number(x.amount) === Number(t.amount)
            )
        )
      );
    } catch (e) {
      alert("Failed to mark payment as settled");
    }
  };

  const amountClass = (val: number) => {
    if (Math.abs(val) < 0.005) return "amount--zero";
    return val > 0 ? "amount--debt" : "amount--credit";
  };

  return (
    <div className="balance-overview" style={{ marginTop: 8 }}>
      <h3 style={{ margin: "0 0 8px 0" }}>Balances</h3>

      {loading && <p>Loading balances...</p>}

      {!loading && (
        <>
          <div style={{ display: "grid", gap: 8 }}>
            {balances.map((b: any) => (
              <div key={b.userId} className="balance-row">
                <div className="name">{b.name}</div>
                <div className={`amount ${amountClass(Number(b.balance))}`}>
                  {Number(b.balance).toFixed(2)} €
                </div>
              </div>
            ))}
          </div>

          <div style={{ display: "flex", justifyContent: "flex-end", marginTop: 10 }}>
            <button className="btn btn-muted btn-sm" onClick={loadSuggested}>
              Calculate Settlements
            </button>
          </div>

          {showSettlements && (
            <div style={{ marginTop: 12 }}>
              <h4 style={{ margin: "8px 0" }}>Suggested transfers</h4>
              {settlements.length === 0 && <p style={{ color: "var(--muted-fg)" }}>No transfers needed</p>}
              <div style={{ display: "grid", gap: 8 }}>
                {settlements.map((s: any, idx: number) => (
                  <div key={idx} className="settlement-card">
                    <div>
                      <strong>{users[s.fromUserId] ?? s.fromUserId}</strong>
                      <span style={{ margin: "0 8px", color: "var(--muted-fg)" }}>→</span>
                      <strong>{users[s.toUserId] ?? s.toUserId}</strong>
                    </div>
                    <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
                      <div style={{ fontWeight: 600 }}>{Number(s.amount).toFixed(2)} €</div>
                      <button className="btn btn-primary btn-sm" onClick={() => settleOne(s)}>
                        Mark paid
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </>
      )}
    </div>
  );
}