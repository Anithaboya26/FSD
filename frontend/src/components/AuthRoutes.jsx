import { LoaderCircle } from "lucide-react";
import { Navigate, useLocation, useParams } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

export default function AuthRoutes({ children }) {
  const { user, status } = useAuth();
  const location = useLocation();

  if (status === "loading") {
    return <LoadingScreen />;
  }
  if (status === "error") {
    return (
      <main className="grid min-h-screen place-items-center bg-lavender px-5">
        <div className="text-center">
          <h1 className="text-2xl font-extrabold text-ink">Can’t reach Ventixe right now</h1>
          <p className="mt-2 text-sm text-muted">Check that the API is running, then refresh this page.</p>
        </div>
      </main>
    );
  }
  if (!user) {
    return <Navigate replace state={{ from: location }} to="/login" />;
  }
  return children;
}

export function DashboardRoute({ children }) {
  const { user, status } = useAuth();
  const { role } = useParams();
  const location = useLocation();

  if (status === "loading") return <LoadingScreen />;
  if (status === "error") return <AuthRoutes>{children}</AuthRoutes>;
  if (!user) return <Navigate replace state={{ from: location }} to="/login" />;

  const accountRole = user.role.toLowerCase();
  if (role !== accountRole) {
    return <Navigate replace to={`/dashboard/${accountRole}`} />;
  }
  return children;
}

export function RoleRoute({ role, children }) {
  const { user, status } = useAuth();
  const location = useLocation();

  if (status === "loading") return <LoadingScreen />;
  if (status === "error") return <AuthRoutes>{children}</AuthRoutes>;
  if (!user) return <Navigate replace state={{ from: location }} to="/login" />;
  if (user.role.toLowerCase() !== role) {
    return <Navigate replace to={`/dashboard/${user.role.toLowerCase()}`} />;
  }
  return children;
}

function LoadingScreen() {
  return (
    <main className="grid min-h-screen place-items-center bg-lavender text-violet" role="status" aria-label="Loading your account">
      <LoaderCircle className="animate-spin" size={32} />
    </main>
  );
}
