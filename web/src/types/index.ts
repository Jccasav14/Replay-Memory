export interface User {
  id: string;
  email: string;
  fullName: string;
  role: 'ROLE_USER' | 'ROLE_ADMIN';
}

export interface MediaItem {
  mediaId: string;
  fileType: 'IMAGE' | 'VIDEO' | 'AUDIO' | 'DOCUMENT';
  storagePath: string;
  thumbnailStoragePath?: string;
  mimeType: string;
  fileSizeBytes: number;
}

export interface LocationPoint {
  name?: string;
  address?: string;
  latitude?: number;
  longitude?: number;
}

export interface AiAnalysis {
  summary?: string;
  detailedDescription?: string;
  extractedText?: string;
  detectedObjects?: string[];
  detectedPeople?: string[];
  detectedCategories?: string[];
  detectedEmotions?: string[];
}

export interface Memory {
  id: string;
  userId: string;
  type: 'PHOTO' | 'VIDEO' | 'NOTE' | 'DOCUMENT' | 'LOCATION_EVENT' | 'MANUAL_EVENT';
  title?: string;
  description?: string;
  occurredAt: string;
  location?: LocationPoint;
  media: MediaItem[];
  peopleIds: string[];
  objectIds: string[];
  tags: string[];
  aiAnalysis?: AiAnalysis;
  processingStatus: 'PENDING_STORAGE' | 'PENDING_AI' | 'PROCESSED' | 'FAILED';
  createdAt: string;
  updatedAt: string;
}

export interface Person {
  id: string;
  name: string;
  relationship?: string;
  notes?: string;
  avatarStoragePath?: string;
  interactionCount: number;
}

export interface LocationEntity {
  id: string;
  name: string;
  category?: string;
  address?: string;
  visitCount: number;
}

export interface ObjectEntity {
  id: string;
  name: string;
  category?: string;
  notes?: string;
}
