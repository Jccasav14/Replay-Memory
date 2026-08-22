import React, { useState } from 'react';
import { View, Text, StyleSheet, Modal, TouchableOpacity, ScrollView } from 'react-native';

interface MemoryFilterModalProps {
  visible: boolean;
  onClose: () => void;
  onApplyFilters: (filters: { type?: string; emotion?: string }) => void;
}

export const MemoryFilterModal: React.FC<MemoryFilterModalProps> = ({
  visible,
  onClose,
  onApplyFilters,
}) => {
  const [selectedType, setSelectedType] = useState<string | null>(null);
  const [selectedEmotion, setSelectedEmotion] = useState<string | null>(null);

  const types = ['ALL', 'PHOTO', 'NOTE', 'AUDIO', 'LOCATION_EVENT'];
  const emotions = ['Joyful', 'Nostalgic', 'Calm', 'Accomplished', 'Energetic'];

  const handleApply = () => {
    onApplyFilters({
      type: selectedType && selectedType !== 'ALL' ? selectedType : undefined,
      emotion: selectedEmotion || undefined,
    });
    onClose();
  };

  const handleReset = () => {
    setSelectedType(null);
    setSelectedEmotion(null);
    onApplyFilters({});
    onClose();
  };

  return (
    <Modal visible={visible} transparent animationType="slide">
      <View style={styles.overlay}>
        <View style={styles.sheet}>
          <Text style={styles.title}>Filter Timeline</Text>
          <Text style={styles.subtitle}>Narrow down your autobiographical recollections</Text>

          <ScrollView style={styles.scroll}>
            <Text style={styles.sectionHeader}>Memory Type</Text>
            <View style={styles.chipRow}>
              {types.map((t) => (
                <TouchableOpacity
                  key={t}
                  style={[
                    styles.chip,
                    selectedType === t && styles.activeChip,
                  ]}
                  onPress={() => setSelectedType(t === selectedType ? null : t)}
                >
                  <Text
                    style={[
                      styles.chipText,
                      selectedType === t && styles.activeChipText,
                    ]}
                  >
                    {t}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>

            <Text style={[styles.sectionHeader, { marginTop: 16 }]}>Detected Emotion</Text>
            <View style={styles.chipRow}>
              {emotions.map((e) => (
                <TouchableOpacity
                  key={e}
                  style={[
                    styles.chip,
                    selectedEmotion === e && styles.activeEmotionChip,
                  ]}
                  onPress={() => setSelectedEmotion(e === selectedEmotion ? null : e)}
                >
                  <Text
                    style={[
                      styles.chipText,
                      selectedEmotion === e && styles.activeChipText,
                    ]}
                  >
                    {e}
                  </Text>
                </TouchableOpacity>
              ))}
            </View>
          </ScrollView>

          <View style={styles.btnRow}>
            <TouchableOpacity style={styles.resetBtn} onPress={handleReset}>
              <Text style={styles.resetBtnText}>Reset</Text>
            </TouchableOpacity>
            <TouchableOpacity style={styles.applyBtn} onPress={handleApply}>
              <Text style={styles.applyBtnText}>Apply Filters</Text>
            </TouchableOpacity>
          </View>
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
    maxHeight: '80%',
    borderColor: '#1f2937',
    borderWidth: 1,
  },
  title: { fontSize: 18, fontWeight: '700', color: '#f3f4f6' },
  subtitle: { fontSize: 13, color: '#9ca3af', marginTop: 2, marginBottom: 16 },
  scroll: { marginBottom: 20 },
  sectionHeader: { fontSize: 13, fontWeight: '600', color: '#cbd5e1', marginBottom: 8 },
  chipRow: { flexDirection: 'row', flexWrap: 'wrap', gap: 8 },
  chip: {
    backgroundColor: '#1f2937',
    paddingHorizontal: 12,
    paddingVertical: 6,
    borderRadius: 16,
    borderWidth: 1,
    borderColor: '#374151',
  },
  activeChip: {
    backgroundColor: '#3b82f6',
    borderColor: '#60a5fa',
  },
  activeEmotionChip: {
    backgroundColor: '#8b5cf6',
    borderColor: '#a78bfa',
  },
  chipText: { color: '#9ca3af', fontSize: 12, fontWeight: '500' },
  activeChipText: { color: '#ffffff', fontWeight: '700' },
  btnRow: { flexDirection: 'row', gap: 12 },
  resetBtn: {
    flex: 1,
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#374151',
  },
  resetBtnText: { color: '#9ca3af', fontWeight: '600' },
  applyBtn: {
    flex: 2,
    backgroundColor: '#3b82f6',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
  },
  applyBtnText: { color: '#fff', fontWeight: '700' },
});
