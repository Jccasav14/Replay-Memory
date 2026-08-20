package com.replay.objects;

import com.replay.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;

    public List<ObjectEntity> listObjects(String userId) {
        return objectRepository.findByUserIdOrderByNameAsc(userId);
    }

    public ObjectEntity createObject(ObjectEntity object, String userId) {
        object.setUserId(userId);
        return objectRepository.save(object);
    }

    public ObjectEntity getObject(String id, String userId) {
        return objectRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Object with ID " + id + " not found"));
    }
}
