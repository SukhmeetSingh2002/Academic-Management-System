package org.academic.User;

public interface User {
    String SessionID = null;
    String getUserName();
    String getPassword();

    String[] getOptions();

    String updateProfile(String password);


}
