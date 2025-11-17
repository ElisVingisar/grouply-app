import { BrowserRouter, Routes, Route, Outlet, Navigate } from "react-router-dom";
import { isAuthenticated } from "./api/auth";
import NavBar from "./components/NavBar";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import EventsPage from "./pages/EventsPage";
import NewEventPage from "./pages/NewEventPage";
import InvitationsPage from "./pages/InvitationsPage";
import ProfilePage from "./pages/ProfilePage";

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  if (!isAuthenticated()) {
    return <Navigate to="/login" replace />;
  }
  return <>{children}</>;
}

function AppLayout() {
  return (
    <>
      <NavBar />
      <div className="container">
        <Outlet />
      </div>
    </>
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected layout with NavBar visible everywhere inside */}
        <Route
          element={
            <ProtectedRoute>
              <AppLayout />
            </ProtectedRoute>
          }
        >
          <Route path="/" element={<EventsPage />} />
          <Route path="/events" element={<EventsPage />} />
          <Route path="/events/new" element={<NewEventPage />} />
          <Route path="/invitations" element={<InvitationsPage />} />
          <Route path="/profile" element={<ProfilePage />} />
          {/* Optional: catch-all still shows NavBar */}
          {/* <Route path="*" element={<div style={{ padding: 24 }}>Not found</div>} /> */}
        </Route>
      </Routes>
    </BrowserRouter>
  );
}