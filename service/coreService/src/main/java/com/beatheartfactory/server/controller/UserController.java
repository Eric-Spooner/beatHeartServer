package com.beatheartfactory.server.controller;

import com.beatheartfactory.server.api.UserApi;
import com.beatheartfactory.server.model.Activity;
import com.beatheartfactory.server.model.GameStatistics;
import com.beatheartfactory.server.model.User;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class UserController implements UserApi {

    private Map<Integer, User> users = ImmutableMap.of();
    private static int id = 0;

    @Override
    public ResponseEntity<User> getUser(Integer userId) {
        if (users.containsKey(userId)) {
            return ResponseEntity.ok(users.get(userId));
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Void> postGameStats(Integer userId, GameStatistics body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Void> postStats(Integer userId, Activity body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Integer> postUser(User body) {
        int actualId = id;
        body.setId(actualId);
        id++;
        this.users = new ImmutableMap.Builder<Integer, User>().putAll(users).put(actualId, body).build();
        return ResponseEntity.ok(actualId);
    }
}
