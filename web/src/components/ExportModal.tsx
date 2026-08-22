import React, { useState } from 'react';
import { X, Download, FileText, Code2, Check } from 'lucide-react';
import { Memory } from '../types';
import { useToast } from '../context/ToastContext';

interface ExportModalProps {
  isOpen: boolean;
  onClose: () => void;
  memories: Memory[];
}

export const ExportModal: React.FC<ExportModalProps> = ({ isOpen, onClose, memories }) => {
  const [format, setFormat] = useState<'json' | 'markdown'>('json');
  const { showToast } = useToast();

  if (!isOpen) return null;

  const handleDownload = () => {
    try {
      let content = '';
      let filename = `replay-memories-export-${new Date().toISOString().slice(0, 10)}`;
      let mimeType = 'text/plain';

      if (format === 'json') {
        content = JSON.stringify(memories, null, 2);
        filename += '.json';
        mimeType = 'application/json';
      } else {
        filename += '.md';
        content = `# REPLAY Autobiographical Memory Export\n\n`;
        content += `Export Date: ${new Date().toLocaleString()}\n`;
        content += `Total Memories: ${memories.length}\n\n---\n\n`;

        memories.forEach((mem, idx) => {
          content += `### ${idx + 1}. ${mem.title || 'Untitled Memory'}\n`;
          content += `- **Date:** ${new Date(mem.occurredAt).toLocaleString()}\n`;
          content += `- **Type:** ${mem.type}\n`;
          if (mem.tags && mem.tags.length > 0) {
            content += `- **Tags:** ${mem.tags.join(', ')}\n`;
          }
          if (mem.description) {
            content += `\n${mem.description}\n`;
          }
          if (mem.aiAnalysis?.summary) {
            content += `\n> **AI Summary:** ${mem.aiAnalysis.summary}\n`;
          }
          content += `\n---\n\n`;
        });
      }

      const blob = new Blob([content], { type: mimeType });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);

      showToast(`Exported ${memories.length} memories as ${format.toUpperCase()}`, 'success');
      onClose();
    } catch (err) {
      showToast('Failed to export memories', 'error');
    }
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        backdropFilter: 'blur(6px)',
        zIndex: 9999,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '20px',
      }}
      onClick={onClose}
    >
      <div
        style={{
          background: 'var(--bg-card, #1e293b)',
          border: '1px solid var(--border-subtle, #334155)',
          borderRadius: '16px',
          maxWidth: '500px',
          width: '100%',
          padding: '24px',
          boxShadow: '0 20px 40px rgba(0,0,0,0.5)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <h3 style={{ fontSize: '18px', fontWeight: 600, color: '#ffffff' }}>Export Personal Memories</h3>
          <button
            onClick={onClose}
            style={{
              background: 'transparent',
              border: 'none',
              color: 'var(--text-muted, #94a3b8)',
              cursor: 'pointer',
            }}
          >
            <X size={20} />
          </button>
        </div>

        <p style={{ fontSize: '14px', color: 'var(--text-secondary, #94a3b8)', marginBottom: '20px' }}>
          Select the format you wish to export your memory timeline to. You can back up your memories or import them elsewhere.
        </p>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', marginBottom: '24px' }}>
          <button
            onClick={() => setFormat('json')}
            style={{
              padding: '16px',
              borderRadius: '12px',
              border: format === 'json' ? '2px solid #3b82f6' : '1px solid #334155',
              background: format === 'json' ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
              color: '#ffffff',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '8px',
              cursor: 'pointer',
            }}
          >
            <Code2 size={24} color="#3b82f6" />
            <span style={{ fontWeight: 600, fontSize: '14px' }}>JSON Archive</span>
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>Complete metadata</span>
          </button>

          <button
            onClick={() => setFormat('markdown')}
            style={{
              padding: '16px',
              borderRadius: '12px',
              border: format === 'markdown' ? '2px solid #3b82f6' : '1px solid #334155',
              background: format === 'markdown' ? 'rgba(59, 130, 246, 0.1)' : 'transparent',
              color: '#ffffff',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center',
              gap: '8px',
              cursor: 'pointer',
            }}
          >
            <FileText size={24} color="#10b981" />
            <span style={{ fontWeight: 600, fontSize: '14px' }}>Markdown Doc</span>
            <span style={{ fontSize: '12px', color: '#94a3b8' }}>Human readable</span>
          </button>
        </div>

        <div style={{ display: 'flex', gap: '12px', justifyContent: 'flex-end' }}>
          <button
            onClick={onClose}
            style={{
              padding: '10px 18px',
              borderRadius: '8px',
              background: 'transparent',
              border: '1px solid #334155',
              color: '#cbd5e1',
              cursor: 'pointer',
            }}
          >
            Cancel
          </button>
          <button
            onClick={handleDownload}
            style={{
              padding: '10px 20px',
              borderRadius: '8px',
              background: 'linear-gradient(135deg, #3b82f6, #6366f1)',
              border: 'none',
              color: '#ffffff',
              fontWeight: 600,
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              cursor: 'pointer',
            }}
          >
            <Download size={16} />
            Export File
          </button>
        </div>
      </div>
    </div>
  );
};
