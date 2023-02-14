package org.academic.Authentication;

import org.academic.User.UserType;
import org.academic.Services.AuthDetails;

import java.sql.SQLException;


public class Authenticator {
    public static boolean authenticate(String userName, String password) throws SQLException {
        UserType userType = AuthDetails.verifyUser(userName, password);
        if (userType == null) {
            return false;
        } else {
            Session.getInstance().setSession(userName, password, userType);
            return true;
        }
    }

    public static void logout() {
        Session.getInstance().clearSession();
    }

}
