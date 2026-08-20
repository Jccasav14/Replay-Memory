import React, { useEffect, useState } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, RefreshControl } from 'react-native';
import { apiClient } from '../services/apiClient';
import { sqliteStorage } from '../services/sqliteStorage';

export const HomeScreen = ({ navigation }: any) => {
  const [memories, setMemories] = useState<any[]>([]);
  const [refreshing, setRefreshing] = useState(false);

  const loadMemories = async () => {
    try {
      const res = await apiClient.get('/memories?page=0&size=10');
      setMemories(res.data.data.content || []);
    } catch {
      // Fallback to local SQLite memories
      const local = await sqliteStorage.getLocalMemories();
      setMemories(local);
    }
  };

  useEffect(() => {
    loadMemories();
  }, []);

  const onRefresh = async () => {
    setRefreshing(true);
    await loadMemories();
    setRefreshing(false);
  };

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.brandTitle}>REPLAY</Text>
        <Text style={styles.subtitle}>Your Cognitive Timeline</Text>
      </View>

      <FlatList
        data={memories}
        keyExtractor={(item) => item.id}
        refreshControl={<RefreshControl refreshing={refreshing} onRefresh={onRefresh} tintColor="#3b82f6" />}
        renderItem={({ item }) => (
          <View style={styles.card}>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{item.type || 'MEMORY'}</Text>
            </View>
            <Text style={styles.title}>{item.title || 'Untitled Memory'}</Text>
            {item.description ? <Text style={styles.description}>{item.description}</Text> : null}
            <Text style={styles.date}>{new Date(item.occurred_at || item.occurredAt).toLocaleDateString()}</Text>
          </View>
        )}
        ListEmptyComponent={
          <View style={styles.emptyContainer}>
            <Text style={styles.emptyText}>No memories recorded yet.</Text>
            <TouchableOpacity style={styles.createBtn} onPress={() => navigation.navigate('Create')}>
              <Text style={styles.createBtnText}>Capture First Memory</Text>
            </TouchableOpacity>
          </View>
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0a0d14', padding: 16 },
  header: { marginTop: 40, marginBottom: 20 },
  brandTitle: { fontSize: 28, fontWeight: '800', color: '#3b82f6' },
  subtitle: { fontSize: 13, color: '#9ca3af', marginTop: 2 },
  card: { backgroundColor: '#111724', borderRadius: 12, padding: 16, marginBottom: 12, borderColor: '#1f2937', borderWidth: 1 },
  badge: { alignSelf: 'flex-start', backgroundColor: 'rgba(59,130,246,0.15)', paddingHorizontal: 8, paddingVertical: 3, borderRadius: 4, marginBottom: 8 },
  badgeText: { color: '#60a5fa', fontSize: 11, fontWeight: '600' },
  title: { color: '#f3f4f6', fontSize: 16, fontWeight: '700' },
  description: { color: '#9ca3af', fontSize: 13, marginTop: 4 },
  date: { color: '#6b7280', fontSize: 11, marginTop: 8 },
  emptyContainer: { alignItems: 'center', marginTop: 60 },
  emptyText: { color: '#9ca3af', marginBottom: 16 },
  createBtn: { backgroundColor: '#3b82f6', paddingVertical: 10, paddingHorizontal: 20, borderRadius: 8 },
  createBtnText: { color: '#fff', fontWeight: '600' },
});
