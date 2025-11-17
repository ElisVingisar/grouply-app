import { useEffect, useState } from "react";
import { listPendingInvitations } from "../api/expenses";
import "./NavBar.css";

export default function NavBar() {
    const [pendingCount, setPendingCount] = useState(0);
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
    const user = JSON.parse(localStorage.getItem("user") || "{}");

    useEffect(() => {
        let cancel = false;
        listPendingInvitations()
            .then(list => {
                if (!cancel) setPendingCount(list.length);
            })
            .catch(() => { });
        return () => { cancel = true; };
    }, []);

    const handleLogout = () => {
        localStorage.removeItem("authToken");
        localStorage.removeItem("user");
        window.location.href = "/login";
    };

    return (
        <nav className="navbar">
            <div className="container">
                <div className="navbar-content">
                    <a href="/" className="navbar-brand">
                        <span className="brand-icon">◉</span>
                        <span className="brand-text">Grouply</span>
                    </a>

                    <button
                        className="mobile-menu-toggle"
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        aria-label="Toggle menu"
                    >
                        <span></span>
                        <span></span>
                        <span></span>
                    </button>

                    <div className={`navbar-menu ${mobileMenuOpen ? "active" : ""}`}>
                        <a href="/" className="nav-link">
                            Events
                        </a>
                        <a href="/invitations" className="nav-link">
                            Invitations
                            {pendingCount > 0 && <span className="badge">{pendingCount}</span>}
                        </a>

                        <div className="navbar-actions">
                            <div className="user-menu">
                                <button className="user-button">
                                    <div className="user-avatar">{user.name?.[0]?.toUpperCase() || "U"}</div>
                                    <span className="user-name">{user.name || "User"}</span>
                                </button>
                                <div className="user-dropdown">
                                    <a href="/profile" className="dropdown-item">
                                        Profile
                                    </a>
                                    <button onClick={handleLogout} className="dropdown-item">
                                        Sign out
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </nav>
    );
}