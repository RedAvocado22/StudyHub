package com.studyhub.service;

import com.studyhub.model.User;
import com.studyhub.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserProfileService {
    UserRepository userRepository;
    @Autowired
    public UserProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.
                findById(id).
                orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
    }

    public void save(User existingUser) {
        userRepository.save(existingUser);
    }
}
