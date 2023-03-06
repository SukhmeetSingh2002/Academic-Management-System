package org.academic.Authentication;

import org.academic.Services.Course_Offerings;
import org.academic.Services.InstructorDAL;
import org.academic.Services.OfficeStaffDAL;
import org.academic.Services.StudentDAL;
import org.academic.User.UserType;

import java.util.Date;

public class Session {
    private static Session session = null;
    private String firstName;
    public String getFirstName() {
        return firstName;
    }
    
    private String email;

    public String getEmail() {
        return email;
    }

    private String userName;
    private String password;
    private UserType userType;
    private Date loginTime;
    private Date logoutTime;
    private String sessionId;
    private String FacultyID;
    private String StudentEntryNumber;
    private String currentSemester;
    private String StaffID;
    private String[] currentAcademicEvent;

    public String[] getCurrentAcademicEvent() {
        this.currentAcademicEvent = OfficeStaffDAL.getCurrentEvent();
        return currentAcademicEvent;
    }

    public String getStaffID() {
        return StaffID;
    }

    public String getCurrentSemester() {
        this.currentSemester = Course_Offerings.get_current_semester();
        return currentSemester;
    }

    public String getStudentEntryNumber() {
        return StudentEntryNumber;
    }

    public String getFacultyID() {
        return this.FacultyID;
    }

    public void setFacultyID(String facultyID) {
        this.FacultyID = facultyID;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

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

    public void setSession(String userName, String password, UserType userType, String sessionId) {
        this.userName = userName;
        this.password = password;
        this.userType = userType;
        this.loginTime = new Date();
        this.sessionId = sessionId;
        this.currentSemester = Course_Offerings.get_current_semester();

        if (userType == UserType.FACULTY) {
            this.FacultyID = InstructorDAL.getFacultyId(userName).toUpperCase();
            this.firstName = InstructorDAL.getName(FacultyID);
            this.email = InstructorDAL.getEmail(FacultyID);
        }
        if (userType == UserType.STUDENT) {
            // to check case
            this.StudentEntryNumber = StudentDAL.getStudentEntryNumber(userName);
            this.firstName = StudentDAL.getName(StudentEntryNumber);
            this.email = StudentDAL.getEmail(StudentEntryNumber);
        }
        if (userType == UserType.OFFICE_STAFF) {
            this.StaffID = OfficeStaffDAL.getStaffID(userName);
            this.firstName = OfficeStaffDAL.getFirstName(userName);
            this.email = OfficeStaffDAL.getEmail(userName);
        }
        // TODO: set time in database
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
