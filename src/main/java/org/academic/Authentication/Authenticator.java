package org.academic.Authentication;

import org.academic.User.UserType;
import org.academic.Services.AuthDetails;
import org.academic.cli.OutputHandler;

import java.sql.SQLException;


public class Authenticator {
    public static boolean authenticate(String userName, String password) throws SQLException {
        UserType userType = AuthDetails.verifyUser(userName, password);
        if (userType == null) {
            OutputHandler.logError("Invalid username or password");
            return false;
        } else {
            Session.getInstance().setSession(userName, password, userType, String.valueOf(System.currentTimeMillis()));
            OutputHandler.logError("Login successful " + userName + " " + userType + " " + Session.getInstance().getSessionId());
            AuthDetails.loginLog(userName, java.time.LocalDateTime.now().toString(), Session.getInstance().getSessionId());
            return true;
        }
    }

    public static void logout() throws SQLException {
        OutputHandler.logError("Logging out" + Session.getInstance().getUserName() + " " + Session.getInstance().getSessionId());
        AuthDetails.logoutLog(Session.getInstance().getUserName(), java.time.LocalDateTime.now().toString(), Session.getInstance().getSessionId());
        Session.getInstance().clearSession();
    }

}
