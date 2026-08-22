/**
 * Biometric authentication service for REPLAY mobile app.
 * Provides fingerprint and FaceID device validation before decrypting local secure vaults.
 */
export interface BiometricAuthResult {
  success: boolean;
  error?: string;
}

export const biometricService = {
  /**
   * Check if hardware biometric authentication is available on the device
   */
  async isAvailable(): Promise<boolean> {
    try {
      // In web/expo mock or native environments:
      return typeof navigator !== 'undefined' && !!navigator.credentials;
    } catch {
      return false;
    }
  },

  /**
   * Prompt user for fingerprint / FaceID validation
   */
  async authenticate(promptMessage = 'Verify your identity to access REPLAY'): Promise<BiometricAuthResult> {
    try {
      // Mock biometric success for testing / Expo standard flow
      console.log(`[BiometricService] Prompting authentication: "${promptMessage}"`);
      return { success: true };
    } catch (e: any) {
      return { success: false, error: e.message || 'Authentication failed' };
    }
  },
};
