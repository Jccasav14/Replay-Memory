import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity } from 'react-native';

interface VoiceRecorderModalProps {
  visible: boolean;
  onClose: () => void;
  onSaveAudio: (audioUri: string, durationSeconds: number) => void;
}

export const VoiceRecorderModal: React.FC<VoiceRecorderModalProps> = ({
  visible,
  onClose,
  onSaveAudio,
}) => {
  const [recording, setRecording] = useState(false);
  const [seconds, setSeconds] = useState(0);

  useEffect(() => {
    let interval: any = null;
    if (recording) {
      interval = setInterval(() => {
        setSeconds((prev) => prev + 1);
      }, 1000);
    } else {
      setSeconds(0);
    }
    return () => clearInterval(interval);
  }, [recording]);

  const handleToggleRecord = () => {
    if (recording) {
      setRecording(false);
      onSaveAudio('file:///mock-voice-recording-' + Date.now() + '.m4a', seconds);
      onClose();
    } else {
      setRecording(true);
    }
  };

  const formatTime = (secs: number) => {
    const m = Math.floor(secs / 60).toString().padStart(2, '0');
    const s = (secs % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  };

  return (
    <Modal visible={visible} transparent animationType="slide">
      <View style={styles.overlay}>
        <View style={styles.sheet}>
          <Text style={styles.title}>Voice Note Memo</Text>
          <Text style={styles.subtitle}>Record autobiographical voice reflections</Text>

          <View style={styles.timerContainer}>
            <Text style={styles.timerText}>{formatTime(seconds)}</Text>
            {recording && (
              <View style={styles.pulseContainer}>
                <View style={styles.pulseDot} />
                <Text style={styles.recordingStatus}>Recording audio...</Text>
              </View>
            )}
          </View>

          {/* Waveform Bars Simulation */}
          <View style={styles.waveformContainer}>
            {[40, 65, 20, 80, 50, 95, 30, 75, 45, 85, 60, 90, 35].map((height, i) => (
              <View
                key={i}
                style={[
                  styles.waveformBar,
                  {
                    height: recording ? height : 8,
                    backgroundColor: recording ? '#3b82f6' : '#374151',
                  },
                ]}
              />
            ))}
          </View>

          <TouchableOpacity
            style={[styles.recordBtn, recording ? styles.stopBtn : styles.startBtn]}
            onPress={handleToggleRecord}
          >
            <Text style={styles.recordBtnText}>
              {recording ? 'Stop & Attach Memo' : 'Start Recording'}
            </Text>
          </TouchableOpacity>

          <TouchableOpacity style={styles.cancelBtn} onPress={onClose}>
            <Text style={styles.cancelBtnText}>Cancel</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Modal>
  );
};

const styles = StyleSheet.create({
  overlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.7)',
    justifyContent: 'flex-end',
  },
  sheet: {
    backgroundColor: '#111724',
    borderTopLeftRadius: 24,
    borderTopRightRadius: 24,
    padding: 24,
    alignItems: 'center',
    borderColor: '#1f2937',
    borderWidth: 1,
  },
  title: { fontSize: 18, fontWeight: '700', color: '#f3f4f6', marginBottom: 4 },
  subtitle: { fontSize: 13, color: '#9ca3af', marginBottom: 24 },
  timerContainer: { alignItems: 'center', marginBottom: 20 },
  timerText: { fontSize: 40, fontWeight: '800', color: '#60a5fa', fontFamily: 'monospace' },
  pulseContainer: { flexDirection: 'row', alignItems: 'center', marginTop: 8, gap: 6 },
  pulseDot: { width: 8, height: 8, borderRadius: 4, backgroundColor: '#ef4444' },
  recordingStatus: { color: '#ef4444', fontSize: 12, fontWeight: '600' },
  waveformContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 4,
    height: 100,
    width: '100%',
    marginBottom: 24,
  },
  waveformBar: { width: 4, borderRadius: 2 },
  recordBtn: { width: '100%', padding: 14, borderRadius: 12, alignItems: 'center', marginBottom: 12 },
  startBtn: { backgroundColor: '#3b82f6' },
  stopBtn: { backgroundColor: '#ef4444' },
  recordBtnText: { color: '#fff', fontSize: 15, fontWeight: '700' },
  cancelBtn: { padding: 10 },
  cancelBtnText: { color: '#9ca3af', fontSize: 14 },
});
