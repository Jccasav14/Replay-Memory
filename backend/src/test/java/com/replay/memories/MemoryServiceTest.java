package com.replay.memories;

import com.replay.common.ResourceNotFoundException;
import com.replay.media.FileStorageService;
import com.replay.media.StoredFile;
import com.replay.memories.dto.CreateMemoryRequest;
import com.replay.memories.dto.MemoryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemoryServiceTest {

    @Mock
    private MemoryRepository memoryRepository;

    @Mock
    private FileStorageService storageService;

    @InjectMocks
    private MemoryService memoryService;

    private Memory sampleMemory;
    private final String userId = "user-test-123";

    @BeforeEach
    void setUp() {
        sampleMemory = Memory.builder()
                .id("mem-001")
                .userId(userId)
                .type(MemoryType.NOTE)
                .title("Testing Life Events")
                .description("A great day for testing software")
                .occurredAt(Instant.now())
                .processingStatus(ProcessingStatus.PROCESSED)
                .build();
    }

    @Test
    @DisplayName("Should create memory successfully without files")
    void createMemory_WithoutFiles_Success() {
        CreateMemoryRequest request = new CreateMemoryRequest();
        request.setTitle("Note Title");
        request.setDescription("Note content");
        request.setOccurredAt(Instant.now());

        when(memoryRepository.save(any(Memory.class))).thenAnswer(invocation -> {
            Memory mem = invocation.getArgument(0);
            mem.setId("mem-new-1");
            return mem;
        });

        MemoryResponse response = memoryService.createMemory(request, null, userId);

        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Note Title");
        assertThat(response.getType()).isEqualTo(MemoryType.NOTE);
        verify(memoryRepository, times(1)).save(any(Memory.class));
    }

    @Test
    @DisplayName("Should create photo memory when file is attached")
    void createMemory_WithMediaFile_Success() {
        CreateMemoryRequest request = new CreateMemoryRequest();
        request.setTitle("Photo Memory");
        request.setOccurredAt(Instant.now());

        MockMultipartFile file = new MockMultipartFile("files", "sunset.jpg", "image/jpeg", "image content".getBytes());
        StoredFile storedFile = StoredFile.builder()
                .fileId("file-999")
                .fileType("IMAGE")
                .storagePath("/storage/sunset.jpg")
                .thumbnailStoragePath("/storage/thumb_sunset.jpg")
                .mimeType("image/jpeg")
                .fileSizeBytes(12345L)
                .build();

        when(storageService.store(any(MultipartFile.class), eq(userId))).thenReturn(storedFile);
        when(memoryRepository.save(any(Memory.class))).thenAnswer(invocation -> {
            Memory mem = invocation.getArgument(0);
            mem.setId("mem-photo-1");
            return mem;
        });

        MemoryResponse response = memoryService.createMemory(request, List.of(file), userId);

        assertThat(response).isNotNull();
        assertThat(response.getMedia()).hasSize(1);
        assertThat(response.getType()).isEqualTo(MemoryType.PHOTO);
    }

    @Test
    @DisplayName("Should retrieve existing memory by ID")
    void getMemoryById_Found_ReturnsResponse() {
        when(memoryRepository.findByIdAndUserIdAndIsDeletedFalse("mem-001", userId))
                .thenReturn(Optional.of(sampleMemory));

        MemoryResponse response = memoryService.getMemoryById("mem-001", userId);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("mem-001");
        assertThat(response.getTitle()).isEqualTo("Testing Life Events");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when memory not found")
    void getMemoryById_NotFound_ThrowsException() {
        when(memoryRepository.findByIdAndUserIdAndIsDeletedFalse("unknown", userId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> memoryService.getMemoryById("unknown", userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    @DisplayName("Should soft-delete existing memory")
    void deleteMemory_Success() {
        when(memoryRepository.findByIdAndUserIdAndIsDeletedFalse("mem-001", userId))
                .thenReturn(Optional.of(sampleMemory));

        memoryService.deleteMemory("mem-001", userId);

        assertThat(sampleMemory.isDeleted()).isTrue();
        verify(memoryRepository, times(1)).save(sampleMemory);
    }

    @Test
    @DisplayName("Should return paginated list of user memories")
    void listMemories_ReturnsPage() {
        Page<Memory> page = new PageImpl<>(List.of(sampleMemory));
        when(memoryRepository.findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(eq(userId), any(PageRequest.class)))
                .thenReturn(page);

        Page<MemoryResponse> responses = memoryService.listMemories(userId, 0, 10);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo("mem-001");
    }
}
