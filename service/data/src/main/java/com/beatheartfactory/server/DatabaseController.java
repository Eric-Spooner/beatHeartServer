package com.beatheartfactory.server;


import com.beatheartfactory.server.model.User;
import org.springframework.stereotype.Controller;

import java.sql.*;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

@Controller
public class DatabaseController {

    private Logger LOG = Logger.getLogger(DatabaseController.class.getName());

    // JDBC driver name and database URL
    private static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private static final String DB_URL = "jdbc:mysql://340.hostserv.eu/usr_web35_1";

    //  Database credentials
    private static final String USER = "web35";
    private static final String PASS = "&b9gsBHF";

    private Connection conn;

    public DatabaseController() {
        conn = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");

            //STEP 3: Open a connection
            LOG.info("Connecting to a selected database...");
            conn = DriverManager.getConnection(DB_URL, USER, PASS);
            LOG.info("Connected database successfully...");
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            LOG.warning(String.format("Unable to connect to database caused by: %s", e));
        }
    }

    public User getUserById(int id) throws SQLException {
        Statement statement = conn.createStatement();
        String sql = "Select userId, Username, Firstname, Lastname, users_description from bhf_users WHERE userId = %d";
        ResultSet resultSet = statement.executeQuery(String.format(sql, id));
        LinkedList<User> users = new LinkedList<>();
        while (resultSet.next()) {
            users.add(new User().username(resultSet.getString("Username"))
                    .firstname(resultSet.getString("Firstname"))
                    .lastname(resultSet.getString("Lastname"))
                    .description(resultSet.getString("users_description"))
                    .id(resultSet.getInt("UserId")));
        }
        return users.getFirst();
    }

    public List<User> getUsers() throws SQLException {
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("Select userId, Username, Firstname, Lastname, users_description from bhf_users");
        List<User> users = new LinkedList<>();
        while (resultSet.next()) {
            users.add(new User().username(resultSet.getString("Username"))
                    .firstname(resultSet.getString("Firstname"))
                    .lastname(resultSet.getString("Lastname"))
                    .description(resultSet.getString("users_description"))
                    .id(resultSet.getInt("UserId")));
        }
        return users;
    }

    public boolean putUser(User user) throws SQLException {
        Statement statement = conn.createStatement();
        String statment = "INSERT INTO bhf_users (UserId, Username, Firstname, Lastname, users_description)" +
                " VALUES (%d, '%s', '%s', '%s', '%s')";
        return statement.execute(String.format(statment, user.getId(), user.getUsername(),
                user.getFirstname(), user.getLastname(), user.getDescription()));
    }

}

