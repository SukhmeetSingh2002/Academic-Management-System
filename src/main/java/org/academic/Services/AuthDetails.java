package org.academic.Services;

import org.academic.Database.Connector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.academic.User.UserType;


public class AuthDetails {
    //    get the username and password fr  om the database
    private static final Connection conn = Connector.getConnection();

    public static UserType verifyUser(String userName, String password) throws SQLException {
        if (conn == null) {
            return null;
        }
        String userQuery = "SELECT * FROM user_authentication WHERE user_name = '" + userName + "' AND password = '" + password + "'";

        ResultSet rs = conn.createStatement().executeQuery(userQuery);
        if (rs.next()) {
            return UserType.valueOf(rs.getString("user_type"));
        } else {
            return null;
        }

    }
}
