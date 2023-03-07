package org.academic.User;

import org.academic.Authentication.Authenticator;
import org.academic.CourseType;
import org.academic.Database.GradeDTO;
import org.academic.Database.UgCurriculumDTO;
import org.academic.Events;
import org.academic.Services.*;
import org.academic.cli.OutputHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.internal.verification.VerificationModeFactory.times;

class OfficeStaffTest {

    private OfficeStaff officeStaff;

    @BeforeEach
    void setUp() throws SQLException {
        Authenticator.authenticate("staff", "123s");
        officeStaff = new OfficeStaff("staff", "123s", "XYZ");
    }

    @AfterEach
    void tearDown() throws SQLException {
        Authenticator.logout();
    }

    @Test
    void getUserName() {
        assertEquals("staff", officeStaff.getUserName());
    }

    @Test
    void getPassword() {
        assertEquals("123s", officeStaff.getPassword());
    }

    @Test
    void getOptions() {
        String[] expectedOptions = {"Manage Academic Events", "Register a new user", "Generate Transcript", "Add new Course to Course Catalog", "Run a Graduation Check", "Edit UG curriculum", "Edit Profile", "Logout"};
        assertArrayEquals(expectedOptions, officeStaff.getOptions());
    }

    @Test
    void updateProfile() {
//        TODO: Implement this
    }

    @Test
    void getNextEventSuccess() {
        assertAll(
                () -> assertEquals(Events.COURSE_FLOAT_START,officeStaff.getNextEvent(Events.SEMESTER_START)),
                () -> assertEquals(Events.COURSE_FLOAT_END,officeStaff.getNextEvent(Events.COURSE_FLOAT_START)),
                () -> assertEquals(Events.COURSE_REGISTRATION_START,officeStaff.getNextEvent(Events.COURSE_FLOAT_END)),
                () -> assertEquals(Events.COURSE_REGISTRATION_END,officeStaff.getNextEvent(Events.COURSE_REGISTRATION_START)),
                () -> assertEquals(Events.GRADE_SUBMISSION_START,officeStaff.getNextEvent(Events.COURSE_REGISTRATION_END)),
                () -> assertEquals(Events.GRADE_SUBMISSION_END,officeStaff.getNextEvent(Events.GRADE_SUBMISSION_START)),
                () -> assertEquals(Events.SEMESTER_END,officeStaff.getNextEvent(Events.GRADE_SUBMISSION_END)),
                () -> assertEquals(Events.SEMESTER_START,officeStaff.getNextEvent(Events.SEMESTER_END))

        );
    }

//    not authorized to access this event
    @Test
    void getNextEventUnauthorized() throws SQLException {
        Authenticator.logout();
        assertNull(officeStaff.getNextEvent(Events.SEMESTER_START));
    }

    @Test
    void updateAcademicEventSuccess() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic(Course_Offerings.class);
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);

        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 202Y1");
        officeStaffDALMockedStatic.when(OfficeStaffDAL::closeCurrentEvent).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.openNextEvent(any(),any(), any())).thenReturn(true);

        String expectedMessage = "Event updated successfully";
        String actualMessage = officeStaff.updateAcademicEvent(Events.COURSE_REGISTRATION_START);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.openNextEvent(any(),any(),any()));
        officeStaffDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester);
        courseOfferingsMockedStatic.close();

    }

//    error opening
    @Test
    void updateAcademicEventFailure()  {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic(Course_Offerings.class);
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);

        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        officeStaffDALMockedStatic.when(OfficeStaffDAL::closeCurrentEvent).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.openNextEvent(any(),any(), any())).thenReturn(false);

        String expectedMessage = "Error in opening the next event";
        String actualMessage = officeStaff.updateAcademicEvent(Events.COURSE_REGISTRATION_START);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.openNextEvent(any(),any(),any()));
        officeStaffDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester);
        courseOfferingsMockedStatic.close();

    }

//    not autenticated
    @Test
    void updateAcademicEventUnauthenticated() throws SQLException {
        Authenticator.logout();
        assertEquals("You are not authorized to perform this action",officeStaff.updateAcademicEvent(Events.COURSE_REGISTRATION_START));
    }


    @Test
    void updateSemesterWithCorrectSemester() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String currentSemester = "ODD 2020";
        String notCurrentSemester = "EVEN 2021";

        officeStaffDALMockedStatic.when(OfficeStaffDAL::closeCurrentSemester).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.updateSemester(currentSemester)).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.updateSemester(not(eq(currentSemester)))).thenReturn(false);

        boolean expected = true;
        boolean expected2 = false;
        boolean actual = officeStaff.updateSemester(currentSemester);
        boolean actual2 = officeStaff.updateSemester(notCurrentSemester);

        assertAll(
                () -> assertEquals(expected, actual),
                () -> assertEquals(expected2, actual2)
        );


        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.updateSemester(currentSemester));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.updateSemester(notCurrentSemester));
        officeStaffDALMockedStatic.close();


    }

//    not authenticated
    @Test
    void updateSemesterUnauthenticated() throws SQLException {
        Authenticator.logout();
        assertFalse(officeStaff.updateSemester("ODD 2020"));
    }

//    close current semester fails
    @Test
    void updateSemesterCloseCurrentSemesterFails() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String currentSemester = "ODD 2020";

        officeStaffDALMockedStatic.when(OfficeStaffDAL::closeCurrentSemester).thenReturn(false);

        assertFalse(officeStaff.updateSemester(currentSemester));

        officeStaffDALMockedStatic.verify(OfficeStaffDAL::closeCurrentSemester);
        officeStaffDALMockedStatic.close();


    }

//    Student already exists
    @Test
    void addStudentAlreadyExists() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String studentEntryNumber = "2020csb1129";
        String studentName = "John Doe";
        String studentEmail = "test@iitrpr.ac.in";
        String studentPassword = "password";
        String studentDepartment = "CSE";
        int studentBatch = 2020;

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber)).thenReturn(true);

        String expectedMessage = "Student already exists";
        String actualMessage = officeStaff.addStudent(studentEntryNumber, studentName, studentEmail, studentPassword, studentBatch);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber));
        officeStaffDALMockedStatic.close();

    }

//    Error in adding the student in auth table
    @Test
    void addStudentErrorInAuthTable() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String studentEntryNumber = "2020csb1129";
        String studentName = "John Doe";
        String studentEmail = "test@iitrpr.ac.in";
        String studentPassword = "password";
        String studentDepartment = "CSE";
        int studentBatch = 2020;

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(studentEntryNumber, studentPassword, UserType.STUDENT)).thenReturn(false);

        String expectedMessage = "Error in adding the student in auth table";
        String actualMessage = officeStaff.addStudent(studentEntryNumber, studentName, studentEmail, studentPassword, studentBatch);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(studentEntryNumber, studentPassword, UserType.STUDENT));
        officeStaffDALMockedStatic.close();
    }

//    "Error in adding the student";
    @Test
    void addStudentErrorInStudentTable() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String studentEntryNumber = "2020csb1129";
        String studentName = "John Doe";
        String studentEmail = "test@iitrpr.ac.in";
        String studentPassword = "password";
        String studentDepartment = "CSE";
        int studentBatch = 2020;

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(studentEntryNumber, studentPassword, UserType.STUDENT)).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addStudent(studentEntryNumber, studentName, studentEmail, studentBatch, studentEntryNumber)).thenReturn(false);

        String expectedMessage = "Error in adding the student";
        String actualMessage = officeStaff.addStudent(studentEntryNumber, studentName, studentEmail, studentPassword, studentBatch);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(studentEntryNumber, studentPassword, UserType.STUDENT));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addStudent(studentEntryNumber, studentName, studentEmail, studentBatch, studentEntryNumber));
        officeStaffDALMockedStatic.close();
    }

//    "Student added successfully";
    @Test
    void addStudentSuccess() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String studentEntryNumber = "2020csb1129";
        String studentName = "John Doe";
        String studentEmail = "test@iitrpr.ac.in";
        String studentPassword = "password";
        String studentDepartment = "CSE";
        int studentBatch = 2020;

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(studentEntryNumber, studentPassword, UserType.STUDENT)).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addStudent(studentEntryNumber, studentName, studentEmail, studentBatch, studentEntryNumber)).thenReturn(true);

        String expectedMessage = "Student added successfully";
        String actualMessage = officeStaff.addStudent(studentEntryNumber, studentName, studentEmail, studentPassword, studentBatch);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStudentExists(studentEntryNumber));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(studentEntryNumber, studentPassword, UserType.STUDENT));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addStudent(studentEntryNumber, studentName, studentEmail, studentBatch, studentEntryNumber));
        officeStaffDALMockedStatic.close();
    }

//    not authorized student
    @Test
    void addStudentNotAuthorized() throws SQLException {
        Authenticator.logout();
        String studentEntryNumber = "2020csb1129";
        String studentName = "John Doe";
        String studentEmail = "test@iitrpr.ac.in";
        String studentPassword = "password";
        String studentDepartment = "CSE";
        int studentBatch = 2020;

        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.addStudent(studentEntryNumber, studentName, studentEmail, studentPassword, studentBatch);

        assertEquals(expectedMessage,actualMessage);

    }


//    You are not authorized to perform this action";, Parameters :public String addFaculty(String facultyUserName, String facultyName, String facultyEmail, String facultyPassword,String facultyDepartment)
    @Test
    void addFacultyNotAuthorized() throws SQLException {
        Authenticator.logout();
        String facultyUserName = "Instrucor1";
        String facultyName = "John Doe";
        String facultyEmail = "test@iitrpr.ac.in";
        String facultyPassword = "password";
        String facultyDepartment = "CSE";

        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.addFaculty(facultyUserName, facultyName, facultyEmail, facultyPassword, facultyDepartment);

        assertEquals(expectedMessage,actualMessage);
    }

//   "Faculty already exists";
    @Test
    void addFacultyAlreadyExists() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String facultyUserName = "Instrucor1";
        String facultyName = "John Doe";
        String facultyEmail = "test@iitrpr.ac.in";
        String facultyPassword = "password";
        String facultyDepartment = "CSE";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isFacultyExists(facultyUserName)).thenReturn(true);

        String expectedMessage = "Faculty already exists";
        String actualMessage = officeStaff.addFaculty(facultyUserName, facultyName, facultyEmail, facultyPassword, facultyDepartment);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isFacultyExists(facultyUserName));
        officeStaffDALMockedStatic.close();
    }

//    "Error in adding the faculty in auth table";
    @Test
    void addFacultyErrorInAuthTable() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String facultyUserName = "Instrucor1";
        String facultyName = "John Doe";
        String facultyEmail = "test@iitrpr.ac.in";
        String facultyPassword = "password";
        String facultyDepartment = "CSE";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isFacultyExists(facultyUserName)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(facultyUserName, facultyPassword, UserType.FACULTY)).thenReturn(false);

        String expectedMessage = "Error in adding the faculty in auth table";
        String actualMessage = officeStaff.addFaculty(facultyUserName, facultyName, facultyEmail, facultyPassword, facultyDepartment);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isFacultyExists(facultyUserName));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(facultyUserName, facultyPassword, UserType.FACULTY));
        officeStaffDALMockedStatic.close();
    }

//    "Error in adding the faculty";
    @Test
    void addFacultyErrorInFacultyTable() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String facultyUserName = "Instrucor1";
        String facultyName = "John Doe";
        String facultyEmail = "test@iitrpr.ac.in";
        String facultyPassword = "password";
        String facultyDepartment = "CSE";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isFacultyExists(facultyUserName)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(facultyUserName, facultyPassword, UserType.FACULTY)).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addFaculty(facultyUserName, facultyName, facultyEmail, facultyDepartment)).thenReturn(false);

        String expectedMessage = "Error in adding the faculty";
        String actualMessage = officeStaff.addFaculty(facultyUserName, facultyName, facultyEmail, facultyPassword, facultyDepartment);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isFacultyExists(facultyUserName));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(facultyUserName, facultyPassword, UserType.FACULTY));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addFaculty(facultyUserName, facultyName, facultyEmail, facultyDepartment));
        officeStaffDALMockedStatic.close();
    }

//    "Faculty added successfully";
    @Test
    void addFacultySuccess() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String facultyUserName = "Instrucor1";
        String facultyName = "John Doe";
        String facultyEmail = "test@iitrpr.ac.in";
        String facultyPassword = "password";
        String facultyDepartment = "CSE";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isFacultyExists(facultyUserName)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(facultyUserName, facultyPassword, UserType.FACULTY)).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addFaculty(facultyUserName, facultyName, facultyEmail, facultyDepartment)).thenReturn(true);

        String expectedMessage = "Faculty added successfully";
        String actualMessage = officeStaff.addFaculty(facultyUserName, facultyName, facultyEmail, facultyPassword, facultyDepartment);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isFacultyExists(facultyUserName));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(facultyUserName, facultyPassword, UserType.FACULTY));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addFaculty(facultyUserName, facultyName, facultyEmail, facultyDepartment));
        officeStaffDALMockedStatic.close();
    }



//    add staff not authorized. Parameters : String addStaff(String staffUserName, String staffName, String staffEmail, String staffPassword)
    @Test
    void addStaffNotAuthorized() throws SQLException {
        Authenticator.logout();
        String staffUserName = "Staff1";
        String staffName = "John Doe";
        String staffEmail = "test@iitrpr.ac.in";
        String staffPassword = "password";

        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.addStaff(staffUserName, staffName, staffEmail, staffPassword);

        assertEquals(expectedMessage,actualMessage);
    }

//    add staff already exists. Parameters : String addStaff(String staffUserName, String staffName, String staffEmail, String staffPassword)
    @Test
    void addStaffAlreadyExists() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String staffUserName = "Staff1";
        String staffName = "John Doe";
        String staffEmail = "test@iitrpr.ac.in";
        String staffPassword = "password";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStaffExists(staffUserName)).thenReturn(true);

        String expectedMessage = "Staff already exists";
        String actualMessage = officeStaff.addStaff(staffUserName, staffName, staffEmail, staffPassword);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStaffExists(staffUserName));
        officeStaffDALMockedStatic.close();
    }

//    "Error in adding the staff in auth table";
    @Test
    void addStaffErrorInAuthTable() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String staffUserName = "Staff1";
        String staffName = "John Doe";
        String staffEmail = "test@iitrpr.ac.in";
        String staffPassword = "password";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStaffExists(staffUserName)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICE_STAFF)).thenReturn(false);

        String expectedMessage = "Error in adding the staff in auth table";
        String actualMessage = officeStaff.addStaff(staffUserName, staffName, staffEmail, staffPassword);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStaffExists(staffUserName));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICE_STAFF));
        officeStaffDALMockedStatic.close();
    }

//    "Error in adding the staff";
    @Test
    void addStaffErrorInStaffTable() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String staffUserName = "Staff1";
        String staffName = "John Doe";
        String staffEmail = "test@iitrpr.ac.in";
        String staffPassword = "password";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStaffExists(staffUserName)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICE_STAFF)).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addStaff(staffUserName, staffName, staffEmail)).thenReturn(false);

        String expectedMessage = "Error in adding the staff";
        String actualMessage = officeStaff.addStaff(staffUserName, staffName, staffEmail, staffPassword);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStaffExists(staffUserName));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICE_STAFF));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addStaff(staffUserName, staffName, staffEmail));
        officeStaffDALMockedStatic.close();
    }

//    "Staff added successfully";
    @Test
    void addStaffSuccess() {
        MockedStatic<OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic(OfficeStaffDAL.class);
        String staffUserName = "Staff1";
        String staffName = "John Doe";
        String staffEmail = "test@iitrpr.ac.in";
        String staffPassword = "password";

        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.isStaffExists(staffUserName)).thenReturn(false);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICE_STAFF)).thenReturn(true);
        officeStaffDALMockedStatic.when(() -> OfficeStaffDAL.addStaff(staffUserName, staffName, staffEmail)).thenReturn(true);

        String expectedMessage = "Staff added successfully";
        String actualMessage = officeStaff.addStaff(staffUserName, staffName, staffEmail, staffPassword);

        assertEquals(expectedMessage,actualMessage);

        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.isStaffExists(staffUserName));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICE_STAFF));
        officeStaffDALMockedStatic.verify(() -> OfficeStaffDAL.addStaff(staffUserName, staffName, staffEmail));
        officeStaffDALMockedStatic.close();
    }

//    add course in catalog not authorized. Parameters : String addCourseInCatalog(String courseName, String courseCode, String courseCredits, String prerequisites)

    @Test
    void addCourseInCatalogNotAuthorized() throws SQLException {
        Authenticator.logout();
        String courseName = "Course1";
        String courseCode = "CSE101";
        String courseCredits = "4";
        String prerequisites = "CSE100";

        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.addCourseInCatalog(courseName, courseCode, courseCredits, prerequisites);

        assertEquals(expectedMessage,actualMessage);
    }

//    add course in catalog already exists. Parameters : String addCourseInCatalog(String courseName, String courseCode, String courseCredits, String prerequisites)
    @Test
    void addCourseInCatalogAlreadyExists() {
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);
        String courseName = "Course1";
        String courseCode = "CSE101";
        String courseCredits = "4";
        String prerequisites = "CSE100";

        courseCatalogMockedStatic.when(() ->  Course_catalog.isCourseExists(courseCode)).thenReturn(true);

        String expectedMessage = "Course already exists";
        String actualMessage = officeStaff.addCourseInCatalog(courseName, courseCode, courseCredits, prerequisites);

        assertEquals(expectedMessage,actualMessage);

        courseCatalogMockedStatic.verify(() ->  Course_catalog.isCourseExists(courseCode));
        courseCatalogMockedStatic.close();

    }

//    add course in catalog error in adding course. Parameters : String addCourseInCatalog(String courseName, String courseCode, String courseCredits, String prerequisites)
    @Test
    void addCourseInCatalogErrorInAddingCourse() {
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);
        String courseName = "Course1";
        String courseCode = "CSE101";
        String courseCredits = "4";
        String prerequisites = "CSE100";

        courseCatalogMockedStatic.when(() ->  Course_catalog.isCourseExists(courseCode)).thenReturn(false);
        courseCatalogMockedStatic.when(() ->  Course_catalog.addCourse(courseName, courseCode, courseCredits, prerequisites)).thenReturn(false);

        String expectedMessage = "Error in adding the course in catalog";
        String actualMessage = officeStaff.addCourseInCatalog(courseName, courseCode, courseCredits, prerequisites);

        assertEquals(expectedMessage,actualMessage);

        courseCatalogMockedStatic.verify(() ->  Course_catalog.isCourseExists(courseCode));
        courseCatalogMockedStatic.verify(() ->  Course_catalog.addCourse(courseName, courseCode, courseCredits, prerequisites));
        courseCatalogMockedStatic.close();

    }

//    add course in catalog success. Parameters : String addCourseInCatalog(String courseName, String courseCode, String courseCredits, String prerequisites)
    @Test
    void addCourseInCatalogSuccess() {
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);
        String courseName = "Course1";
        String courseCode = "CSE101";
        String courseCredits = "4";
        String prerequisites = "CSE100";

        courseCatalogMockedStatic.when(() ->  Course_catalog.isCourseExists(courseCode)).thenReturn(false);
        courseCatalogMockedStatic.when(() ->  Course_catalog.addCourse(courseName, courseCode, courseCredits, prerequisites)).thenReturn(true);

        String expectedMessage = "Course added successfully";
        String actualMessage = officeStaff.addCourseInCatalog(courseName, courseCode, courseCredits, prerequisites);

        assertEquals(expectedMessage,actualMessage);

        courseCatalogMockedStatic.verify(() ->  Course_catalog.isCourseExists(courseCode));
        courseCatalogMockedStatic.verify(() ->  Course_catalog.addCourse(courseName, courseCode, courseCredits, prerequisites));
        courseCatalogMockedStatic.close();

    }

//    generate transcript not authorized. Parameters : String generateTranscript(String batch)
    @Test
    void generateTranscriptUnauthorized() throws SQLException {
        Authenticator.logout();
        String batch = "2019";

        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.generateTranscript(batch);

        assertEquals(expectedMessage,actualMessage);
    }

//    Error in getting the students entry numbers
    @Test
    void generateTranscriptErrorInGettingStudentsEntryNumbers() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        String batch = "2019";
        ArrayList<String> studentsEntryNumbers = new ArrayList<>();


        studentDALMockedStatic.when(() ->  StudentDAL.getStudentsEntryNumbers(batch)).thenReturn(studentsEntryNumbers);

        String expectedMessage = "Error in getting the students entry numbers";
        String actualMessage = officeStaff.generateTranscript(batch);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getStudentsEntryNumbers(batch));
        studentDALMockedStatic.close();
    }

//    Error in getting the student name
    @Test
    void generateTranscriptErrorInGettingStudentsName() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        String batch = "2019";
        ArrayList<String> studentsEntryNumbers = new ArrayList<>();
        studentsEntryNumbers.add("2019CS101");
        String studentName = null;

        studentDALMockedStatic.when(() ->  StudentDAL.getStudentsEntryNumbers(batch)).thenReturn(studentsEntryNumbers);
        studentDALMockedStatic.when(() ->  StudentDAL.getName("2019CS101")).thenReturn(studentName);

        String expectedMessage = "Error in getting the student name";
        String actualMessage = officeStaff.generateTranscript(batch);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getStudentsEntryNumbers(batch));
        studentDALMockedStatic.verify(() ->  StudentDAL.getName("2019CS101"));
        studentDALMockedStatic.close();



    }

//    Error in getting the student grades
    @Test
    void generateTranscriptErrorInGettingStudentsGrades() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        MockedStatic< Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic( Course_Offerings.class);
        String batch = "2019";
        ArrayList<String> studentsEntryNumbers = new ArrayList<>();
        studentsEntryNumbers.add("2019CS101");
        String studentName = "Student1";
        GradeDTO[] studentGrades = new GradeDTO[0];


        studentDALMockedStatic.when(() ->  StudentDAL.getStudentsEntryNumbers(batch)).thenReturn(studentsEntryNumbers);
        studentDALMockedStatic.when(() ->  StudentDAL.getName("2019CS101")).thenReturn(studentName);
        studentDALMockedStatic.when(() ->  Course_Offerings.view_all_grade("2019CS101")).thenReturn(studentGrades);


        String expectedMessage = "Error in getting the student grades";
        String actualMessage = officeStaff.generateTranscript(batch);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getStudentsEntryNumbers(batch));
        studentDALMockedStatic.verify(() ->  StudentDAL.getName("2019CS101"));
        studentDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(() ->  Course_Offerings.view_all_grade("2019CS101"));
        courseOfferingsMockedStatic.close();
    }

//    Error in getting the credits of the course
    @Test
    void generateTranscriptErrorInGettingCreditsOfTheCourse() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        MockedStatic< Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic( Course_Offerings.class);
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);
        String batch = "2019";
        ArrayList<String> studentsEntryNumbers = new ArrayList<>();
        studentsEntryNumbers.add("2019CS101");
        String studentName = "Student1";
        GradeDTO[] studentGrades = new GradeDTO[1];
        studentGrades[0] = new GradeDTO("CSE101","Intermediate Programming", "A","EVEN 2021");
        String courseCredits = null;

        studentDALMockedStatic.when(() ->  StudentDAL.getStudentsEntryNumbers(batch)).thenReturn(studentsEntryNumbers);
        studentDALMockedStatic.when(() ->  StudentDAL.getName("2019CS101")).thenReturn(studentName);
        studentDALMockedStatic.when(() ->  Course_Offerings.view_all_grade("2019CS101")).thenReturn(studentGrades);
        courseCatalogMockedStatic.when(() ->  Course_catalog.getCreditStructure("CSE101")).thenReturn(courseCredits);

        String expectedMessage = "Error in getting the credits of the course";
        String actualMessage = officeStaff.generateTranscript(batch);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getStudentsEntryNumbers(batch));
        studentDALMockedStatic.verify(() ->  StudentDAL.getName("2019CS101"));
        studentDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(() ->  Course_Offerings.view_all_grade("2019CS101"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() ->  Course_catalog.getCreditStructure("CSE101"));
        courseCatalogMockedStatic.close();
    }

//    Error in getting the CGPA
    @Test
    void generateTranscriptErrorInGettingCGPA() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        MockedStatic< Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic( Course_Offerings.class);
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);

        String batch = "2019";
        ArrayList<String> studentsEntryNumbers = new ArrayList<>();
        studentsEntryNumbers.add("2019CS101");
        String studentName = "Student1";
        GradeDTO[] studentGrades = new GradeDTO[1];
        studentGrades[0] = new GradeDTO("CSE101","Intermediate Programming", "A","EVEN 2021");
        String courseCredits = "3-0-0-3";
        double cgpa = -1;

        studentDALMockedStatic.when(() ->  StudentDAL.getStudentsEntryNumbers(batch)).thenReturn(studentsEntryNumbers);
        studentDALMockedStatic.when(() ->  StudentDAL.getName("2019CS101")).thenReturn(studentName);
        studentDALMockedStatic.when(() ->  Course_Offerings.view_all_grade("2019CS101")).thenReturn(studentGrades);
        courseCatalogMockedStatic.when(() ->  Course_catalog.getCreditStructure("CSE101")).thenReturn(courseCredits);
        studentDALMockedStatic.when(() ->  StudentDAL.getCGPA("2019CS101", studentGrades)).thenReturn(cgpa);

        String expectedMessage = "Error in getting the CGPA";
        String actualMessage = officeStaff.generateTranscript(batch);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getStudentsEntryNumbers(batch));
        studentDALMockedStatic.verify(() ->  StudentDAL.getName("2019CS101"));
        studentDALMockedStatic.verify(() ->  StudentDAL.getCGPA("2019CS101", studentGrades));
        studentDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(() ->  Course_Offerings.view_all_grade("2019CS101"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() ->  Course_catalog.getCreditStructure("CSE101"));
        courseCatalogMockedStatic.close();
    }

//    successful generation of transcript
    @Test
    void generateTranscriptSuccess() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        MockedStatic< Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic( Course_Offerings.class);
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);

        String batch = "2019";
        ArrayList<String> studentsEntryNumbers = new ArrayList<>();
        studentsEntryNumbers.add("2019CS101");
        String studentName = "Student1";
        GradeDTO[] studentGrades = new GradeDTO[1];
        studentGrades[0] = new GradeDTO("CSE101","Intermediate Programming", "A","EVEN 2021");
        String courseCredits = "3-0-0-3";
        double cgpa = 3.5;

        studentDALMockedStatic.when(() ->  StudentDAL.getStudentsEntryNumbers(batch)).thenReturn(studentsEntryNumbers);
        studentDALMockedStatic.when(() ->  StudentDAL.getName("2019CS101")).thenReturn(studentName);
        courseOfferingsMockedStatic.when(() ->  Course_Offerings.view_all_grade("2019CS101")).thenReturn(studentGrades);
        courseCatalogMockedStatic.when(() ->  Course_catalog.getCreditStructure("CSE101")).thenReturn(courseCredits);
        studentDALMockedStatic.when(() ->  StudentDAL.getCGPA("2019CS101", studentGrades)).thenReturn(cgpa);

        String expectedMessage = "Transcript generated successfully";
        String actualMessage = officeStaff.generateTranscript(batch);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getStudentsEntryNumbers(batch));
        studentDALMockedStatic.verify(() ->  StudentDAL.getName("2019CS101"));
        studentDALMockedStatic.verify(() ->  StudentDAL.getCGPA("2019CS101", studentGrades));
        studentDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(() ->  Course_Offerings.view_all_grade("2019CS101"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() ->  Course_catalog.getCreditStructure("CSE101"));
        courseCatalogMockedStatic.close();
    }

//    check graduation unauthorized . Parametrs: String checkGraduation(String studentEntryNumber)
    @Test
    void checkGraduation() throws SQLException {
        Authenticator.logout();
        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.checkGraduation("2019CS101");
        assertEquals(expectedMessage,actualMessage);
    }

//    Invalid student entry number
    @Test
    void checkGraduationInvalidStudentEntryNumber() throws SQLException {
        String wrongEntryNumber = "Invalid student entry number";
        String[] invalidEntryNumbers = {"2019CS1010","201CS101","2019CS10","2019CS10111"};
        String[] actualMessages = new String[4];
        String[] expectedMessages = new String[4];
        for(int i=0;i<4;i++){
            expectedMessages[i] = wrongEntryNumber;
        }
        for(int i=0;i<4;i++){
            actualMessages[i] = officeStaff.checkGraduation(invalidEntryNumbers[i]);
        }

        assertArrayEquals(expectedMessages,actualMessages);


    }

//    "Error in getting the student batch"
    @Test
    void checkGraduationBatchError(){
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        String studentEntryNumber = "2019CSB1021";
        String batch = null;

        studentDALMockedStatic.when(() ->  StudentDAL.getBatch(studentEntryNumber)).thenReturn(batch);
        String expectedMessage = "Error in getting the student batch";
        String actualMessage = officeStaff.checkGraduation(studentEntryNumber);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getBatch(studentEntryNumber));
        studentDALMockedStatic.close();
    }

//    "Error in getting the student Grades"
    @Test
    void checkGraduationGradesError(){
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        MockedStatic< Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic( Course_Offerings.class);

        String studentEntryNumber = "2019CSB1021";
        String batch = "2019";
        GradeDTO[] studentGrades = new GradeDTO[0];

        studentDALMockedStatic.when(() ->  StudentDAL.getBatch(studentEntryNumber)).thenReturn(batch);
        courseOfferingsMockedStatic.when(() ->  Course_Offerings.view_all_grade(studentEntryNumber)).thenReturn(studentGrades);

        String expectedMessage = "Error in getting the student grades";
        String actualMessage = officeStaff.checkGraduation(studentEntryNumber);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getBatch(studentEntryNumber));
        studentDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(() ->  Course_Offerings.view_all_grade(studentEntryNumber));
        courseOfferingsMockedStatic.close();

    }

//  has not completed all core courses, elective courses and credits
    @Test
    void checkGraduationHasNotCompletedAllCoreCoursesElectiveCoursesAndCredits() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        MockedStatic< Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic( Course_Offerings.class);
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);

        String studentEntryNumber = "2019CSB1021";
        String batch = "2019";
        GradeDTO[] studentGrades = new GradeDTO[1];
        studentGrades[0] = new GradeDTO("CSE101","Intermediate Programming", "A","EVEN 2021");
        String courseCredits = "3-0-0-3";
        UgCurriculumDTO[] ugCurriculumDTOS = new UgCurriculumDTO[2];
        ugCurriculumDTOS[0] = new UgCurriculumDTO("2020","CS101","Core","5","1");
        ugCurriculumDTOS[1] = new UgCurriculumDTO("2020","CS102","Core","5","1");

        studentDALMockedStatic.when(() ->  StudentDAL.getBatch(studentEntryNumber)).thenReturn(batch);
        courseOfferingsMockedStatic.when(() ->  Course_Offerings.view_all_grade(studentEntryNumber)).thenReturn(studentGrades);
        courseCatalogMockedStatic.when(() ->  Course_catalog.getCreditStructure("CSE101")).thenReturn(courseCredits);
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(ugCurriculumDTOS);

        String expectedMessage = "Student has not completed all the core courses in the curriculum Student has not completed the minimum number of credits Student has not completed the minimum number of electives";
        String actualMessage = officeStaff.checkGraduation(studentEntryNumber);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getBatch(studentEntryNumber));
        studentDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(() ->  Course_Offerings.view_all_grade(studentEntryNumber));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() ->  Course_catalog.getCreditStructure("CSE101"));
        courseCatalogMockedStatic.close();

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.close();
    }

//    has completed all core courses, elective courses and credits and is eligible for graduation
    @Test
    void checkGraduationHasCompletedAllCoreCoursesElectiveCoursesAndCreditsAndIsEligibleForGraduation() {
        MockedStatic< StudentDAL> studentDALMockedStatic = Mockito.mockStatic( StudentDAL.class);
        MockedStatic< Course_Offerings> courseOfferingsMockedStatic = Mockito.mockStatic( Course_Offerings.class);
        MockedStatic< Course_catalog> courseCatalogMockedStatic = Mockito.mockStatic( Course_catalog.class);
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);

        String studentEntryNumber = "2019CSB1021";
        String batch = "2019";
        GradeDTO[] studentGrades = new GradeDTO[3];
        studentGrades[0] = new GradeDTO("CS101","Core", "A","EVEN 2021");
        studentGrades[1] = new GradeDTO("CS102","Core", "A","EVEN 2021");
        studentGrades[2] = new GradeDTO("CS103","Core", "A","EVEN 2021");
        String courseCredits = "3-0-0-3";
        UgCurriculumDTO[] ugCurriculumDTOS = new UgCurriculumDTO[3];
        ugCurriculumDTOS[0] = new UgCurriculumDTO("2020","CS101","Core","5","1");
        ugCurriculumDTOS[1] = new UgCurriculumDTO("2020","CS102","Core","5","1");
        ugCurriculumDTOS[2] = new UgCurriculumDTO("2020","CS103","Elective","5","1");


        studentDALMockedStatic.when(() ->  StudentDAL.getBatch(studentEntryNumber)).thenReturn(batch);
        courseOfferingsMockedStatic.when(() ->  Course_Offerings.view_all_grade(studentEntryNumber)).thenReturn(studentGrades);
        courseCatalogMockedStatic.when(() ->  Course_catalog.getCreditStructure(any())).thenReturn(courseCredits);
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(ugCurriculumDTOS);

        String expectedMessage = "Graduation eligible";
        String actualMessage = officeStaff.checkGraduation(studentEntryNumber);

        assertEquals(expectedMessage,actualMessage);

        studentDALMockedStatic.verify(() ->  StudentDAL.getBatch(studentEntryNumber));
        studentDALMockedStatic.close();

        courseOfferingsMockedStatic.verify(() ->  Course_Offerings.view_all_grade(studentEntryNumber));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() ->  Course_catalog.getCreditStructure(any()),times(3));
        courseCatalogMockedStatic.close();

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.close();
    }


//    not authorized to add course. Parameters: String addCourseUGCurriculum(String courseCode, String batch, CourseType courseTypeStr) {
    @Test
    void addCourseUGCurriculumUnauthorized() throws SQLException {
        Authenticator.logout();
        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.addCourseUGCurriculum("CS101","2020", CourseType.CORE);
        assertEquals(expectedMessage,actualMessage);
    }

//    Invalid course code";
    @Test
    void addCourseUGCurriculumInvalidCourseCode() {
        String expectedMessage = "Invalid course code";
        String actualMessage = officeStaff.addCourseUGCurriculum("XXS101","2020", CourseType.CORE);
        assertEquals(expectedMessage,actualMessage);
    }

//    Invalid batch
    @Test
    void addCourseUGCurriculumInvalidBatch() {
        String expectedMessage = "Invalid batch";
        String actualMessage = officeStaff.addCourseUGCurriculum("CS101","202", CourseType.CORE);
        assertEquals(expectedMessage,actualMessage);
    }

//    already exists in the curriculum
    @Test
    void addCourseUGCurriculumAlreadyExistsInCurriculum() {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        String courseCode = "CS101";
        String batch = "2020";
        CourseType courseType = CourseType.CORE;
        UgCurriculumDTO[] ugCurriculumDTOS = new UgCurriculumDTO[1];
        ugCurriculumDTOS[0] = new UgCurriculumDTO(batch,courseCode,courseType.toString(),"5","1");
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(ugCurriculumDTOS);

        String expectedMessage = "Course already in the curriculum";
        String actualMessage = officeStaff.addCourseUGCurriculum(courseCode,batch,courseType);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.close();

    }

//    ug curriculum is null
    @Test
    void addCourseUGCurriculumUGCurriculumIsNull() {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        String courseCode = "CS101";
        String batch = "2020";
        CourseType courseType = CourseType.CORE;
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(null);

        String expectedMessage = "Error in getting the courses in the curriculum";
        String actualMessage = officeStaff.addCourseUGCurriculum(courseCode,batch,courseType);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.close();
    }

//    Error in adding the course to the curriculum
    @Test
    void addCourseUGCurriculumErrorInAddingCourseToCurriculum() throws SQLException {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        String courseCode = "CS101";
        String batch = "2020";
        CourseType courseType = CourseType.CORE;
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(new UgCurriculumDTO[0]);
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.addCourseToUGCurriculum(courseCode,batch,courseType)).thenReturn(false);

        String expectedMessage = "Error in adding the course to the curriculum";
        String actualMessage = officeStaff.addCourseUGCurriculum(courseCode,batch,courseType);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.addCourseToUGCurriculum(courseCode,batch,courseType));
        curriculumDALMockedStatic.close();
    }

//  "Course added to the curriculum successfully";
    @Test
    void addCourseUGCurriculumSuccess() throws SQLException {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        String courseCode = "CS101";
        String batch = "2020";
        CourseType courseType = CourseType.CORE;
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(new UgCurriculumDTO[0]);
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.addCourseToUGCurriculum(courseCode,batch,courseType)).thenReturn(true);

        String expectedMessage = "Course added to the curriculum successfully";
        String actualMessage = officeStaff.addCourseUGCurriculum(courseCode,batch,courseType);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.addCourseToUGCurriculum(courseCode,batch,courseType));
        curriculumDALMockedStatic.close();
    }

//    not authorized to add batch. Parameters: String addBatch(String batch, int minCredits, int minElectiveCourses)
    @Test
    void addBatchUnauthorized() throws SQLException {
        Authenticator.logout();
        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.addBatch("2020", 120, 2);
        assertEquals(expectedMessage,actualMessage);
    }

//    Invalid batch
    @Test
    void addBatchInvalidBatch() {
        String expectedMessage = "Invalid batch";
        String actualMessage = officeStaff.addBatch("202", 120, 2);
        assertEquals(expectedMessage,actualMessage);
    }

//    batch already exists
    @Test
    void addBatchAlreadyExists() {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        UgCurriculumDTO[] ugCurriculumDTOS = new UgCurriculumDTO[1];
        ugCurriculumDTOS[0] = new UgCurriculumDTO("2020","CS101",CourseType.CORE.toString(),"5","1");
        String batch = "2020";
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(ugCurriculumDTOS);

        String expectedMessage = "Batch already in the curriculum";
        String actualMessage = officeStaff.addBatch(batch, 120, 2);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.close();
    }

//    Error in getting the courses in the curriculum"
    @Test
    void addBatchErrorInGettingCoursesInCurriculum() {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        String batch = "2020";
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(null);

        String expectedMessage = "Error in getting the courses in the curriculum";
        String actualMessage = officeStaff.addBatch(batch, 120, 2);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.close();
    }

//    Error in adding the batch to the curriculum
    @Test
    void addBatchErrorInAddingBatchToCurriculum() throws SQLException {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        String batch = "2020";
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(new UgCurriculumDTO[0]);
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.addBatch(batch,120,2)).thenReturn(false);

        String expectedMessage = "Error in adding the batch to the curriculum";
        String actualMessage = officeStaff.addBatch(batch, 120, 2);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.addBatch(batch,120,2));
        curriculumDALMockedStatic.close();
    }

//    Batch added to the curriculum successfully
    @Test
    void addBatchSuccess() throws SQLException {
        MockedStatic< CurriculumDAL> curriculumDALMockedStatic = Mockito.mockStatic( CurriculumDAL.class);
        String batch = "2020";
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch)).thenReturn(new UgCurriculumDTO[0]);
        curriculumDALMockedStatic.when(() ->  CurriculumDAL.addBatch(batch,120,2)).thenReturn(true);

        String expectedMessage = "Batch added to the curriculum successfully";
        String actualMessage = officeStaff.addBatch(batch, 120, 2);
        assertEquals(expectedMessage,actualMessage);

        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.getCoursesInUGCurriculum(batch));
        curriculumDALMockedStatic.verify(() ->  CurriculumDAL.addBatch(batch,120,2));
        curriculumDALMockedStatic.close();
    }

//    not authorized edit profile. Parameters: String editProfile(String string, String name)
    @Test
    void editProfileUnauthorized() throws SQLException {
        Authenticator.logout();
        String expectedMessage = "You are not authorized to perform this action";
        String actualMessage = officeStaff.editProfile("name", "name");
        assertEquals(expectedMessage,actualMessage);
    }

//   edit name invalid format
    @Test
    void editProfileNameInvalid() {
        String expectedMessage = "Invalid name";
        String actualMessage = officeStaff.editProfile("name", "test134");
        assertEquals(expectedMessage,actualMessage);
    }

//    edit name in valid format
    @Test
    void editProfileNameValid() {
        MockedStatic< OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic( OfficeStaffDAL.class);
        officeStaffDALMockedStatic.when(() ->  OfficeStaffDAL.updateName(officeStaff.getUserName(),"test name")).thenReturn(true);
        officeStaffDALMockedStatic.when(() ->  OfficeStaffDAL.updateName(officeStaff.getUserName(),"test name Error")).thenReturn(false);

        String expectedMessage = "Profile updated successfully";
        String expectedMessage2 = "Error in updating the name";
        String actualMessage = officeStaff.editProfile("name", "test name");
        String actualMessage2 = officeStaff.editProfile("name", "test name Error");

        assertEquals(expectedMessage,actualMessage);
        assertEquals(expectedMessage2,actualMessage2);

        officeStaffDALMockedStatic.verify(() ->  OfficeStaffDAL.updateName(officeStaff.getUserName(),"test name"));
        officeStaffDALMockedStatic.verify(() ->  OfficeStaffDAL.updateName(officeStaff.getUserName(),"test name Error"));
        officeStaffDALMockedStatic.close();

    }

//    edit email in invalid format
    @Test
    void editProfileEmailInvalid() {
        String expectedMessage = "Invalid email";
        String actualMessage = officeStaff.editProfile("email", "test");
        assertEquals(expectedMessage,actualMessage);
    }

//    edit email in valid format
    @Test
    void editProfileEmailValid() {
        MockedStatic< OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic( OfficeStaffDAL.class);
        officeStaffDALMockedStatic.when(() ->  OfficeStaffDAL.updateEmail(officeStaff.getUserName(),"email@domain.com")).thenReturn(true);
        officeStaffDALMockedStatic.when(() ->  OfficeStaffDAL.updateEmail(officeStaff.getUserName(),"email2@domain.com")).thenReturn(true);

        String expectedMessage = "Profile updated successfully";
        String expectedMessage2 = "Error in updating the email";
        String actualMessage = officeStaff.editProfile("email", "email@domain.com");
        String actualMessage2 = officeStaff.editProfile("email", "email2@iitrpr.ac.in");

        assertEquals(expectedMessage, actualMessage);
        assertEquals(expectedMessage2, actualMessage2);

        officeStaffDALMockedStatic.verify(() ->  OfficeStaffDAL.updateEmail(officeStaff.getUserName(),"email@domain.com"));
        officeStaffDALMockedStatic.verify(() ->  OfficeStaffDAL.updateEmail(officeStaff.getUserName(),"email2@iitrpr.ac.in"));
        officeStaffDALMockedStatic.close();
    }

//    edit password in invalid format (length < 3)
    @Test
    void editProfilePasswordInvalid() {
        String expectedMessage = "Password must contain at least 3 characters";
        String actualMessage = officeStaff.editProfile("password", "12");
        assertEquals(expectedMessage, actualMessage);
    }

//    edit password in valid format
    @Test
    void editPasswordValid(){
        MockedStatic< OfficeStaffDAL> officeStaffDALMockedStatic = Mockito.mockStatic( OfficeStaffDAL.class);
        officeStaffDALMockedStatic.when(() ->  OfficeStaffDAL.updatePassword(officeStaff.getUserName(),"1234")).thenReturn(true);
        officeStaffDALMockedStatic.when(() ->  OfficeStaffDAL.updatePassword(officeStaff.getUserName(),"12345")).thenReturn(false);

        String expectedMessage = "Profile updated successfully";
        String expectedMessage2 = "Error in updating the password";
        String actualMessage = officeStaff.editProfile("password","1234");
        String actualMessage2 = officeStaff.editProfile("password","12345");

        assertEquals(expectedMessage,actualMessage);
        assertEquals(expectedMessage2,actualMessage2);

        officeStaffDALMockedStatic.verify(() ->  OfficeStaffDAL.updatePassword(officeStaff.getUserName(),"1234"));
        officeStaffDALMockedStatic.verify(() ->  OfficeStaffDAL.updatePassword(officeStaff.getUserName(),"12345"));
        officeStaffDALMockedStatic.close();

    }


}