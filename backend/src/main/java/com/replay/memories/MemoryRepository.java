package com.replay.memories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemoryRepository extends MongoRepository<Memory, String> {

    Page<Memory> findByUserIdAndIsDeletedFalseOrderByOccurredAtDesc(String userId, Pageable pageable);

    Optional<Memory> findByIdAndUserIdAndIsDeletedFalse(String id, String userId);

    @Query("{ 'userId': ?0, 'isDeleted': false, 'occurredAt': { $gte: ?1, $lte: ?2 } }")
    List<Memory> findByUserIdAndOccurredAtBetween(String userId, Instant start, Instant end);

    List<Memory> findByUserIdAndProcessingStatusAndIsDeletedFalse(String userId, ProcessingStatus status);

    long countByUserIdAndIsDeletedFalse(String userId);
}
