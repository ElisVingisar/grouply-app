import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { register, setToken } from "../api/auth";

export default function RegisterPage() {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError("");
    
    if (password.length < 6) {
      setError("Password must be at least 6 characters");
      return;
    }

    setLoading(true);

    try {
      const response = await register({ name, email, password });
      setToken(response.token);
      navigate("/events");
    } catch (err) {
      setError("Registration failed. Email may already be in use.");
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
      <h1 style={{ marginBottom: 24, textAlign: "center" }}>Create Account</h1>
      
      <form onSubmit={handleSubmit}>
        <div className="field">
          <label className="field__label">Name</label>
          <input
            className="input"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Your name"
            required
          />
        </div>

        <div className="field">
          <label className="field__label">Email</label>
          <input
            className="input"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="your@email.com"
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
            placeholder="At least 6 characters"
            required
            minLength={6}
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
          {loading ? "Creating account..." : "Register"}
        </button>

        <div style={{ textAlign: "center" }}>
          <a 
            href="/login" 
            style={{ color: "var(--primary)", textDecoration: "none" }}
          >
            Already have an account? Login
          </a>
        </div>
      </form>
    </div>
  );
}