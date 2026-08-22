import React, { useState } from 'react';
import { View, Text, TextInput, TouchableOpacity, StyleSheet, Alert } from 'react-native';
import { apiClient } from '../services/apiClient';
import { authStorage } from '../services/authStorage';

export const LoginScreen = ({ onLoginSuccess }: any) => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async () => {
    if (!email || !password) {
      Alert.alert('Validation', 'Please enter email and password');
      return;
    }

    setLoading(true);
    try {
      const res = await apiClient.post('/auth/login', { email, password });
      const { accessToken, refreshToken, user } = res.data.data;
      await authStorage.saveTokens(accessToken, refreshToken);
      await authStorage.saveUser(user);
      onLoginSuccess();
    } catch (e: any) {
      Alert.alert('Login Failed', e.response?.data?.detail || 'Invalid credentials');
    } finally {
      setLoading(false);
    }
  };

  return (
    <View style={styles.container}>
      <Text style={styles.brandTitle}>REPLAY</Text>
      <Text style={styles.brandSubtitle}>Personal Memory Engine</Text>

      <View style={styles.form}>
        <Text style={styles.label}>Email</Text>
        <TextInput
          style={styles.input}
          placeholder="your@email.com"
          placeholderTextColor="#6b7280"
          autoCapitalize="none"
          keyboardType="email-address"
          value={email}
          onChangeText={setEmail}
        />

        <Text style={styles.label}>Password</Text>
        <TextInput
          style={styles.input}
          placeholder="••••••••"
          placeholderTextColor="#6b7280"
          secureTextEntry
          value={password}
          onChangeText={setPassword}
        />

        <TouchableOpacity style={styles.btn} onPress={handleLogin} disabled={loading}>
          <Text style={styles.btnText}>{loading ? 'Signing In...' : 'Sign In'}</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: { flex: 1, backgroundColor: '#0a0d14', justifyContent: 'center', padding: 24 },
  brandTitle: { fontSize: 32, fontWeight: '900', color: '#3b82f6', textAlign: 'center' },
  brandSubtitle: { fontSize: 13, color: '#9ca3af', textAlign: 'center', marginBottom: 36, marginTop: 4 },
  form: { backgroundColor: '#111724', padding: 24, borderRadius: 16, borderColor: '#1f2937', borderWidth: 1 },
  label: { fontSize: 13, color: '#9ca3af', marginBottom: 6 },
  input: { backgroundColor: '#1a2234', borderRadius: 8, borderColor: '#374151', borderWidth: 1, color: '#fff', padding: 12, marginBottom: 16 },
  btn: { backgroundColor: '#3b82f6', padding: 14, borderRadius: 8, alignItems: 'center', marginTop: 8 },
  btnText: { color: '#fff', fontWeight: '700', fontSize: 15 },
});
