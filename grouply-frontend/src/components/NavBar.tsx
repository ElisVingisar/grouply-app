import { Link, NavLink } from "react-router-dom";
import "./NavBar.css";

export default function NavBar() {
    return (
        <header className="nav">
            <div className="nav__inner">
                <Link to="/" className="nav__brand">Grouply</Link>

                <nav className="nav__links">
                    <NavLink to="/" end className={({ isActive }) => "nav__link" + (isActive ? " is-active" : "")}>
                        Events
                    </NavLink>
                    <NavLink to="/events/new" className={({ isActive }) => "nav__link" + (isActive ? " is-active" : "")}>
                        Add
                    </NavLink>
                </nav>

                <div className="nav__actions">
                </div>
            </div>
        </header>
    );
}