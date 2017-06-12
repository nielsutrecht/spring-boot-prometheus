package com.nibado.example.springprometheus.user;

import com.nibado.example.springprometheus.user.domain.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class UserService {
    private final Map<UUID, User> sessions = new HashMap<>();
    private final List<User> users = new ArrayList<>();

    public UUID startSession(final String email, final String password) {
        if(email == null || password == null) {
            throw new IllegalArgumentException("Email and password are required");
        }

        User user = users
                .stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Could not find user with email " + email));

        if(!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("User password invalid");
        }

        UUID sessionId = UUID.randomUUID();

        sessions.put(sessionId, user);

        return sessionId;
    }

    public User getSession(final UUID sessionId) {
        if(!sessions.containsKey(sessionId)) {
            throw new IllegalArgumentException("Invalid session id");
        } else {
            return sessions.get(sessionId);
        }
    }

    public void endSession(final UUID sessionId) {
        sessions.remove(sessionId);
    }

    @PostConstruct
    public void init() {
        users.add(new User("tom@example.com", "secret"));
        users.add(new User("sally@example.com", "supersecret"));
    }
}
