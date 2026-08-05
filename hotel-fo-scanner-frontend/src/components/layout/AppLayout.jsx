import React from 'react';
import { Outlet, NavLink, useNavigate } from 'react-router-dom';
import useAuthStore from '../../store/useAuthStore';
import { ScanFace, FileText, History, LogOut, LayoutDashboard } from 'lucide-react';

const SidebarItem = ({ icon: Icon, text, to }) => (
  <NavLink
    to={to}
    className={({ isActive }) =>
      `flex items-center gap-3 px-4 py-3 mb-2 rounded-xl transition-all duration-200 group ${
        isActive
          ? 'bg-blue-600/10 text-blue-600 font-medium'
          : 'text-slate-500 hover:bg-slate-100 hover:text-slate-900'
      }`
    }
  >
    <Icon className="w-5 h-5" />
    <span>{text}</span>
  </NavLink>
);

const AppLayout = () => {
  const user = useAuthStore((state) => state.user);
  const logout = useAuthStore((state) => state.logout);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="flex h-screen bg-slate-50">
      {/* Sidebar */}
      <div className="w-72 bg-white border-r border-slate-200 flex flex-col shadow-sm z-10">
        <div className="p-6">
          <div className="flex items-center gap-3 mb-8">
            <div className="w-10 h-10 bg-gradient-to-tr from-blue-600 to-cyan-500 rounded-lg flex items-center justify-center shadow-md">
              <ScanFace className="w-5 h-5 text-white" />
            </div>
            <div>
              <h1 className="font-bold text-slate-800 text-lg leading-tight">FO Scanner</h1>
              <p className="text-xs text-slate-500">Identity System</p>
            </div>
          </div>

          <div className="space-y-1">
            <SidebarItem icon={LayoutDashboard} text="Dashboard" to="/" />
            <SidebarItem icon={History} text="Scan Logs" to="/logs" />
            <SidebarItem icon={FileText} text="Reports" to="/reports" />
          </div>
        </div>

        <div className="mt-auto p-6 border-t border-slate-100">
          <div className="flex items-center gap-3 mb-6">
            <div className="w-10 h-10 bg-slate-100 rounded-full flex items-center justify-center border border-slate-200">
              <span className="font-bold text-slate-600">
                {user?.fullName?.charAt(0) || 'U'}
              </span>
            </div>
            <div className="overflow-hidden">
              <p className="text-sm font-semibold text-slate-800 truncate">{user?.fullName || 'User'}</p>
              <p className="text-xs text-slate-500 truncate">{user?.role || 'Staff'}</p>
            </div>
          </div>
          
          <button
            onClick={handleLogout}
            className="w-full flex items-center justify-center gap-2 px-4 py-2.5 text-sm font-medium text-red-600 bg-red-50 hover:bg-red-100 rounded-xl transition-colors"
          >
            <LogOut className="w-4 h-4" />
            <span>Sign Out</span>
          </button>
        </div>
      </div>

      {/* Main Content Area */}
      <div className="flex-1 overflow-auto bg-slate-50">
        <main className="p-8 h-full max-w-7xl mx-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default AppLayout;
