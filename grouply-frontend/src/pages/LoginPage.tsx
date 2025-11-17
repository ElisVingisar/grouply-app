import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login, setToken } from "../api/auth";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    setLoading(true);

    try {
      const response = await login({ email, password });
      setToken(response.token);
      navigate("/events");
    } catch (err) {
      setError("Invalid email or password");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ 
      maxWidth: 400, 
      margin: "80px auto", 
      padding: 24,
      background: "var(--card-bg)",
      borderRadius: 12,
      border: "1px solid var(--card-border-color)"
    }}>
      <h1 style={{ marginBottom: 24, textAlign: "center" }}>Login to Grouply</h1>
      
      <form onSubmit={handleSubmit}>
        <div className="field">
          <label className="field__label">Email</label>
          <input
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="admin@localhost"
            required
          />
        </div>

        <div className="field">
          <label className="field__label">Password</label>
          <input
            className="input"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Enter your password"
            required
          />
        </div>

        {error && (
          <div className="form-error" style={{ marginBottom: 16 }}>
            {error}
          </div>
        )}

        <button 
          type="submit" 
          className="btn btn-primary" 
          style={{ width: "100%", marginBottom: 16 }}
          disabled={loading}
        >
          {loading ? "Logging in..." : "Login"}
        </button>

        <div style={{ textAlign: "center" }}>
          <a 
            href="/register" 
            style={{ color: "var(--primary)", textDecoration: "none" }}
          >
            Don't have an account? Register
          </a>
        </div>
      </form>
    </div>
  );
}