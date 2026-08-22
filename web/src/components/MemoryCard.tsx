import React from 'react';
import { Memory } from '../types';
import { Calendar, MapPin, Tag, Image as ImageIcon } from 'lucide-react';

export const MemoryCard: React.FC<{ memory: Memory }> = ({ memory }) => {
  const formattedDate = new Date(memory.occurredAt).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });

  const previewImage = memory.media.find((m) => m.fileType === 'IMAGE');

  return (
    <div className="glass-card" style={{ overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
      {previewImage ? (
        <div style={{ height: '180px', width: '100%', overflow: 'hidden', background: '#05070a', position: 'relative' }}>
          <img
            src={`/api/v1/media/preview?path=${previewImage.storagePath}`}
            alt={memory.title || 'Memory'}
            style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            loading="lazy"
          />
          <div style={{ position: 'absolute', top: '10px', right: '10px', background: 'rgba(0,0,0,0.6)', padding: '4px 8px', borderRadius: '4px', fontSize: '11px' }}>
            {memory.type}
          </div>
        </div>
      ) : (
        <div style={{ height: '60px', padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '8px', background: 'rgba(255,255,255,0.02)' }}>
          <ImageIcon size={18} color="var(--accent-blue)" />
          <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{memory.type}</span>
        </div>
      )}

      <div style={{ padding: '20px', flex: 1, display: 'flex', flexDirection: 'column', gap: '12px' }}>
        <h3 style={{ fontSize: '16px', color: 'var(--text-primary)' }}>{memory.title || 'Untitled Memory'}</h3>
        
        {memory.description && (
          <p style={{ fontSize: '13px', color: 'var(--text-secondary)', lineHeight: 1.5 }}>
            {memory.description}
          </p>
        )}

        {memory.aiAnalysis?.summary && (
          <div style={{ background: 'rgba(139, 92, 246, 0.1)', borderLeft: '3px solid var(--accent-purple)', padding: '8px 12px', borderRadius: '4px', fontSize: '12px', color: '#c4b5fd' }}>
            {memory.aiAnalysis.summary}
          </div>
        )}

        <div style={{ marginTop: 'auto', paddingTop: '12px', borderTop: '1px solid var(--border-subtle)', display: 'flex', flexWrap: 'wrap', gap: '12px', fontSize: '12px', color: 'var(--text-muted)' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <Calendar size={14} />
            <span>{formattedDate}</span>
          </div>

          {memory.location?.name && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <MapPin size={14} />
              <span>{memory.location.name}</span>
            </div>
          )}

          {memory.tags && memory.tags.length > 0 && (
            <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
              <Tag size={14} />
              <span>{memory.tags.slice(0, 2).join(', ')}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
