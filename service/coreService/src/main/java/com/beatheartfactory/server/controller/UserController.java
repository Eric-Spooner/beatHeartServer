package com.beatheartfactory.server.controller;

import com.beatheartfactory.server.api.UserApi;
import com.beatheartfactory.server.model.Activity;
import com.beatheartfactory.server.model.GameStatistics;
import com.beatheartfactory.server.model.User;
import com.google.common.collect.ImmutableMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.logging.Logger;

@RestController
public class UserController implements UserApi {

    private Logger LOG = Logger.getLogger(UserController.class.getName());

    private Map<Integer, User> users = ImmutableMap.of();
    private static int id = 0;

    @Override
    public ResponseEntity<User> getUser(@PathVariable Integer userId) {
        if (users.containsKey(userId)) {
            return ResponseEntity.ok(users.get(userId));
        }
        LOG.info(String.format("User with id %d is not included in the list", userId));
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Void> postGameStats(@PathVariable Integer userId, @RequestBody GameStatistics body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Void> postStats(@PathVariable Integer userId, @RequestBody Activity body) {
        return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
    }

    @Override
    public ResponseEntity<Integer> postUser(@RequestBody User body) {
        int actualId = id;
        body.setId(actualId);
        id++;
        this.users = new ImmutableMap.Builder<Integer, User>().putAll(users).put(actualId, body).build();
        LOG.info(String.format("Added new user %s with id %d", users, id));
        return ResponseEntity.ok(actualId);
    }
}
