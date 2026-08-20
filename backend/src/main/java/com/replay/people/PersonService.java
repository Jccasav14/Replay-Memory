package com.replay.people;

import com.replay.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonService {

    private final PersonRepository personRepository;

    public List<Person> listPeople(String userId) {
        return personRepository.findByUserIdOrderByNameAsc(userId);
    }

    public Person createPerson(Person person, String userId) {
        person.setUserId(userId);
        return personRepository.save(person);
    }

    public Person getPerson(String id, String userId) {
        return personRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Person with ID " + id + " not found"));
    }
}
