package com.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        User savedUser = userService.createUser(user);
        return ResponseEntity
                .created(URI.create("/users/" + savedUser.getUserId()))
                .body(savedUser);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(@PathVariable Long userId, @RequestBody User user) {
        return userService.updateUser(userId, user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        if (userService.deleteUser(userId)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/byNamePaginated")
    public ResponseEntity<List<User>> getAllUsersByPage(
            @RequestParam String userName,
            @RequestParam Integer page,
            @RequestParam Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        List<User> users = userService.getUsersByNameWithPagination(userName, pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/byNameContains")
    public ResponseEntity<List<User>> getUsersBySearch(@RequestParam String userName) {
        List<User> users = userService.getUsersByNameContains(userName);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search/byNameAndEmail")
    public ResponseEntity<List<User>> getUserByUserNameAndEmail(
            @RequestParam String userName,
            @RequestParam String email) {
        List<User> users = userService.getUsersByNameAndEmail(userName, email);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search/byNameOrEmail")
    public ResponseEntity<List<User>> getUserByUserNameOrEmail(
            @RequestParam String userName,
            @RequestParam String email) {
        List<User> users = userService.getUsersByNameOrEmail(userName, email);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search/byNameContains")
    public ResponseEntity<List<User>> getUserByUserNameContains(
            @RequestParam String userName) {
        List<User> users = userService.getUsersByNameContains(userName);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search/byNameContainsOrEmailContains")
    public ResponseEntity<List<User>> getUserByUserNameContainsOrEmailContains(
            @RequestParam String userName,
            @RequestParam String emailId) {
        List<User> users = userService.getUsersByNameContainsOrEmailContains(userName, emailId);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/search/byNameAndEmailHQL")
    public ResponseEntity<List<User>> getUsersByUserNameEmailIdHQL(
            @RequestParam String userName,
            @RequestParam String emailId) {
        List<User> users = userService.getUsersByNameAndEmailHQL(userName, emailId);
        if (users.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/age/{age}")
    public ResponseEntity<List<User>> getUsersByAge(@PathVariable Integer age) {
        List<User> users = userService.getUsersByAge(age);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/age/greater-than/{age}")
    public ResponseEntity<List<User>> getUsersByAgeGreaterThan(@PathVariable Integer age) {
        List<User> users = userService.getUsersByAgeGreaterThan(age);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/age/less-than/{age}")
    public ResponseEntity<List<User>> getUsersByAgeLessThan(@PathVariable Integer age) {
        List<User> users = userService.getUsersByAgeLessThan(age);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/age/between")
    public ResponseEntity<List<User>> getUsersByAgeBetween(
            @RequestParam Integer minAge,
            @RequestParam Integer maxAge) {
        List<User> users = userService.getUsersByAgeBetween(minAge, maxAge);
        return ResponseEntity.ok(users);
    }
}