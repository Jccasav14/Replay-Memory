package com.replay.people;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends MongoRepository<Person, String> {

    List<Person> findByUserIdOrderByNameAsc(String userId);

    Optional<Person> findByIdAndUserId(String id, String userId);

    Optional<Person> findByUserIdAndNameIgnoreCase(String userId, String name);
}
