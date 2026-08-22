import React, { useEffect, useState } from 'react';
import api from '../services/api';
import { Person, LocationEntity, ObjectEntity } from '../types';
import { Network, UserCheck, MapPin, Box } from 'lucide-react';

export const LifeGraphPage: React.FC = () => {
  const [people, setPeople] = useState<Person[]>([]);
  const [locations, setLocations] = useState<LocationEntity[]>([]);
  const [objects, setObjects] = useState<ObjectEntity[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchEntities = async () => {
      try {
        const [pRes, lRes, oRes] = await Promise.all([
          api.get('/people'),
          api.get('/locations'),
          api.get('/objects'),
        ]);
        setPeople(pRes.data.data || []);
        setLocations(lRes.data.data || []);
        setObjects(oRes.data.data || []);
      } catch (e) {
        console.error('Failed to load Life Graph entities', e);
      } finally {
        setLoading(false);
      }
    };
    fetchEntities();
  }, []);

  return (
    <div style={{ padding: '32px 40px', maxWidth: '1400px', margin: '0 auto' }}>
      <div style={{ marginBottom: '32px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
          <Network size={28} color="var(--accent-purple)" />
          <h1 style={{ fontSize: '28px', color: '#ffffff' }}>Life Graph Explorer</h1>
        </div>
        <p style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
          Topological network of people you know, places you frequent, and objects you own.
        </p>
      </div>

      {loading ? (
        <p style={{ color: 'var(--text-secondary)' }}>Loading Life Graph...</p>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '40px' }}>
          {/* People Section */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <UserCheck size={20} color="var(--accent-blue)" />
              <h2 style={{ fontSize: '18px', color: '#ffffff' }}>People in Your Life ({people.length})</h2>
            </div>
            {people.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>No people added yet.</p>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '16px' }}>
                {people.map((person) => (
                  <div key={person.id} className="glass-card" style={{ padding: '16px' }}>
                    <h3 style={{ fontSize: '15px', color: '#ffffff' }}>{person.name}</h3>
                    <p style={{ fontSize: '12px', color: 'var(--accent-blue)', marginTop: '4px' }}>{person.relationship || 'Contact'}</p>
                    <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>{person.interactionCount} memory interactions</p>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Locations Section */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <MapPin size={20} color="#ec4899" />
              <h2 style={{ fontSize: '18px', color: '#ffffff' }}>Key Locations ({locations.length})</h2>
            </div>
            {locations.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>No key locations added yet.</p>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '16px' }}>
                {locations.map((loc) => (
                  <div key={loc.id} className="glass-card" style={{ padding: '16px' }}>
                    <h3 style={{ fontSize: '15px', color: '#ffffff' }}>{loc.name}</h3>
                    <p style={{ fontSize: '12px', color: '#ec4899', marginTop: '4px' }}>{loc.category || 'Place'}</p>
                    <p style={{ fontSize: '11px', color: 'var(--text-muted)', marginTop: '8px' }}>{loc.address || 'Local coordinate'}</p>
                  </div>
                ))}
              </div>
            )}
          </div>

          {/* Objects Section */}
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px' }}>
              <Box size={20} color="#10b981" />
              <h2 style={{ fontSize: '18px', color: '#ffffff' }}>Tracked Objects ({objects.length})</h2>
            </div>
            {objects.length === 0 ? (
              <p style={{ color: 'var(--text-muted)', fontSize: '13px' }}>No objects tracked yet.</p>
            ) : (
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(240px, 1fr))', gap: '16px' }}>
                {objects.map((obj) => (
                  <div key={obj.id} className="glass-card" style={{ padding: '16px' }}>
                    <h3 style={{ fontSize: '15px', color: '#ffffff' }}>{obj.name}</h3>
                    <p style={{ fontSize: '12px', color: '#10b981', marginTop: '4px' }}>{obj.category || 'Asset'}</p>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
