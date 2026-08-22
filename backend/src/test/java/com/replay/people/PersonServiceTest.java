package com.replay.people;

import com.replay.common.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PersonServiceTest {

    @Mock
    private PersonRepository personRepository;

    @InjectMocks
    private PersonService personService;

    private final String userId = "user-test-123";
    private Person samplePerson;

    @BeforeEach
    void setUp() {
        samplePerson = Person.builder()
                .id("person-1")
                .userId(userId)
                .name("Alex Smith")
                .relationship("Friend")
                .notes("Colleague from university")
                .build();
    }

    @Test
    @DisplayName("Should list all people belonging to a user")
    void listPeople_ReturnsUserPeople() {
        when(personRepository.findByUserIdOrderByNameAsc(userId)).thenReturn(List.of(samplePerson));

        List<Person> people = personService.listPeople(userId);

        assertThat(people).hasSize(1);
        assertThat(people.get(0).getName()).isEqualTo("Alex Smith");
    }

    @Test
    @DisplayName("Should save and return a new person for user")
    void createPerson_Success() {
        Person newPerson = Person.builder()
                .name("Carlos Gomez")
                .relationship("Family")
                .build();

        when(personRepository.save(any(Person.class))).thenAnswer(invocation -> {
            Person p = invocation.getArgument(0);
            p.setId("person-2");
            return p;
        });

        Person created = personService.createPerson(newPerson, userId);

        assertThat(created).isNotNull();
        assertThat(created.getId()).isEqualTo("person-2");
        assertThat(created.getUserId()).isEqualTo(userId);
        assertThat(created.getName()).isEqualTo("Carlos Gomez");
    }

    @Test
    @DisplayName("Should find person by id and userId")
    void getPerson_Found_ReturnsPerson() {
        when(personRepository.findByIdAndUserId("person-1", userId)).thenReturn(Optional.of(samplePerson));

        Person found = personService.getPerson("person-1", userId);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo("person-1");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when person not found")
    void getPerson_NotFound_ThrowsException() {
        when(personRepository.findByIdAndUserId("unknown-id", userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personService.getPerson("unknown-id", userId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }
}
