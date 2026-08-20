import React, { useEffect, useState } from 'react';
import { Memory } from '../types';
import api from '../services/api';
import { MemoryCard } from '../components/MemoryCard';
import { Sparkles, Activity, Image as ImageIcon, MapPin, Users } from 'lucide-react';

export const DashboardPage: React.FC = () => {
  const [recentMemories, setRecentMemories] = useState<Memory[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchDashboard = async () => {
      try {
        const res = await api.get('/memories?page=0&size=6');
        setRecentMemories(res.data.data.content || []);
      } catch (e) {
        console.error('Failed to load dashboard memories', e);
      } finally {
        setLoading(false);
      }
    };
    fetchDashboard();
  }, []);

  return (
    <div style={{ padding: '32px 40px', maxWidth: '1400px', margin: '0 auto' }}>
      {/* Header */}
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '28px', color: '#ffffff', marginBottom: '8px' }}>Memory Dashboard</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          Overview of your biographical timeline, Life Graph entities, and cognitive highlights.
        </p>
      </div>

      {/* Metrics Row */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '20px', marginBottom: '40px' }}>
        <div className="glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ padding: '12px', borderRadius: '12px', background: 'rgba(59, 130, 246, 0.15)', color: 'var(--accent-blue)' }}>
            <Activity size={24} />
          </div>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Total Memories</p>
            <h3 style={{ fontSize: '22px', color: '#ffffff' }}>{recentMemories.length}</h3>
          </div>
        </div>

        <div className="glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ padding: '12px', borderRadius: '12px', background: 'rgba(139, 92, 246, 0.15)', color: 'var(--accent-purple)' }}>
            <Users size={24} />
          </div>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Life Graph People</p>
            <h3 style={{ fontSize: '22px', color: '#ffffff' }}>12</h3>
          </div>
        </div>

        <div className="glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ padding: '12px', borderRadius: '12px', background: 'rgba(236, 72, 153, 0.15)', color: '#ec4899' }}>
            <MapPin size={24} />
          </div>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Places Visited</p>
            <h3 style={{ fontSize: '22px', color: '#ffffff' }}>8</h3>
          </div>
        </div>

        <div className="glass-card" style={{ padding: '20px', display: 'flex', alignItems: 'center', gap: '16px' }}>
          <div style={{ padding: '12px', borderRadius: '12px', background: 'rgba(16, 185, 129, 0.15)', color: '#10b981' }}>
            <Sparkles size={24} />
          </div>
          <div>
            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>AI Insights Indexed</p>
            <h3 style={{ fontSize: '22px', color: '#ffffff' }}>100%</h3>
          </div>
        </div>
      </div>

      {/* Recent Memories */}
      <div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
          <h2 style={{ fontSize: '20px', color: '#ffffff' }}>Recent Memories</h2>
        </div>

        {loading ? (
          <p style={{ color: 'var(--text-secondary)' }}>Loading memories...</p>
        ) : recentMemories.length === 0 ? (
          <div className="glass-card" style={{ padding: '40px', textAlign: 'center' }}>
            <ImageIcon size={48} color="var(--text-muted)" style={{ margin: '0 auto 16px' }} />
            <h3 style={{ fontSize: '18px', color: '#ffffff', marginBottom: '8px' }}>No memories captured yet</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '14px', maxWidth: '400px', margin: '0 auto' }}>
              Create your first memory via the mobile app or upload photos to start reconstructing your Life Graph.
            </p>
          </div>
        ) : (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '24px' }}>
            {recentMemories.map((mem) => (
              <MemoryCard key={mem.id} memory={mem} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
