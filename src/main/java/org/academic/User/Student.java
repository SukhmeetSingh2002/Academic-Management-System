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

import java.util.Arrays;

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

    //    return a list of options available to the user

    /**
     * @return the options
     */
    public String[] getOptions() {
        return new String[]{"Enroll in a course", "Drop a course", "View prerequisites", "View courses offered", "View courses registered", "View grades", "Edit profile", "Logout"};
/*
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

        // check if the course is offered in the current semester
        Course_Offerings_DTO[] courseOfferings = this.viewCoursesOffered();
        boolean isCourseOffered = false;
        for (Course_Offerings_DTO courseOffering : courseOfferings) {
            if (courseOffering.course_code().equals(courseCode)) {
                isCourseOffered = true;
                break;
            }
        }
        if (!isCourseOffered) {
            return "The course is not offered in the current semester";
        }

        // check if the student has already registered for the course
        double creditsRegistered = 0;
        CourseRegisterDTO[] courseRegister = this.viewCoursesRegistered();
        boolean isCourseRegistered = false;
        for (CourseRegisterDTO course : courseRegister) {
            creditsRegistered += Double.parseDouble(course.credit_structure().split("-")[3]);
            if (course.course_code().equals(courseCode)) {
                isCourseRegistered = true;
                break;
            }
        }
        if (isCourseRegistered) {
            return "You have already registered for the course";
        }

        // check if the student has already passed the course
        GradeDTO[] grades = this.viewGrades();
        boolean isCoursePassed = false;
        for (GradeDTO grade : grades) {
            if (grade.course_code().equals(courseCode) && !grade.grade().equals("F")) {
                isCoursePassed = true;
                break;
            }
        }
        if (isCoursePassed) {
            return "You have already passed the course";
        }

        // total credits registered in previous 2 semesters
        double totalCredits;
        double creditsInPreviousSemester = 0;
        double creditsInPreviousPreviousSemester = 0;
        String previousSemester = this.getPreviousSemester(Session.getInstance().getCurrentSemester());
        String previousPreviousSemester = this.getPreviousSemester(previousSemester);
        for (GradeDTO grade : grades) {
            if (grade.semester().equals(previousSemester)) {
                String creditStructure = Course_catalog.getCreditStructure(grade.course_code());
                String[] creditParts = creditStructure.split("-");
                creditsInPreviousSemester += Double.parseDouble(creditParts[3]);
            }
            if (grade.semester().equals(previousPreviousSemester)) {
                String creditStructure = Course_catalog.getCreditStructure(grade.course_code());
                String[] creditParts = creditStructure.split("-");
                creditsInPreviousPreviousSemester += Double.parseDouble(creditParts[3]);
            }
        }
        // if one of them is zero, then the student is in the second semester and if both are zero, then the student is in the first semester
        totalCredits = creditsInPreviousSemester + creditsInPreviousPreviousSemester;
        double maxCreditsAllowed;
        if (creditsInPreviousSemester == 0 && creditsInPreviousPreviousSemester == 0) {
            maxCreditsAllowed = 24;
        } else if (creditsInPreviousSemester == 0 || creditsInPreviousPreviousSemester == 0) {
            maxCreditsAllowed = 1.25 * totalCredits;
        } else {
            maxCreditsAllowed = 1.25 * totalCredits/2;
        }

        // check if the allowed credits are exceeded by registering for the course
        double currentCourseCredit = Double.parseDouble(Course_catalog.getCreditStructure(courseCode).split("-")[3]);
        if (creditsRegistered + currentCourseCredit > maxCreditsAllowed) {
            OutputHandler.logError("You are not allowed to register for the course as it will exceed the allowed credits");
            OutputHandler.logError("Credits registered: " + creditsRegistered);
            OutputHandler.logError("Current course credit: " + currentCourseCredit);
            OutputHandler.logError("Max credits allowed: " + maxCreditsAllowed);
            return "You are not allowed to register for the course as it will exceed the allowed credits";
        }

        // check if the student has passed the prerequisites
        String[] prerequisites = Course_catalog.getCoursePrerequisites(courseCode);
        for (String prerequisite : prerequisites) {
            boolean isPrerequisitePassed = false;
            for (GradeDTO grade : grades) {
                if (grade.course_code().equals(prerequisite) && !grade.grade().equals("F")) {
                    isPrerequisitePassed = true;
                    break;
                }
            }
            if (!isPrerequisitePassed) {
                return "You have not passed the prerequisites for the course";
            }
        }

        // register for the course
        if (StudentDAL.registerForCourse(this.entryNumber, courseCode, Session.getInstance().getCurrentSemester())) {
            EventLogger.logEvent(this.userName, "enrollInCourse", java.time.LocalDateTime.now().toString(), this.sessionID);
            return "You have successfully enrolled in the course";
        }
        
        OutputHandler.logError("Not able to register for the course");
        return "Not able to register for the course";
    }


    private String getPreviousSemester(String currentSemester) {
        String[] semester = currentSemester.split(" ");
        int year = Integer.parseInt(semester[1]);
        String semesterType = semester[0];
        if (semesterType.equals("ODD")) {
            return "EVEN " + year;
        } else {
            return "ODD " + (year-1);
        }
    }

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
//        check session
        if (isNotAuthorized()) {
            return "You are not authorized to drop the course";
        }

//        check if the course is registered
        CourseRegisterDTO[] courseRegister = this.viewCoursesRegistered();
        boolean isCourseRegistered = false;
        for (CourseRegisterDTO course : courseRegister) {
            if (course.course_code().equals(courseCode)) {
                isCourseRegistered = true;
                break;
            }
        }
        if (!isCourseRegistered) {
            return "You have not registered for the course";
        }

//        drop the course
        if (StudentDAL.dropCourse(this.entryNumber, courseCode , Session.getInstance().getCurrentSemester())) {
            EventLogger.logEvent(this.userName, "dropCourse", java.time.LocalDateTime.now().toString(), this.sessionID);
            return "You have successfully dropped the course";
        }

        OutputHandler.logError("Not able to drop the course");
        return "Not able to drop the course";
    }

    //    view courses offered
    public Course_Offerings_DTO[] viewCoursesOffered() {
//        check session
        Course_Offerings_DTO[] courseOfferings;
        if (isNotAuthorized()) {
            courseOfferings = new Course_Offerings_DTO[1];
            courseOfferings[0] = new Course_Offerings_DTO("Error", "You are not authorized to view the courses offered", "Error", new String[]{"Error"}, "Error","Error");
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

    public double calculateGPA(GradeDTO[] grades) {
        return StudentDAL.getCGPA(this.entryNumber, grades);
    }


}
