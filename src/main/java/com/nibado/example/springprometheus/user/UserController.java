package com.nibado.example.springprometheus.user;

import com.nibado.example.springprometheus.user.domain.User;
import com.nibado.example.springprometheus.user.domain.UserLogin;
import com.nibado.example.springprometheus.user.domain.UserLoginResponse;
import io.prometheus.client.Summary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/user")
public class UserController {
    static final Summary userRequests = Summary.build()
            .name("user_requests_seconds")
            .help("User request latency in seconds.")
            .labelNames("method")
            .register();

    private final UserService service;

    @Autowired
    public UserController(final UserService service) {
        this.service = service;
    }

    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
    @ResponseBody
    public UserLoginResponse startSession(@RequestBody UserLogin login) {
        Summary.Timer requestTimer = userRequests.labels("start_session").startTimer();

        try {
            return new UserLoginResponse(service.startSession(login.getEmail(), login.getPassword()));
        } finally {
            requestTimer.observeDuration();
        }
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public User getUser(@RequestHeader("session-id") final UUID sessionId) {
        Summary.Timer requestTimer = userRequests.labels("get_user").startTimer();

        try {
            return service.getSession(sessionId);
        } finally {
            requestTimer.observeDuration();
        }
    }

    @RequestMapping(value = "", method = RequestMethod.DELETE)
    @ResponseBody
    public void endSession(@RequestHeader("session-id") final UUID sessionId) {
        Summary.Timer requestTimer = userRequests.labels("end_session").startTimer();

        try {
            service.endSession(sessionId);
        } finally {
            requestTimer.observeDuration();
        }
    }
}
