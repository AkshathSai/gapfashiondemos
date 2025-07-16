package com.demo.user;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User createUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> updateUser(Long userId, User user) {
        return userRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setUserName(user.getUserName());
                    existingUser.setEmailId(user.getEmailId());
                    existingUser.setAge(user.getAge());
                    return userRepository.save(existingUser);
                });
    }

    @Override
    public boolean deleteUser(Long userId) {
        if (userRepository.existsById(userId)) {
            userRepository.deleteById(userId);
            return true;
        }
        return false;
    }

    @Override
    public List<User> getUsersByNameWithPagination(String userName, Pageable pageable) {
        return userRepository.findByUserNameContains(userName, pageable);
    }

    @Override
    public List<User> getUsersByNameContains(String userName) {
        return userRepository.findByUserNameContainsOrderByUserNameAsc(userName);
    }

    @Override
    public List<User> getUsersByNameAndEmail(String userName, String email) {
        return userRepository.findByUserNameAndEmailId(userName, email);
    }

    @Override
    public List<User> getUsersByNameOrEmail(String userName, String email) {
        return userRepository.findByUserNameOrEmailId(userName, email);
    }

    @Override
    public List<User> getUsersByNameContainsOrEmailContains(String userName, String emailId) {
        return userRepository.findByUserNameContainsOrEmailIdContains(userName, emailId);
    }

    @Override
    public List<User> getUsersByNameAndEmailHQL(String userName, String emailId) {
        return userRepository.getUsersByUserNameEmailId(userName, emailId);
    }

    // Age-related method implementations
    @Override
    public List<User> getUsersByAge(Integer age) {
        return userRepository.findByAge(age);
    }

    @Override
    public List<User> getUsersByAgeGreaterThan(Integer age) {
        return userRepository.findByAgeGreaterThan(age);
    }

    @Override
    public List<User> getUsersByAgeLessThan(Integer age) {
        return userRepository.findByAgeLessThan(age);
    }

    @Override
    public List<User> getUsersByAgeBetween(Integer minAge, Integer maxAge) {
        return userRepository.findByAgeBetween(minAge, maxAge);
    }
}