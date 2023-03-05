package org.academic.User;

import org.academic.Database.Course_CatalogDTO;
import org.academic.Authentication.Session;
import org.academic.Database.Course_Offerings_DTO;
import org.academic.Database.GiveGradeDTO;
import org.academic.Services.Course_Offerings;
import org.academic.Services.Course_catalog;
import org.academic.Services.EventLogger;

public class Faculty implements User {
    private String userName;
    private String password;
    private String[] options = {"Add a course offering", "Give grades to students", "View course offering" , "Edit Profile", "Logout"};

    private String FacultyID;
    private String sessionID;

    public Faculty(String userName, String password, String FacultyID) {
        this.userName = userName;
        this.password = password;
        this.FacultyID = FacultyID;
    }

    private boolean isNotAuthorized() {
        Session session = Session.getInstance();
        // OutputHandler.print("Session is active: " + session.isSessionActive());
        // OutputHandler.print("Session user name: " + session.getUserName());
        // OutputHandler.print("Session user type: " + session.getUserType());
        // OutputHandler.print("User name: " + this.userName);
        // OutputHandler.print("User type: " + UserType.FACULTY);
        // OutputHandler.print("User id: " + this.FacultyID);
        // OutputHandler.print("Session id: " + session.getSessionId());
        // OutputHandler.print("Session user id: " + session.getFacultyID());
        // OutputHandler.print("Session user id: " + session.getFirstName());

        if (!session.isSessionActive() || !session.getUserName().equals(this.userName) || !session.getUserType().equals(UserType.FACULTY)) {
            return true;
        }
        this.sessionID = session.getSessionId();
        return false;
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

    /**
     * @return the options available to the user
     */
    @Override
    public String[] getOptions() {
        return options;
    }

    /**
     * @param password new password
     * @return the status of the operation
     */
    @Override
    public String updateProfile(String password) {
        return null;
    }

    public String addCourse(String courseCode, Float creditLimit) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this operation";
        }
        String currentSemester = Course_Offerings.get_current_semester();
        String res = Course_Offerings.offer_course(courseCode,this.FacultyID,currentSemester,creditLimit);

        // make event log entry
        EventLogger.logEvent(this.userName, "Add course", java.time.LocalDateTime.now().toString(),this.sessionID);

        return res;
    }

    public Course_Offerings_DTO[] getCourses() {
        Course_Offerings_DTO[] courses ;
        if (isNotAuthorized()) {
            courses = new Course_Offerings_DTO[1];
            courses[0] =  new Course_Offerings_DTO("Error", "You are not authorized to view the courses offered", "Error", new String[]{"Error"}, "Error","Error");
            return courses;
        }
        // get the courses offered by the faculty in the current semester
        String currentSemester = Course_Offerings.get_current_semester();
        courses = Course_Offerings.get_courses_offered_by_instructor(this.FacultyID, currentSemester);

        // make event log entry
        EventLogger.logEvent(this.userName, "View courses", java.time.LocalDateTime.now().toString(),this.sessionID);

        return courses;
    }

    public GiveGradeDTO[] downloadData(String courseCode) {
        GiveGradeDTO[] studentsData;
        if (isNotAuthorized()) {
            studentsData = new GiveGradeDTO[1];
            studentsData[0] = new GiveGradeDTO("Error", "You are not authorized to download the data", "Error", "Error", "Error");
            return studentsData;
        }
        // get the students data for the course
        String currentSemester = Course_Offerings.get_current_semester();
        studentsData = Course_Offerings.get_students_data(courseCode, currentSemester);

        // make event log entry
        EventLogger.logEvent(this.userName, "Download data", java.time.LocalDateTime.now().toString(),this.sessionID);

        return studentsData;
    }

    public String editAndUploadData(String[] data) {
        if (isNotAuthorized()) {
            return "You are not authorized to upload the data";
        }
        // upload the data to the database use for loop to iterate over the data 
        // and call the function to upload the data
        for (int i = 1; i < data.length; i++) {
            String[] studentData = data[i].split(",");
            String studentID = studentData[0];
            String courseCode = studentData[2];
            String semester = studentData[3];
            String grade = studentData[4];

            String res = Course_Offerings.give_grade(studentID, courseCode, semester, grade);
            if (res.startsWith("Error")) {
                return "Error in uploading the data for student with ID: " + studentID + " for course: " + courseCode + " in semester: " + semester + " with grade: " + grade + ". Please try again\n" + res;
            }
        }

        // make event log entry
        EventLogger.logEvent(this.userName, "Upload data", java.time.LocalDateTime.now().toString(),this.sessionID);

        return "Data uploaded successfully";
    }

    public Course_CatalogDTO[] viewCoursesCatalog() {
        Course_CatalogDTO[] courses;
        if (isNotAuthorized()) {
            courses = new Course_CatalogDTO[1];
            courses[0] = new Course_CatalogDTO("Error", "You are not authorized to view the course catalog", new String[]{"Error"}, "Error");
            return courses;
        }

        // get the course catalog
        courses = Course_catalog.get_courses();

        // make event log entry
        EventLogger.logEvent(this.userName, "View course catalog", java.time.LocalDateTime.now().toString(),this.sessionID);

        return courses;
        
    }


//    options available to faculty are
//      1. Add a course offering
//          i. Add some CGPA constraints
//      2. Give grades to students
//          i. First download the all students details
//          ii. Then upload the grades for each student in the course offering in a csv file
//      3. View course offering
}
