import { BrowserRouter, Routes, Route } from "react-router-dom";
import EventsPage from "./pages/EventsPage";
import NewEventPage from "./pages/NewEventPage";
import NavBar from "./components/NavBar";

export default function App() {
    return (
        <BrowserRouter>
            <NavBar />
            <main className="page">
                <Routes>
                    <Route path="/" element={<EventsPage />} />
                    <Route path="/events/new" element={<NewEventPage />} />
                </Routes>
            </main>
        </BrowserRouter>
    );
}