import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { MemoryCard } from '../components/MemoryCard';
import { Memory } from '../types';
import { Calendar, ChevronLeft, ChevronRight } from 'lucide-react';

export const TimelinePage: React.FC = () => {
  const [currentYear, setCurrentYear] = useState(new Date().getFullYear());
  const [currentMonth, setCurrentMonth] = useState(new Date().getMonth() + 1);
  const [timelineData, setTimelineData] = useState<{ date: string; memories: Memory[] }[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchTimeline = async () => {
      setLoading(true);
      try {
        const res = await api.get(`/timeline?year=${currentYear}&month=${currentMonth}`);
        setTimelineData(res.data.data.days || []);
      } catch (e) {
        console.error('Failed to load timeline', e);
      } finally {
        setLoading(false);
      }
    };
    fetchTimeline();
  }, [currentYear, currentMonth]);

  const handlePrevMonth = () => {
    if (currentMonth === 1) {
      setCurrentMonth(12);
      setCurrentYear((y) => y - 1);
    } else {
      setCurrentMonth((m) => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (currentMonth === 12) {
      setCurrentMonth(1);
      setCurrentYear((y) => y + 1);
    } else {
      setCurrentMonth((m) => m + 1);
    }
  };

  const monthName = new Date(currentYear, currentMonth - 1).toLocaleString('en-US', { month: 'long' });

  return (
    <div style={{ padding: '32px 40px', maxWidth: '1200px', margin: '0 auto' }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '32px' }}>
        <div>
          <h1 style={{ fontSize: '28px', color: '#ffffff', marginBottom: '8px' }}>Personal Timeline</h1>
          <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
            Chronological journey through your recorded memories.
          </p>
        </div>

        {/* Month Picker */}
        <div className="glass-card" style={{ display: 'flex', alignItems: 'center', gap: '16px', padding: '8px 16px' }}>
          <button onClick={handlePrevMonth} style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer' }}>
            <ChevronLeft size={20} />
          </button>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontWeight: 600 }}>
            <Calendar size={18} color="var(--accent-blue)" />
            <span>{monthName} {currentYear}</span>
          </div>
          <button onClick={handleNextMonth} style={{ background: 'none', border: 'none', color: '#fff', cursor: 'pointer' }}>
            <ChevronRight size={20} />
          </button>
        </div>
      </div>

      {loading ? (
        <p style={{ color: 'var(--text-secondary)' }}>Loading timeline...</p>
      ) : timelineData.length === 0 ? (
        <div className="glass-card" style={{ padding: '40px', textAlign: 'center' }}>
          <p style={{ color: 'var(--text-secondary)' }}>No memories recorded in {monthName} {currentYear}.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '32px' }}>
          {timelineData.map((dayGroup) => (
            <div key={dayGroup.date} style={{ display: 'flex', gap: '24px' }}>
              <div style={{ width: '120px', flexShrink: 0 }}>
                <p style={{ fontSize: '14px', fontWeight: 700, color: 'var(--accent-blue)' }}>
                  {new Date(dayGroup.date).toLocaleDateString('en-US', { day: '2-digit', month: 'short' })}
                </p>
                <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                  {new Date(dayGroup.date).toLocaleDateString('en-US', { weekday: 'short' })}
                </p>
              </div>

              <div style={{ flex: 1, display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '20px' }}>
                {dayGroup.memories.map((mem) => (
                  <MemoryCard key={mem.id} memory={mem} />
                ))}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
