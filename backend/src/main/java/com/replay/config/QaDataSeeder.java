package com.replay.config;

import com.replay.auth.Role;
import com.replay.auth.User;
import com.replay.auth.UserRepository;
import com.replay.memories.Memory;
import com.replay.memories.MemoryRepository;
import com.replay.memories.MemoryType;
import com.replay.memories.ProcessingStatus;
import com.replay.people.Person;
import com.replay.people.PersonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Seeds initial demo/QA data when starting up in dev, qa or default profiles.
 * Provides a ready-to-test user account and realistic autobiographical memory records.
 */
@Slf4j
@Component
@Profile("!prod")
@RequiredArgsConstructor
public class QaDataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MemoryRepository memoryRepository;
    private final PersonRepository personRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("QA Data Seeder: Existing database detected. Skipping seed.");
            return;
        }

        log.info("QA Data Seeder: Initializing demo accounts and sample memories...");

        // 1. Create QA Demo User
        User demoUser = User.builder()
                .email("demo@replay.app")
                .fullName("Demo Explorer")
                .passwordHash(passwordEncoder.encode("Password123!"))
                .role(Role.ROLE_USER)
                .build();
        User savedUser = userRepository.save(demoUser);
        String userId = savedUser.getId();

        // 2. Create QA People
        Person personMom = personRepository.save(Person.builder()
                .userId(userId)
                .name("Elena Rostova")
                .relationship("Family (Mother)")
                .notes("Loves coffee and gardening")
                .build());

        Person personBestFriend = personRepository.save(Person.builder()
                .userId(userId)
                .name("Marcus Vance")
                .relationship("Best Friend")
                .notes("Software architect and cycling partner")
                .build());

        // 3. Create Sample Memories across timeline
        Memory mem1 = Memory.builder()
                .userId(userId)
                .title("Trip to Coffee Plantation")
                .description("Spent the weekend touring the coffee valley in Colombia. Amazing weather, warm aroma and great stories.")
                .type(MemoryType.NOTE)
                .occurredAt(Instant.now().minus(14, ChronoUnit.DAYS))
                .peopleIds(List.of(personMom.getId()))
                .tags(List.of("travel", "coffee", "nature", "family"))
                .processingStatus(ProcessingStatus.PROCESSED)
                .build();

        Memory mem2 = Memory.builder()
                .userId(userId)
                .title("Hackathon Weekend Victory")
                .description("Built the first version of the Life Graph engine in 48 hours. Team took first place for AI UX innovation!")
                .type(MemoryType.NOTE)
                .occurredAt(Instant.now().minus(5, ChronoUnit.DAYS))
                .peopleIds(List.of(personBestFriend.getId()))
                .tags(List.of("coding", "ai", "hackathon", "milestone"))
                .processingStatus(ProcessingStatus.PROCESSED)
                .build();

        Memory mem3 = Memory.builder()
                .userId(userId)
                .title("Morning Run at the Park")
                .description("Completed a crisp 5k morning run. Felt energizing and clear-headed before starting the sprint.")
                .type(MemoryType.NOTE)
                .occurredAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .tags(List.of("fitness", "health", "morning"))
                .processingStatus(ProcessingStatus.PROCESSED)
                .build();

        memoryRepository.saveAll(List.of(mem1, mem2, mem3));
        log.info("QA Data Seeder: Successfully seeded demo user (demo@replay.app), 2 people, and 3 timeline memories.");
    }
}
