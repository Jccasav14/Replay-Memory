package com.replay.objects;

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
@Document(collection = "objects")
@CompoundIndex(name = "user_name_idx", def = "{'userId': 1, 'name': 1}")
public class ObjectEntity {

    @Id
    private String id;
    private String userId;
    private String name;
    private String category; // DEVICE, VEHICLE, TOOL, JEWELRY, DOCUMENT, OTHER
    private String serialNumber;
    private LocalDate acquisitionDate;
    private String photoStoragePath;
    private String notes;

    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
}
