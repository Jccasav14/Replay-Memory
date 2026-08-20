import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Image, ScrollView, Alert } from 'react-native';
import * as ImagePicker from 'expo-image-picker';
import { sqliteStorage } from '../services/sqliteStorage';
import { apiClient } from '../services/apiClient';

export const CreateMemoryScreen = ({ navigation }: any) => {
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [imageUri, setImageUri] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const pickImage = async () => {
    const result = await ImagePicker.launchImageLibraryAsync({
      mediaTypes: ImagePicker.MediaTypeOptions.Images,
      allowsEditing: true,
      quality: 0.8,
    });

    if (!result.canceled && result.assets[0]) {
      setImageUri(result.assets[0].uri);
    }
  };

  const takePhoto = async () => {
    const { status } = await ImagePicker.requestCameraPermissionsAsync();
    if (status !== 'granted') {
      Alert.alert('Permission needed', 'Camera permission is required to capture memories.');
      return;
    }

    const result = await ImagePicker.launchCameraAsync({
      allowsEditing: true,
      quality: 0.8,
    });

    if (!result.canceled && result.assets[0]) {
      setImageUri(result.assets[0].uri);
    }
  };

  const handleSave = async () => {
    if (!title.trim()) {
      Alert.alert('Validation', 'Please enter a title for this memory.');
      return;
    }

    setLoading(true);
    const localId = 'loc-' + Math.random().toString(36).substring(2, 9);
    const occurredAt = new Date().toISOString();

    try {
      // 1. Always save in offline SQLite local store first
      await sqliteStorage.insertMemory({
        id: localId,
        type: imageUri ? 'PHOTO' : 'NOTE',
        title,
        description,
        occurredAt,
        mediaLocalUri: imageUri || undefined,
      });

      // 2. Optimistically try online upload if possible
      try {
        const formData = new FormData();
        formData.append('data', JSON.stringify({
          type: imageUri ? 'PHOTO' : 'NOTE',
          title,
          description,
          occurredAt,
        }));

        if (imageUri) {
          const filename = imageUri.split('/').pop() || 'photo.jpg';
          const match = /\.(\w+)$/.exec(filename);
          const type = match ? `image/${match[1]}` : `image/jpeg`;
          formData.append('files', { uri: imageUri, name: filename, type } as any);
        }

        const res = await apiClient.post('/memories', formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });

        if (res.data?.data?.id) {
          await sqliteStorage.markAsSynced(localId, res.data.data.id);
        }
      } catch {
        console.log('Saved to local SQLite queue (device currently offline)');
      }

      Alert.alert('Memory Recorded', 'Your memory has been saved to your timeline.');
      navigation.navigate('Home');
    } catch (e: any) {
      Alert.alert('Error', e.message || 'Failed to record memory');
    } finally {
      setLoading(false);
    }
  };

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.header}>Capture Memory</Text>

      <Text style={styles.label}>Title</Text>
      <TextInput
        style={styles.input}
        placeholder="What happened?"
        placeholderTextColor="#6b7280"
        value={title}
        onChangeText={setTitle}
      />

      <Text style={styles.label}>Description / Reflection</Text>
      <TextInput
        style={[styles.input, { height: 100 }]}
        placeholder="Add context, thoughts, or feelings..."
        placeholderTextColor="#6b7280"
        multiline
        value={description}
        onChangeText={setDescription}
      />

      <View style={styles.btnRow}>
        <TouchableOpacity style={styles.mediaBtn} onPress={takePhoto}>
          <Text style={styles.mediaBtnText}>Take Photo</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.mediaBtn} onPress={pickImage}>
          <Text style={styles.mediaBtnText}>Pick from Gallery</Text>
        </TouchableOpacity>
      </View>

      {imageUri && (
        <Image source={{ uri: imageUri }} style={styles.imagePreview} />
      )}

      <TouchableOpacity style={styles.saveBtn} onPress={handleSave} disabled={loading}>
        <Text style={styles.saveBtnText}>{loading ? 'Recording...' : 'Record to Timeline'}</Text>
      </TouchableOpacity>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0a0d14', padding: 20 },
  header: { fontSize: 24, fontWeight: '700', color: '#f3f4f6', marginTop: 30, marginBottom: 20 },
  label: { fontSize: 13, color: '#9ca3af', marginBottom: 6 },
  input: { backgroundColor: '#111724', borderRadius: 8, borderColor: '#1f2937', borderWidth: 1, color: '#fff', padding: 12, marginBottom: 16 },
  btnRow: { flexDirection: 'row', gap: 12, marginBottom: 16 },
  mediaBtn: { flex: 1, backgroundColor: '#1f2937', padding: 12, borderRadius: 8, alignItems: 'center' },
  mediaBtnText: { color: '#60a5fa', fontWeight: '600', fontSize: 13 },
  imagePreview: { width: '100%', height: 200, borderRadius: 12, marginBottom: 20 },
  saveBtn: { backgroundColor: '#3b82f6', padding: 14, borderRadius: 8, alignItems: 'center', marginTop: 10 },
  saveBtnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
});
