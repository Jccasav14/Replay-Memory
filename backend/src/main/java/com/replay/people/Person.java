package com.replay.people;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "people")
@CompoundIndex(name = "user_name_idx", def = "{'userId': 1, 'name': 1}")
public class Person {

    @Id
    private String id;
    private String userId;
    private String name;
    private String relationship; // FRIEND, FAMILY, COLLEAGUE, ACQUAINTANCE
    private String avatarStoragePath;
    private String notes;
    private LocalDate firstMetDate;
    @Builder.Default
    private int interactionCount = 0;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
