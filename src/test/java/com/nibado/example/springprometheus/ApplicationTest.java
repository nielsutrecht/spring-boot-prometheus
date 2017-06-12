package com.nibado.example.springprometheus;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nibado.example.springprometheus.user.domain.User;
import com.nibado.example.springprometheus.user.domain.UserLogin;
import com.nibado.example.springprometheus.user.domain.UserLoginResponse;
import com.squareup.okhttp.*;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;


/**
 * Simulator that does a bunch of requests on the service so we are able to see some Prometheus output.
 */
public class ApplicationTest {
    private static String BASE_URL = "http://localhost:8080/";
    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final UserLogin[] LOGINS = new UserLogin[]{
            new UserLogin("tom@example.com", "secret"),
            new UserLogin("sally@example.com", "supersecret")};

    private final OkHttpClient client = new OkHttpClient();

    public void simulate() {
        try {
            prometheusInfo();
        } catch(Exception e) {
            System.out.println("Service does not seem to be running on " + BASE_URL);
            return;
        }
        getUser(new UUID(0, 0));
        getUser(new UUID(0, 0));
        startSession(new UserLogin("test", "test"));
        startSession(new UserLogin("tom@example.com", "test"));

        for (int i = 0; i < 4; i++) {
            startSession(LOGINS[i % 2]).ifPresent(sessionId -> {
                getUser(sessionId);
                getUser(sessionId);
                endSession(sessionId);
            });
        }

        try {
            System.out.println(prometheusInfo());
        } catch(Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Optional<UUID> startSession(UserLogin login) {
        try {
            RequestBody body = RequestBody.create(JSON, MAPPER.writeValueAsString(login));
            Request request = new Request.Builder()
                    .url(BASE_URL + "user")
                    .post(body)
                    .build();

            Response response = client.newCall(request).execute();

            return Optional.of(MAPPER.readValue(response.body().string(), UserLoginResponse.class).getToken());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private Optional<User> getUser(UUID sessionId) {
        Request request = new Request.Builder()
                .url(BASE_URL + "user")
                .header("session-id", sessionId.toString())
                .get()
                .build();

        try {
            Response response = client.newCall(request).execute();

            return Optional.of(MAPPER.readValue(response.body().string(), User.class));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private void endSession(UUID sessionId) {
        Request request = new Request.Builder()
                .url(BASE_URL + "user")
                .header("session-id", sessionId.toString())
                .delete()
                .build();

        try {
            client.newCall(request).execute();
        } catch (Exception e) {
        }
    }

    private String prometheusInfo() throws IOException {
        Request request = new Request.Builder()
                .url(BASE_URL + "prometheus")
                .get()
                .build();


        Response response = client.newCall(request).execute();

        return response.body().string();
    }

    public static void main(String... argv) {
        new ApplicationTest().simulate();
    }
}
