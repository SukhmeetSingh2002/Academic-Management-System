package org.academic.User;

public class Faculty implements User {
    private String userName;
    private String password;

    /**
     * @return the userName
     */
    @Override
    public String getUserName() {
        return userName;
    }

    /**
     * @return the password
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * @param userName the userName to set
     */
    @Override
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * @param password the password to set
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return the options available to the user
     */
    @Override
    public String[] getOptions() {
        return new String[0];
    }

    /**
     * @param password new password
     * @return the status of the operation
     */
    @Override
    public String updateProfile(String password) {
        return null;
    }

//    options available to faculty are
//      1. Add a course offering
//          i. Add some CGPA constraints
//      2. Give grades to students
//          i. First download the all students details
//          ii. Then upload the grades for each student in the course offering in a csv file
//      3. View course offering
}
