package org.academic.Services;

import org.academic.Database.Connector;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.academic.User.UserType;
import org.academic.cli.OutputHandler;


public class AuthDetails {
    //    get the username and password from the database
    private static final Connection conn = Connector.getConnection();

    public static UserType verifyUser(String userName, String password) throws SQLException {
        if (conn == null) {
            return null;
        }
        String userQuery = "SELECT * FROM user_authentication WHERE user_name = '" + userName + "' AND password = '" + password + "'";

        ResultSet rs = conn.createStatement().executeQuery(userQuery);
        if (rs.next()) {
            return UserType.valueOf(rs.getString("role"));
        } else {
            return null;
        }

    }

    public static void loginLog(String userName, String loginTime, String sessionID) throws SQLException {
        if (conn == null) {
            return;
        }
        String query = "INSERT INTO login_log (user_name, login_time, login_id,is_logged_in ) VALUES ('%s', '%s', '%s', %b)".formatted(userName, loginTime, sessionID, true);
        OutputHandler.logError(query);
        conn.createStatement().executeUpdate(query);
    }

    public static void logoutLog(String userName, String logoutTime, String sessionID) throws SQLException {
        if (conn == null) {
            return;
        }
        String query = "UPDATE login_log SET logout_time = '%s', is_logged_in = %b WHERE user_name = '%s' AND login_id = '%s'".formatted(logoutTime, false, userName, sessionID);
        conn.createStatement().executeUpdate(query);
    }
}
