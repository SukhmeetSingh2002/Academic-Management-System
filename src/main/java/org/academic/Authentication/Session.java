package org.academic.Authentication;

import org.academic.User.UserType;

import java.util.Date;

public class Session {
    private static Session session = null;
    private String userName;
    private String password;
    private UserType userType;
    private Date loginTime;
    private Date logoutTime;


    /**
     * @return the loginTime
     */

    public Date getLoginTime() {
        return loginTime;
    }

    /**
     * @return logout time
     */
    public Date getLogoutTime() {
        return logoutTime;
    }

    public static Session getInstance() {
        if (session == null) {
            session = new Session();
        }
        return session;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    private void setUserName(String userName) {
        this.userName = userName;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    public UserType getUserType() {
        return userType;
    }

    private void setUserType(UserType userType) {
        this.userType = userType;
    }

    public void setSession(String userName, String password, UserType userType) {
        this.userName = userName;
        this.password = password;
        this.userType = userType;
        this.loginTime = new Date();
//        TODO: set time in database
    }

    public void clearSession() {
        this.userName = null;
        this.password = null;
        this.userType = null;
        this.logoutTime = new Date();
    }

    public boolean isSessionActive() {
        return this.userName != null && this.password != null && this.userType != null;
    }


}
