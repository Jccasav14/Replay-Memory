package com.replay.objects;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObjectRepository extends MongoRepository<ObjectEntity, String> {

    List<ObjectEntity> findByUserIdOrderByNameAsc(String userId);

    Optional<ObjectEntity> findByIdAndUserId(String id, String userId);
}
