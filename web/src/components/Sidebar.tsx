import React from 'react';
import { NavLink } from 'react-router-dom';
import { Clock, Network, Search, LayoutDashboard, UserCheck, MapPin, Box, LogOut } from 'lucide-react';
import { useAuth } from '../context/AuthContext';

export const Sidebar: React.FC = () => {
  const { logout, user } = useAuth();

  const links = [
    { to: '/', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/timeline', label: 'Timeline', icon: Clock },
    { to: '/search', label: 'Intelligent Search', icon: Search },
    { to: '/graph', label: 'Life Graph', icon: Network },
    { to: '/people', label: 'People', icon: UserCheck },
    { to: '/places', label: 'Places', icon: MapPin },
    { to: '/objects', label: 'Objects', icon: Box },
  ];

  return (
    <aside style={{
      width: '260px',
      background: 'var(--bg-secondary)',
      borderRight: '1px solid var(--border-subtle)',
      display: 'flex',
      flexDirection: 'column',
      height: '100vh',
      position: 'sticky',
      top: 0
    }}>
      <div style={{ padding: '24px', borderBottom: '1px solid var(--border-subtle)' }}>
        <h2 style={{ fontSize: '20px', background: 'var(--accent-gradient)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
          REPLAY
        </h2>
        <p style={{ fontSize: '12px', color: 'var(--text-muted)', marginTop: '4px' }}>Personal Memory Engine</p>
      </div>

      <nav style={{ flex: 1, padding: '16px 12px', display: 'flex', flexDirection: 'column', gap: '6px' }}>
        {links.map((link) => {
          const Icon = link.icon;
          return (
            <NavLink
              key={link.to}
              to={link.to}
              style={({ isActive }) => ({
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                padding: '10px 14px',
                borderRadius: 'var(--radius-sm)',
                fontSize: '14px',
                color: isActive ? '#ffffff' : 'var(--text-secondary)',
                background: isActive ? 'rgba(59, 130, 246, 0.15)' : 'transparent',
                borderLeft: isActive ? '3px solid var(--accent-blue)' : '3px solid transparent',
                transition: 'all 0.15s ease'
              })}
            >
              <Icon size={18} />
              <span>{link.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div style={{ padding: '16px 20px', borderTop: '1px solid var(--border-subtle)' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <div>
            <p style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)' }}>{user?.fullName}</p>
            <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>{user?.email}</p>
          </div>
          <button
            onClick={logout}
            style={{ background: 'none', border: 'none', color: 'var(--text-secondary)', cursor: 'pointer' }}
            title="Log out"
          >
            <LogOut size={18} />
          </button>
        </div>
      </div>
    </aside>
  );
};
