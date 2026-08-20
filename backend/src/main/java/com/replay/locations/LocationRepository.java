package com.replay.locations;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends MongoRepository<Location, String> {

    List<Location> findByUserIdOrderByNameAsc(String userId);

    Optional<Location> findByIdAndUserId(String id, String userId);

    Optional<Location> findByUserIdAndNameIgnoreCase(String userId, String name);
}
