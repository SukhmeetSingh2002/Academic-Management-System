package org.academic.User;

public interface User {
    String SessionID = null;
    String getUserName();
    String getPassword();

    void setUserName(String userName);
    void setPassword(String password);

    String[] getOptions();

    String updateProfile(String password);


}
