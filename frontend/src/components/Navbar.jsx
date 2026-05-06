import { Link, useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  const navLinks = [
    { to: "/",          label: "Dashboard" },
    { to: "/controls",  label: "Controls"  },
    { to: "/analytics", label: "Analytics" },
  ];

  return (
    <nav style={{ backgroundColor: "#1B4F8A" }} className="text-white shadow-lg">
      <div className="max-w-7xl mx-auto px-4 flex items-center justify-between h-16">

        {/* Brand */}
        <Link to="/" className="font-bold text-lg tracking-wide">
          🛡️ Tool-53
        </Link>

        {/* Nav links */}
        <div className="hidden md:flex gap-6">
          {navLinks.map(link => (
            <Link
              key={link.to}
              to={link.to}
              className={`text-sm font-medium transition-opacity hover:opacity-80 min-h-touch flex items-center ${
                location.pathname === link.to ? "border-b-2 border-white" : "opacity-70"
              }`}
            >
              {link.label}
            </Link>
          ))}
        </div>

        {/* User info + logout */}
        <div className="flex items-center gap-4">
          <span className="text-sm opacity-70 hidden md:block">
            {user?.email} · <span className="uppercase text-xs">{user?.role}</span>
          </span>
          <button
            onClick={handleLogout}
            className="text-sm bg-white text-primary px-4 rounded-lg hover:bg-gray-100 min-h-touch flex items-center"
            style={{ color: "#1B4F8A" }}
          >
            Logout
          </button>
        </div>
      </div>

      {/* Mobile nav */}
      <div className="md:hidden flex gap-4 px-4 pb-3">
        {navLinks.map(link => (
          <Link key={link.to} to={link.to}
            className="text-sm opacity-80 hover:opacity-100">
            {link.label}
          </Link>
        ))}
      </div>
    </nav>
  );
}
