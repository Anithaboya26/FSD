import { Route, Routes } from "react-router-dom";
import Dashboard from "./components/Dashboard.jsx";
import { DashboardRoute, RoleRoute } from "./components/AuthRoutes.jsx";
import { LoginPage, RegisterPage } from "./pages/AuthPage.jsx";
import BrowseEventsPage from "./pages/BrowseEventsPage.jsx";
import EventDetailPage from "./pages/EventDetailPage.jsx";
import HomePage from "./pages/HomePage.jsx";
import NotFoundPage from "./pages/NotFoundPage.jsx";
import OrganizerEventsPage from "./pages/OrganizerEventsPage.jsx";
import CreateEventPage from "./pages/CreateEventPage.jsx";

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/events" element={<BrowseEventsPage />} />
      <Route path="/events/:slug" element={<EventDetailPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/dashboard/:role" element={<DashboardRoute><Dashboard /></DashboardRoute>} />
      <Route path="/organizer/events" element={<RoleRoute role="organizer"><OrganizerEventsPage /></RoleRoute>} />
      <Route path="/organizer/events/new" element={<RoleRoute role="organizer"><CreateEventPage /></RoleRoute>} />
      <Route path="/organizer/events/:eventId/edit" element={<RoleRoute role="organizer"><CreateEventPage /></RoleRoute>} />
      <Route path="/not-found" element={<NotFoundPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
}
