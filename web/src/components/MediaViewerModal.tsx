import React from 'react';
import { X, ZoomIn, Download, Tag, User, MapPin } from 'lucide-react';
import { Memory } from '../types';

interface MediaViewerModalProps {
  memory: Memory | null;
  onClose: () => void;
}

export const MediaViewerModal: React.FC<MediaViewerModalProps> = ({ memory, onClose }) => {
  if (!memory) return null;

  const firstMedia = memory.media && memory.media.length > 0 ? memory.media[0] : null;

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.85)',
        backdropFilter: 'blur(8px)',
        zIndex: 9999,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '24px',
      }}
      onClick={onClose}
    >
      <div
        style={{
          background: 'var(--bg-card, #1e293b)',
          border: '1px solid var(--border-subtle, #334155)',
          borderRadius: '16px',
          maxWidth: '850px',
          width: '100%',
          maxHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.5)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div
          style={{
            padding: '16px 24px',
            borderBottom: '1px solid var(--border-subtle, #334155)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 600, color: 'var(--text-primary, #ffffff)' }}>
              {memory.title || 'Memory Details'}
            </h3>
            <span style={{ fontSize: '12px', color: 'var(--text-muted, #94a3b8)' }}>
              {new Date(memory.occurredAt).toLocaleString()}
            </span>
          </div>
          <button
            onClick={onClose}
            style={{
              background: 'rgba(255,255,255,0.05)',
              border: 'none',
              borderRadius: '50%',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--text-secondary, #cbd5e1)',
              cursor: 'pointer',
            }}
          >
            <X size={18} />
          </button>
        </div>

        {/* Content Body */}
        <div style={{ padding: '24px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '20px' }}>
          {firstMedia && (
            <div
              style={{
                borderRadius: '12px',
                overflow: 'hidden',
                background: '#000000',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                maxHeight: '400px',
              }}
            >
              <img
                src={`/api/media/${firstMedia.mediaId}`}
                alt={memory.title || 'Memory media'}
                style={{ width: '100%', height: 'auto', objectFit: 'contain' }}
                onError={(e) => {
                  (e.target as HTMLElement).style.display = 'none';
                }}
              />
            </div>
          )}

          {memory.description && (
            <div>
              <h4 style={{ fontSize: '14px', color: 'var(--text-muted, #94a3b8)', marginBottom: '6px' }}>Description</h4>
              <p style={{ fontSize: '15px', color: 'var(--text-primary, #ffffff)', lineHeight: 1.6 }}>
                {memory.description}
              </p>
            </div>
          )}

          {memory.aiAnalysis && (
            <div
              style={{
                background: 'rgba(99, 102, 241, 0.08)',
                border: '1px solid rgba(99, 102, 241, 0.2)',
                borderRadius: '12px',
                padding: '16px',
              }}
            >
              <h4 style={{ fontSize: '14px', color: '#a5b4fc', marginBottom: '8px', fontWeight: 600 }}>
                AI Autobiographical Insights
              </h4>
              {memory.aiAnalysis.summary && (
                <p style={{ fontSize: '14px', color: '#e0e7ff', marginBottom: '10px' }}>
                  {memory.aiAnalysis.summary}
                </p>
              )}
              {memory.aiAnalysis.detectedEmotions && memory.aiAnalysis.detectedEmotions.length > 0 && (
                <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap', marginTop: '8px' }}>
                  {memory.aiAnalysis.detectedEmotions.map((emotion, idx) => (
                    <span
                      key={idx}
                      style={{
                        fontSize: '12px',
                        padding: '4px 10px',
                        borderRadius: '20px',
                        background: 'rgba(168, 85, 247, 0.2)',
                        color: '#c084fc',
                        border: '1px solid rgba(168, 85, 247, 0.4)',
                      }}
                    >
                      {emotion}
                    </span>
                  ))}
                </div>
              )}
            </div>
          )}

          {/* Tags */}
          {memory.tags && memory.tags.length > 0 && (
            <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
              {memory.tags.map((tag, idx) => (
                <span
                  key={idx}
                  style={{
                    fontSize: '12px',
                    padding: '4px 10px',
                    borderRadius: '6px',
                    background: 'rgba(59, 130, 246, 0.15)',
                    color: '#60a5fa',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                  }}
                >
                  <Tag size={12} />
                  {tag}
                </span>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
