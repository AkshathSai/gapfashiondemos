package com.demo.user;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> getAllUsers();
    User createUser(User user);
    Optional<User> getUserById(Long userId);
    Optional<User> updateUser(Long userId, User user);
    boolean deleteUser(Long userId);
    List<User> getUsersByNameWithPagination(String userName, Pageable pageable);
    List<User> getUsersByNameContains(String userName);
    List<User> getUsersByNameAndEmail(String userName, String email);
    List<User> getUsersByNameOrEmail(String userName, String email);
    List<User> getUsersByNameContainsOrEmailContains(String userName, String emailId);
    List<User> getUsersByNameAndEmailHQL(String userName, String emailId);
    List<User> getUsersByAge(Integer age);
    List<User> getUsersByAgeGreaterThan(Integer age);
    List<User> getUsersByAgeLessThan(Integer age);
    List<User> getUsersByAgeBetween(Integer minAge, Integer maxAge);
}
