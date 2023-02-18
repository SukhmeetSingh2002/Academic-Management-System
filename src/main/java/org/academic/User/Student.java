package org.academic.User;

import org.academic.Database.CourseRegisterDTO;
import org.academic.Database.Course_Offerings_DTO;
import org.academic.Services.EventLogger;
import org.academic.Database.GradeDTO;
import org.academic.Authentication.Session;
import org.academic.Services.Course_Offerings;
import org.academic.Services.Course_catalog;
import org.academic.Services.StudentDAL;
import org.academic.cli.OutputHandler;

public class Student implements User {
    private String userName;
    private String password;
    private final String entryNumber;
    private String sessionID;

    private boolean isNotAuthorized() {
        Session session = Session.getInstance();
        if (!session.isSessionActive() || !session.getUserName().equals(this.userName) || !session.getUserType().equals(UserType.STUDENT)) {
            return true;
        }
        this.sessionID = session.getSessionId();
        return false;
    }

    public Student(String userName, String password, String entryNumber) {
        this.userName = userName;
        this.password = password;
        this.entryNumber = entryNumber;
    }

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

//    return a list of options available to the user

    /**
     * @return the options
     */
    public String[] getOptions() {
        return new String[]{"Enroll in a course", "Drop a course", "View prerequisites", "View courses offered", "View courses registered", "View grades", "Edit profile", "Logout"};
/*
        TODO: add the following options
         1. Enroll in a course
         2. Drop a course
         3. View prerequisites
         5. View courses offered
         6. View courses registered
         7. View grades
         8. Edit profile
         9. Logout
*/
    }

    private Boolean updatePassword(String password) {
        if (StudentDAL.changePassword(this.userName, this.password, password)) {
            this.password = password;
            return true;
        } else {
            return false;
        }


    }

    /**
     * @param password Password to be updated
     * @return String message to be displayed to the user
     */
    @Override
    public String updateProfile( String password) {
//        change password
        if (password != null) {
            if (this.updatePassword(password)) {
                EventLogger.logEvent(this.userName,"Password updated successfully",java.time.LocalDateTime.now().toString(),this.sessionID);
                OutputHandler.logError("Password updated successfully");
                return "Password updated successfully";
            } else {
                EventLogger.logEvent(this.userName,"Password update failed",java.time.LocalDateTime.now().toString(),this.sessionID);
                OutputHandler.logError("Password update failed");
                return "Password update failed";
            }
        } else {
            OutputHandler.logError("Password is null");
            return "Password is null";
        }
    }

    //    enroll in a course
    public String enrollInCourse(String courseCode) {
//        check if the user is authorized to enroll in the course using session
        Session session = Session.getInstance();
        if (!session.isSessionActive() || !session.getUserName().equals(this.userName) || !session.getUserType().equals(UserType.STUDENT)) {
            return "You are not authorized to enroll in the course";
        }
//
        return "You have successfully enrolled in the course";

    }

    //    view course catalog

    public String[] viewPrerequisites(String courseCode) {
        if (isNotAuthorized()) {
            return new String[]{"You are not authorized to view the prerequisites"};
        }
        String[] prerequisites = Course_catalog.getCoursePrerequisites(courseCode);
        EventLogger.logEvent(this.userName, "viewPrerequisites", java.time.LocalDateTime.now().toString(), this.sessionID);
        return prerequisites;
    }

    //    drop a course
    public String dropCourse(String courseCode) {
//        TODO : Drop a course
        return "You have successfully dropped the course";
    }

    //    view courses offered
    public Course_Offerings_DTO[] viewCoursesOffered() {
//        check session
        Course_Offerings_DTO[] courseOfferings;
        if (isNotAuthorized()) {
            courseOfferings = new Course_Offerings_DTO[1];
            courseOfferings[0] = new Course_Offerings_DTO("Error", "You are not authorized to view the courses offered", "Error", new String[]{"Error"}, "Error");
            return courseOfferings;
        }

//        get current semester
        String currentSemester = Course_Offerings.get_current_semester();
//        get the list of courses offered in the current semester
        courseOfferings = Course_Offerings.view_course_offerings(currentSemester);

//        make log entry
        EventLogger.logEvent(this.userName, "viewCoursesOffered", java.time.LocalDateTime.now().toString(), this.sessionID);

        return courseOfferings;
    }

    //    view courses registered
    public CourseRegisterDTO[] viewCoursesRegistered() {
//        check session
        CourseRegisterDTO[] courseRegisterDTOS;
        if (isNotAuthorized()) {
            courseRegisterDTOS = new CourseRegisterDTO[1];
            courseRegisterDTOS[0] = new CourseRegisterDTO("Error", "You are not authorized to view the courses registered", "Error", "Error", "Error", "Error", "Error");
            return courseRegisterDTOS;
        }

        courseRegisterDTOS = Course_Offerings.view_course_registered(this.entryNumber);

//        make log entry
        EventLogger.logEvent(this.userName, "viewCoursesRegistered", java.time.LocalDateTime.now().toString(), this.sessionID);

        return courseRegisterDTOS;
    }

    //    view grades
    public GradeDTO[] viewGrades() {
//        check session
        GradeDTO[] gradeDTOS;
        if (isNotAuthorized()) {
            gradeDTOS = new GradeDTO[1];
            gradeDTOS[0] = new GradeDTO("Error", "You are not authorized to view the grades", "Error", "Error");
            return gradeDTOS;
        }

        gradeDTOS = Course_Offerings.view_all_grade(this.entryNumber);

//        make log entry
        EventLogger.logEvent(this.userName, "viewGrades", java.time.LocalDateTime.now().toString(), this.sessionID);

        return gradeDTOS;
    }
    /*
    options available to student are
    1. Apply to a course offering
      ii. Check if the student has already applied to the course
      iii. Check the prerequisites
      iv. Check the CGPA constraints
      v. Check the time constraints
      vi. Check 1.25 credit rule
      vii. Check if semester is ongoing or not
    2. View courses
      i. View all the courses in the course offering
    3. View grades
      i. View the grades for all the courses completed by the student till now for each semester
    4. View Profile
      i. View the profile of the student
    5. View Prerequisites
      i. View the requisites for a course
    6. Drop a course
    7. Log out
*/
//    TODO: elective logic to be added


}
