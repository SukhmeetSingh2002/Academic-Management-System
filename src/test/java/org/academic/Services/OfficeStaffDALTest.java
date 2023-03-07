package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.Events;
import org.academic.User.UserType;
import org.academic.cli.OutputHandler;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OfficeStaffDALTest {
//    data like student, faculty, staff, event, semester, etc. to be used in the test
    private static void deleteUserFromAuth(String studentUserName) {
        String query = "DELETE FROM user_authentication WHERE user_name = '%s'".formatted(studentUserName);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (Exception e) {
            OutputHandler.logError("\nError while deleting user from auth: " + e.getMessage());
        }
    }
    private final static String studentEntryNumber = "1234";
    private final static String studentFirstName = "John";
    private final static String studentEmail = "John@Doe.com";
    private final static String studentPassword = "JohnDoe";
    private final static String studentUserName = "JohnDoe";
    private final static int batch = 2020;
    private final static String staffID = "1234";
    private final static String semester = "EVEN 1025";
    private final static Events event = Events.COURSE_FLOAT_START;
    private final static String courseID = "CS101";
    private final static String courseName = "Intro to CS";

//    instructor data
    private final static String instructorID = "1234";
    private final static String instructorFirstName = "Instructor";
    private final static String instructorEmail = "Ins@iitrpr.ac.in";
    private final static String instructorPassword = "Instructor";
    private final static String instructorUserName = "Instructor";

//    staff data
    private final static String staffFirstName = "Staff";
    private final static String staffEmail = "staff@iitrpr.ac.in";
    private final static String staffPassword = "Staff";
    private final static String staffUserName = "Staff";




    @AfterAll
    static void cleanUp() {
//        delete all the users added in the test
        deleteStudent(studentEntryNumber);
        deleteFaculty(instructorUserName);
        deleteStaff(staffUserName);

        deleteUserFromAuth(studentUserName);
        deleteUserFromAuth(instructorUserName);
        deleteUserFromAuth(staffUserName);

        deleteEvent(event.toString());
        deleteSemester(semester);
    }

    private static void deleteEvent(String toString) {
        String query = "DELETE FROM academic_calendar WHERE event_name = '%s' and semester = '%s'".formatted(toString, semester);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (Exception e) {
            OutputHandler.logError("\nError while deleting event: " + e.getMessage());
        }
    }

    private static void deleteSemester(String semester) {
        String query = "DELETE FROM semester WHERE semester = '%s'".formatted(semester);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (Exception e) {
            OutputHandler.logError("\nError while deleting semester: " + e.getMessage());
        }
    }

    private static void deleteStaff(String staffUserName) {
        String query = "DELETE FROM office_staff WHERE username = '%s'".formatted(staffUserName);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (Exception e) {
            OutputHandler.logError("\nError while deleting staff: " + e.getMessage());
        }
    }

    private static void deleteFaculty(String instructorID) {
        String query = "DELETE FROM instructor WHERE username = '%s'".formatted(instructorID);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (Exception e) {
            OutputHandler.logError("\nError while deleting faculty: " + e.getMessage());
        }
    }

    private static void deleteStudent(String studentEntryNumber) {
        String query = "DELETE FROM student WHERE entry_number = '%s'".formatted(studentEntryNumber);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (Exception e) {
            OutputHandler.logError("\nError while deleting student: " + e.getMessage());
        }
    }

    @Test
    @Order(16)
    void getFirstName() {
        OutputHandler.logError("\n16. Getting first name");
        String result = OfficeStaffDAL.getFirstName(staffUserName);
        OutputHandler.logError("\nResult: " + result);
        assertEquals(staffFirstName+"1", result);
    }

    @Test
    @Order(15)
    void getStaffID() {
        OutputHandler.logError("\n15. Getting staff ID");
        String result = OfficeStaffDAL.getStaffID(staffUserName);
        OutputHandler.logError("\nResult: " + result);
        assertNotNull(result);
    }

    @Test
    @Order(14)
    void getCurrentEvent() {
        OutputHandler.logError("\n14. Getting current event");
        String[] result = OfficeStaffDAL.getCurrentEvent();
        OutputHandler.logError("\nResult: " + Arrays.toString(result));
        assertAll(
                () -> assertEquals(event.toString(), result[0]),
                () -> assertEquals("2020-10-10", result[1]),
                () -> assertEquals(semester, result[2])
        );
    }

    @Test
    @Order(17)
    void closeCurrentEvent() {
        OutputHandler.logError("\n17. Closing current event");
        boolean result = OfficeStaffDAL.closeCurrentEvent();
        assertTrue(result);
    }

    @Test
    @Order(13)
    void openNextEvent() {
        OutputHandler.logError("\n13. Opening next event");
        boolean result = OfficeStaffDAL.openNextEvent(event.toString(), semester, "2020-10-10");
        assertTrue(result);
    }

    @Test
    @Order(12)
    void updateSemester() {
        OutputHandler.logError("\n12. Updating semester");
        boolean result = OfficeStaffDAL.updateSemester(semester);
        assertTrue(result);
    }

    @Test
    @Order(18)
    void closeCurrentSemester() {
        boolean result = OfficeStaffDAL.closeCurrentSemester();
        assertTrue(result);
    }

    @Test
    @Order(11)
    void isStudentExists() {
        boolean result = OfficeStaffDAL.isStudentExists(studentEntryNumber);
        boolean result2 = OfficeStaffDAL.isStudentExists("12345");
        assertAll(
                () -> assertTrue(result),
                () -> assertFalse(result2)
        );
    }

    @Test
    @Order(1)
    void addNewUser() {
//        add 3 users
        boolean result = OfficeStaffDAL.addNewUser(studentUserName, studentPassword , UserType.STUDENT);
        boolean result2 = OfficeStaffDAL.addNewUser(instructorUserName, instructorPassword, UserType.FACULTY);
        boolean result3 = OfficeStaffDAL.addNewUser(staffUserName, staffPassword, UserType.OFFICE_STAFF);
        assertAll(
                () -> assertTrue(result),
                () -> assertTrue(result2),
                () -> assertTrue(result3)
        );
    }

    @Test
    @Order(2)
    void addStudent() {
        boolean result = OfficeStaffDAL.addStudent(studentEntryNumber, studentFirstName,studentEmail, batch, studentUserName);
        assertTrue(result);
    }

    @Test
    @Order(3)
    void addFaculty() {
        boolean result = OfficeStaffDAL.addFaculty(instructorUserName, instructorFirstName, instructorEmail, "CSE");
        assertTrue(result);
    }

    @Test
    @Order(5)
    void isFacultyExists() {
        boolean result = OfficeStaffDAL.isFacultyExists(instructorUserName);
        boolean result2 = OfficeStaffDAL.isFacultyExists("12345");
        assertAll(
                () -> assertTrue(result),
                () -> assertFalse(result2)
        );
    }

    @Test
    @Order(6)
    void isStaffExists() {
        boolean result = OfficeStaffDAL.isStaffExists(staffUserName);
        boolean result2 = OfficeStaffDAL.isStaffExists("12345");
        assertAll(
                () -> assertTrue(result),
                () -> assertFalse(result2)
        );

    }

    @Test
    @Order(4)
    void addStaff() {
        boolean result = OfficeStaffDAL.addStaff(staffUserName, staffFirstName, staffEmail);
        assertTrue(result);
    }

    @Test
    @Order(7)
    void getEmail() {
        String email = OfficeStaffDAL.getEmail(staffUserName);
        assertEquals(staffEmail, email);
    }

    @Test
    @Order(8)
    void updateName() {
        boolean result = OfficeStaffDAL.updateName(staffUserName, staffFirstName+"1");
        assertTrue(result);
    }

    @Test
    @Order(9)
    void updateEmail() {
        boolean result = OfficeStaffDAL.updateEmail(staffUserName, staffEmail+"1");
        assertTrue(result);
    }

    @Test
    @Order(10)
    void updatePassword() {
        boolean result = OfficeStaffDAL.updatePassword(staffUserName, staffPassword+"1");
        assertTrue(result);
    }
}