package org.academic.User;

import java.util.ArrayList;

import org.academic.CourseType;
import org.academic.Events;
import org.academic.Authentication.Session;
import org.academic.Database.GradeDTO;
import org.academic.Database.UgCurriculumDTO;
import org.academic.Services.Course_Offerings;
import org.academic.Services.Course_catalog;
import org.academic.Services.CurriculumDAL;
import org.academic.Services.EventLogger;
import org.academic.Services.OfficeStaffDAL;
import org.academic.Services.StudentDAL;
import org.academic.cli.OutputHandler;

public class OfficeStaff implements User {
    /**
     *
     */
    private static final String regexCourseCode = "^[A-Z]{2}[0-9]{3}$";
    /**
     *
     */
    private static final String regexEntryNumber = "^[0-9]{4}[a-z]{3}[0-9]{4}$";
    private static final String regexBatch = "^[0-9]{4}$";
    private static final String regexName = "^[a-zA-Z ]+$";
    private static final String regexEmail = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";
    private static final String regexPassword = "^.{3,}$";

    private String userName;
    private String password;
    private String[] options = {"Manage Academic Events", "Register a new user", "Generate Transcript", "Add new Course to Course Catalog", "Run a Graduation Check", "Edit UG curriculum", "Edit Profile", "Logout"};
    
    private String StaffID;
    private String sessionID;
    
    public String getSessionID() {
        return sessionID;
    }
    
    // public String[] getOptions(String[] options) {
    //     return options;
    // }

    public OfficeStaff(String userName, String password, String StaffID) {
        this.userName = userName;
        this.password = password;
        this.StaffID = StaffID;
    }


    private boolean isNotAuthorized() {
        Session session = Session.getInstance();

        if (!session.isSessionActive() || !session.getUserName().equals(this.userName) || !session.getUserType().equals(UserType.OFFICESTAFF)) {
            return true;
        }
        this.sessionID = session.getSessionId();
        return false;
    }
    
    public String getStaffID() {
        return StaffID;
    }

    public void setStaffID(String staffID) {
        StaffID = staffID;
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
     * @return  the options available to the user
     */
    @Override
    public String[] getOptions() {
        return options;
    }

    /**
     * @return change password
     */
    @Override
    public String updateProfile(String password) {
        return null;
    }

    public Events getNextEvent(Events currentEvent) {
        // check if the user is authorized
        if (isNotAuthorized()) {
            return null;
        }

        // get the next event
        if (currentEvent == Events.SEMESTER_START) {
            return Events.COURSE_FLOAT_START;
        } else if (currentEvent == Events.COURSE_FLOAT_START) {
            return Events.COURSE_FLOAT_END;
        } else if (currentEvent == Events.COURSE_FLOAT_END) {
            return Events.COURSE_REGISTRATION_START;
        } else if (currentEvent == Events.COURSE_REGISTRATION_START) {
            return Events.COURSE_REGISTRATION_END;
        } else if (currentEvent == Events.COURSE_REGISTRATION_END) {
            return Events.GRADE_SUBMISSION_START;
        } else if (currentEvent == Events.GRADE_SUBMISSION_START) {
            return Events.GRADE_SUBMISSION_END;
        } else if (currentEvent == Events.GRADE_SUBMISSION_END) {
            return Events.SEMESTER_END;
        } else {
            return Events.SEMESTER_START;
        }
        
    }

    public String updateAcademicEvent(Events nextEvent) {
        // check if the user is authorized
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }


        // close the current event
        boolean isEventClosed = OfficeStaffDAL.closeCurrentEvent();
        // if (!isEventClosed) {
        //     return "Error in closing the current event";
        // }

        EventLogger.logEvent(this.userName, "Closed academic event " + nextEvent.toString(), java.time.LocalDateTime.now().toString(), this.sessionID);

        // semseter and start date of the next event
        String semester = Session.getInstance().getCurrentSemester();
        String startDate = java.time.LocalDate.now().toString();
        OutputHandler.print("startDate: " + startDate);
    

        // open the next event
        boolean isNextEventOpened = OfficeStaffDAL.openNextEvent(nextEvent.toString(), semester, startDate);
        if (!isNextEventOpened) {
            return "Error in opening the next event";
        }

        EventLogger.logEvent(this.userName, "Updated academic event to " + nextEvent.toString(), java.time.LocalDateTime.now().toString(), this.sessionID);

        return "Event updated successfully";

    }

    public void updateSemester(String currentSemester) {
        if (isNotAuthorized()) {
            return;
        }

        // close the current semester
        boolean isSemesterClosed = OfficeStaffDAL.closeCurrentSemester();
        if (!isSemesterClosed) {
            OutputHandler.logError("Error in closing the current semester" );
            OutputHandler.logError(Session.getInstance().getCurrentSemester());
            return;
        }
        
        EventLogger.logEvent(this.userName, "Closed semester " + currentSemester, java.time.LocalDateTime.now().toString(), this.sessionID);

        OfficeStaffDAL.updateSemester(currentSemester);

        EventLogger.logEvent(this.userName, "Updated semester to " + currentSemester, java.time.LocalDateTime.now().toString(), this.sessionID);
        
    }

    public String addStudent(String studentEntryNumber, String studentName, String studentEmail, String studentPassword, int studentBatch) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // check if the student already exists
        boolean isStudentExists = OfficeStaffDAL.isStudentExists(studentEntryNumber);
        if (isStudentExists) {
            return "Student already exists";
        }

        // add the student in auth table
        boolean isStudentAddedInAuth = OfficeStaffDAL.addNewUser(studentEntryNumber, studentPassword, UserType.STUDENT);
        if (!isStudentAddedInAuth) {
            return "Error in adding the student in auth table";
        }

        // add the student in student table
        boolean isStudentAdded = OfficeStaffDAL.addStudent(studentEntryNumber, studentName, studentEmail, studentBatch, studentEntryNumber);
        if (!isStudentAdded) {
            return "Error in adding the student";
        }

        EventLogger.logEvent(this.userName, "Added student " + studentEntryNumber, java.time.LocalDateTime.now().toString(), this.sessionID);
        
        return "Student added successfully";

    }

    public String addFaculty(String facultyUserName, String facultyName, String facultyEmail, String facultyPassword,
            String facultyDepartment) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // check if the faculty already exists
        boolean isFacultyExists = OfficeStaffDAL.isFacultyExists(facultyUserName);
        if (isFacultyExists) {
            return "Faculty already exists";
        }

        // add the faculty in auth table
        boolean isFacultyAddedInAuth = OfficeStaffDAL.addNewUser(facultyUserName, facultyPassword, UserType.FACULTY);
        if (!isFacultyAddedInAuth) {
            return "Error in adding the faculty in auth table";
        }

        // add the faculty in faculty table
        boolean isFacultyAdded = OfficeStaffDAL.addFaculty(facultyUserName, facultyName, facultyEmail, facultyDepartment);
        if (!isFacultyAdded) {
            return "Error in adding the faculty";
        }

        EventLogger.logEvent(this.userName, "Added faculty " + facultyUserName, java.time.LocalDateTime.now().toString(), this.sessionID);

        return "Faculty added successfully";
    }

    public String addStaff(String staffUserName, String staffName, String staffEmail, String staffPassword) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // check if the staff already exists
        boolean isStaffExists = OfficeStaffDAL.isStaffExists(staffUserName);
        if (isStaffExists) {
            return "Staff already exists";
        }

        // add the staff in auth table
        boolean isStaffAddedInAuth = OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICESTAFF);
        if (!isStaffAddedInAuth) {
            return "Error in adding the staff in auth table";
        }

        // add the staff in staff table
        boolean isStaffAdded = OfficeStaffDAL.addStaff(staffUserName, staffName, staffEmail);
        if (!isStaffAdded) {
            return "Error in adding the staff";
        }

        EventLogger.logEvent(this.userName, "Added staff " + staffUserName, java.time.LocalDateTime.now().toString(), this.sessionID);

        return "Staff added successfully";
    }

    public String addCourseInCatalog(String courseName, String courseCode, String courseCredits, String prerequisites) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // check if the course already exists
        boolean isCourseExists = Course_catalog.isCourseExists(courseCode);
        if (isCourseExists) {
            return "Course already exists";
        }

        // add the course in course catalog
        boolean isCourseAdded = Course_catalog.addCourse(courseName, courseCode, courseCredits, prerequisites);
        if (!isCourseAdded) {
            return "Error in adding the course in catalog";
        }

        EventLogger.logEvent(this.userName, "Added course " + courseCode, java.time.LocalDateTime.now().toString(), this.sessionID);

        return "Course added successfully";
    }

    public String generateTranscript(String batch) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // get the list of students entry numbers
        ArrayList<String> studentsEntryNumbers = StudentDAL.getStudentsEntryNumbers(batch);
        if (studentsEntryNumbers == null) {
            return "Error in getting the list of students";
        }

        // get student details and grades for each student
        for (String studentEntryNumber : studentsEntryNumbers) {
            // get student name
            String studentName = StudentDAL.getName(studentEntryNumber);
            if (studentName == null) {
                return "Error in getting the student name";
            }

            // get student grades
            GradeDTO[] studentGrades = Course_Offerings.view_all_grade(studentEntryNumber);
            if (studentGrades == null) {
                return "Error in getting the student grades";
            }

            // get credits of the courses taken by the student
            double totalCredits = 0;
            double[] allCourseCredits = new double[studentGrades.length];

            for (int i = 0; i < studentGrades.length; i++) {
                GradeDTO grade = studentGrades[i];
                String credits = Course_catalog.getCreditStructure(grade.course_code());

                if (credits == null) {
                    return "Error in getting the credits of the course";
                }

                // extract the credits from the string and push it to the array
                String[] creditParts = credits.split("-");
                OutputHandler.print(credits);
                double courseCredits = Double.parseDouble(creditParts[3]);
                allCourseCredits[i] = courseCredits;
                totalCredits += courseCredits;
            }

            // get the CGPA
            double cgpa = StudentDAL.getCGPA(studentEntryNumber,studentGrades);
            if (cgpa == -1) {
                return "Error in getting the CGPA";
            }

            // generate the transcript
            boolean isTranscriptGenerated = OutputHandler.generateTranscript(studentEntryNumber, studentName, batch, studentGrades, allCourseCredits, totalCredits, cgpa);
            if (!isTranscriptGenerated) {
                return "Error in generating the transcript";
            }

            EventLogger.logEvent(this.userName, "Generated transcript for " + studentEntryNumber, java.time.LocalDateTime.now().toString(), this.sessionID);
    }
        

        return "Transcript generated successfully";

    }

    private boolean isEntryNumberValid(String entryNumber) {
        // validate the student entry number using regex
        String regex = regexEntryNumber;
        return entryNumber.matches(regex);

    }

    public String checkGraduation(String studentEntryNumber) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // validate the student entry number using regex
        if (!this.isEntryNumberValid(studentEntryNumber)) {
            return "Invalid student entry number";
        }
        

        // get the student batch
        String studentBatch = StudentDAL.getBatch(studentEntryNumber);
        if (studentBatch == null) {
            return "Error in getting the student batch";
        }

        // get the student grades
        GradeDTO[] studentGrades = Course_Offerings.view_all_grade(studentEntryNumber);
        if (studentGrades == null) {
            return "Error in getting the student grades";
        }

        // get the list of courses in the curriculum
        UgCurriculumDTO[] coursesInUgCurriculum = CurriculumDAL.getCoursesInUGCurriculum(studentBatch);

        // check if the student has completed all the core courses
        boolean hasCompletedAllCoreCourses = true;
        for (UgCurriculumDTO course : coursesInUgCurriculum) {
            if (course.type().equals("Core") && !hasCompletedCourse(course.courseCode(), studentGrades)) {
                hasCompletedAllCoreCourses = false;
                break;
            }
        }

        // check if the student has completed the required number of credits
        double totalCredits = getTotalCredits(studentGrades);
        double minCreditsRequired = Double.parseDouble(coursesInUgCurriculum[0].minCredits());
        boolean hasCompletedMinCredits = totalCredits >= minCreditsRequired;

         // check if the student has completed the required number of electives
        // int numElectives = getNumElectives(coursesInUgCurriculum);
        int minElectivesRequired = Integer.parseInt(coursesInUgCurriculum[0].minElectives());
        int numCompletedElectives = getNumCompletedElectives(studentGrades,coursesInUgCurriculum);
        boolean hasCompletedMinElectives = numCompletedElectives >= minElectivesRequired;

        // check if the student is eligible for graduation
        boolean isGraduationEligible = hasCompletedAllCoreCourses && hasCompletedMinCredits && hasCompletedMinElectives;

        // print grades and ug curriculum
        OutputHandler.print("Student grades");
        for (GradeDTO grade : studentGrades) {
            OutputHandler.print(grade.course_code() + " " + grade.grade());
        }

        OutputHandler.print("UG Curriculum");
        for (UgCurriculumDTO course : coursesInUgCurriculum) {
            OutputHandler.print(course.courseCode() + " " + course.type());
        }

        

        if (isGraduationEligible) {
            EventLogger.logEvent(this.userName, studentEntryNumber + " is eligible for graduation", java.time.LocalDateTime.now().toString(), this.sessionID);
            return "Graduation eligible";
        } else {
            String reason = "";
            if (!hasCompletedAllCoreCourses) {
                reason += "Student has not completed all the core courses in the curriculum";
            }
            if (!hasCompletedMinCredits) {
                reason += " Student has not completed the minimum number of credits";
            }
            if (!hasCompletedMinElectives) {
                reason += " Student has not completed the minimum number of electives";
            }
            EventLogger.logEvent(this.userName, studentEntryNumber+ " : " +reason, java.time.LocalDateTime.now().toString(), this.sessionID);
            return reason;
        }
    }

    private int getNumCompletedElectives(GradeDTO[] studentGrades, UgCurriculumDTO[] coursesInUgCurriculum) {
        int numCompletedElectives = 0;
        for (GradeDTO grade : studentGrades) {
            for (UgCurriculumDTO course : coursesInUgCurriculum) {
                if (course.courseCode().equals(grade.course_code()) && course.type().equals("Elective")) {
                    numCompletedElectives++;
                    break;
                }
            }
        }
        return numCompletedElectives;
    }

    private boolean hasCompletedCourse(String courseCode, GradeDTO[] grades) {
        for (GradeDTO grade : grades) {
            if (grade.course_code().equals(courseCode)) {
                return true;
            }
        }
        return false;
    }

    private double getTotalCredits(GradeDTO[] grades) {
        double totalCredits = 0;
        for (GradeDTO grade : grades) {
            // get credit
            String credit = Course_catalog.getCreditStructure(grade.course_code());
            if (credit == null) {
                return -1;
            }

            // extract the credits from the string and push it to the array
            String[] creditParts = credit.split("-");
            double courseCredits = Double.parseDouble(creditParts[3]);
            totalCredits += courseCredits;


        }
        return totalCredits;
    }

    public String addCourseUGCurriculum(String courseCode, String batch, CourseType courseTypeStr) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // validate the course code using regex
        if (!this.isCourseCodeValid(courseCode)) {
            return "Invalid course code";
        }

        // validate the batch using regex
        if (!this.isBatchValid(batch)) {
            return "Invalid batch";
        }

        // check if the course is already in the curriculum
        UgCurriculumDTO[] coursesInUgCurriculum = CurriculumDAL.getCoursesInUGCurriculum(batch);
        for (UgCurriculumDTO course : coursesInUgCurriculum) {
            if (course.courseCode().equals(courseCode)) {
                return "Course already in the curriculum";
            }
        }

        // add the course to the curriculum
        boolean isCourseAdded = CurriculumDAL.addCourseToUGCurriculum(courseCode, batch, courseTypeStr);
        if (!isCourseAdded) {
            return "Error in adding the course to the curriculum";
        }

        EventLogger.logEvent(this.userName, "Added " + courseCode + " to the curriculum", java.time.LocalDateTime.now().toString(), this.sessionID);
        return "Course added to the curriculum successfully";
    }

    private boolean isBatchValid(String batch) {
        String regex = regexBatch;
        return batch.matches(regex);
    }

    private boolean isCourseCodeValid(String courseCode) {
        String regex = regexCourseCode;
        return courseCode.matches(regex);
    }

    public String addBatch(String batch, int minCredits, int minElectiveCourses) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        // validate the batch using regex
        if (!this.isBatchValid(batch)) {
            return "Invalid batch";
        }

        // check if the batch is already in the curriculum
        UgCurriculumDTO[] coursesInUgCurriculum = CurriculumDAL.getCoursesInUGCurriculum(batch);
        if (coursesInUgCurriculum.length > 0) {
            return "Batch already in the curriculum";
        }

        // add the batch to the curriculum
        boolean isBatchAdded = CurriculumDAL.addBatch(batch, minCredits, minElectiveCourses);
        if (!isBatchAdded) {
            return "Error in adding the batch to the curriculum";
        }

        EventLogger.logEvent(this.userName, "Added " + batch + " to the curriculum", java.time.LocalDateTime.now().toString(), this.sessionID);
        return "Batch added to the curriculum successfully";
    }

    public String editProfile(String string, String name) {
        if (isNotAuthorized()) {
            return "You are not authorized to perform this action";
        }

        switch (string) {
            case "name":
                // validate the name using regex
                if (!this.isNameValid(name)) {
                    return "Invalid name";
                }

                // update the name
                boolean isNameUpdated = OfficeStaffDAL.updateName(this.userName, name);
                if (!isNameUpdated) {
                    return "Error in updating the name";
                }

                break;
            case "email":
                // validate the email using regex
                if (!this.isEmailValid(name)) {
                    return "Invalid email";
                }

                // update the email
                boolean isEmailUpdated = OfficeStaffDAL.updateEmail(this.userName, name);
                if (!isEmailUpdated) {
                    return "Error in updating the email";
                }

                break;
            case "password":
                // validate the password using regex
                if (!this.isPasswordValid(name)) {
                    return "Password must contain at least 3 characters";
                }

                // update the password
                boolean isPasswordUpdated = OfficeStaffDAL.updatePassword(this.userName, name);
                if (!isPasswordUpdated) {
                    return "Error in updating the password";
                }
                break;
            default:
                return "Invalid option";
        }
        return "Profile updated successfully";
    }

    private boolean isPasswordValid(String name) {
        String regex = regexPassword;
        return name.matches(regex);
    }

    private boolean isEmailValid(String name) {
        String regex = regexEmail;
        return name.matches(regex);
    }

    private boolean isNameValid(String name) {
        String regex = regexName;
        return name.matches(regex);
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
