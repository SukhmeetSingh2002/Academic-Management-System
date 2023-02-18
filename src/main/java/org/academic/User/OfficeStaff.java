package org.academic.User;

public class OfficeStaff implements User {
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
     * @return  the options available to the user
     */
    @Override
    public String[] getOptions() {
        return new String[0];
    }

    /**
     * @return change password
     */
    @Override
    public String updateProfile(String password) {
        return null;
    }

//   options available to office staff are
//    1. Report Generation
//      i. Generate a report of all the students in the university
//    2. At the time of graduation check if the student has completed all the courses in UG curriculum
//    3. Start and end a semester
//      i. Start a semester
//      ii. Classes end
//      iii. Grade submission starts
//      iv. Grade submission ends
//      v. End a semester
//    4. Add a course to the course catalog

//    Capstone project no. of credits only to be added by office staff

}
