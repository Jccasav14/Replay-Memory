package com.replay.people;

import com.replay.auth.User;
import com.replay.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/people")
@RequiredArgsConstructor
public class PersonController {

    private final PersonService personService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Person>>> listPeople(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(personService.listPeople(currentUser.getId())));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Person>> createPerson(
            @RequestBody Person person,
            @AuthenticationPrincipal User currentUser) {
        Person created = personService.createPerson(person, currentUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(created, "Person created successfully"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Person>> getPerson(
            @PathVariable String id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(ApiResponse.ok(personService.getPerson(id, currentUser.getId())));
    }
}
