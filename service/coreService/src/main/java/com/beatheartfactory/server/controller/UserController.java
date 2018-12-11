package com.beatheartfactory.server.controller;

import com.beatheartfactory.server.DatabaseController;
import com.beatheartfactory.server.api.UsersApi;
import com.beatheartfactory.server.model.Activity;
import com.beatheartfactory.server.model.GameStatistics;
import com.beatheartfactory.server.model.User;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.awt.image.DataBuffer;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.lang.Math.random;

@RestController
public class UserController implements UsersApi {

    private Logger LOG = Logger.getLogger(UserController.class.getName());

    @Autowired
    private DatabaseController controller;

    private Map<Integer, User> users = ImmutableMap.of();
    private static int id = 100 + (int) (random() * 1000);

    @Override
    public ResponseEntity<List<User>> getUsers() {
        try {
            return ResponseEntity.ok(controller.getUsers());
        } catch (SQLException e) {
            LOG.warning("Exception happened during db query" + e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<User> getUserById(@PathVariable Integer userId) {
        try {
            return ResponseEntity.ok(controller.getUserById(userId));
        } catch (SQLException e) {
            LOG.warning("Exception happened during db query" + e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<Void> postGameStats(@PathVariable Integer userId, @RequestBody GameStatistics body) {
        if (users.containsKey(userId)) {
            LOG.info(String.format("User with id %d has a new GameStatistic %s", userId, body));
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Void> postStats(@PathVariable Integer userId, @RequestBody Activity body) {
        if (users.containsKey(userId)) {
            LOG.info(String.format("User with id %d has a new Activity %s", userId, body));
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<Integer> postUser(@RequestBody User body) {
        if (!checkUser(body)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        int actualId = id;
        body.setId(actualId);
        try {
            id++;
            controller.putUser(body);
            LOG.info(String.format("Added new user %s with id %d", users, id));
            return ResponseEntity.ok(actualId);
        } catch (SQLException e) {
            LOG.warning(e.toString());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private boolean checkUser(User user) {
        if (user.getFirstname() == null || user.getLastname() == null || user.getDescription() == null) {
            return false;
        }
        return true;
    }


}
