import React, { useState } from 'react';
import api from '../services/api';
import { Memory } from '../types';
import { MemoryCard } from '../components/MemoryCard';
import { Search, Sparkles, Send } from 'lucide-react';

export const SearchPage: React.FC = () => {
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);
  const [answer, setAnswer] = useState<string | null>(null);
  const [results, setResults] = useState<Memory[]>([]);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    setLoading(true);
    try {
      const res = await api.post('/search/semantic', { query, topK: 5, generateAnswer: true });
      setAnswer(res.data.data.answer);
      setResults(res.data.data.matchedMemories || []);
    } catch (e) {
      console.error('Search failed', e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ padding: '32px 40px', maxWidth: '1100px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <h1 style={{ fontSize: '28px', color: '#ffffff', marginBottom: '8px' }}>Intelligent Cognitive Search</h1>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          Ask natural language questions about your life, people, places, and events.
        </p>
      </div>

      {/* Search Input Bar */}
      <form onSubmit={handleSearch} style={{ marginBottom: '32px' }}>
        <div className="glass-card" style={{ display: 'flex', alignItems: 'center', padding: '12px 20px', gap: '12px' }}>
          <Search size={22} color="var(--text-muted)" />
          <input
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="e.g. When did I work at university with Carlos?"
            style={{
              flex: 1,
              background: 'none',
              border: 'none',
              color: '#ffffff',
              fontSize: '16px',
              outline: 'none',
              fontFamily: 'var(--font-sans)',
            }}
          />
          <button type="submit" className="btn-primary" disabled={loading}>
            {loading ? 'Searching...' : <Send size={16} />}
          </button>
        </div>
      </form>

      {/* AI Synthesized Answer Card */}
      {answer && (
        <div className="glass-card" style={{ padding: '24px', marginBottom: '32px', borderLeft: '4px solid var(--accent-blue)', background: 'rgba(59, 130, 246, 0.08)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
            <Sparkles size={20} color="var(--accent-blue)" />
            <h3 style={{ fontSize: '16px', color: '#ffffff' }}>Synthesized Answer</h3>
          </div>
          <p style={{ fontSize: '15px', color: 'var(--text-primary)', lineHeight: 1.6 }}>{answer}</p>
        </div>
      )}

      {/* Matched Memories List */}
      {results.length > 0 && (
        <div>
          <h3 style={{ fontSize: '18px', color: '#ffffff', marginBottom: '16px' }}>Retrieved Evidential Memories</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
            {results.map((mem) => (
              <MemoryCard key={mem.id} memory={mem} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
};
