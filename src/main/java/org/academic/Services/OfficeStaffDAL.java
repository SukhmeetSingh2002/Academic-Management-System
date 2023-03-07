package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.User.UserType;
import org.academic.cli.OutputHandler;

import java.sql.Connection;
import java.sql.ResultSet;

public class OfficeStaffDAL {

    public static String getFirstName(String userName) {
        String query = "SELECT name FROM office_staff WHERE username = '" + userName + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                return rs.getString("name");
            }
        } catch (Exception e) {
            OutputHandler.logError("Error while getting first name: " + e.getMessage());
        }
        return "Error";
    }
    
    // staff id
    public static String getStaffID(String userName) {
        String query = "SELECT staff_id FROM office_staff WHERE username = '" + userName + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                return rs.getString("staff_id");
            }
        } catch (Exception e) {
            OutputHandler.logError("Error while getting staff id: " + e.getMessage());
        }
        return "Error";
    }
    
    // get current event from academic calendar
    public static String[] getCurrentEvent() {
        String query = "SELECT * FROM academic_calendar WHERE is_current = true";
        String[] event = new String[3];
        event[0] = "SEMESTER_END";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                event[0] = rs.getString("event_name");
                event[1] = rs.getString("event_date");
                event[2] = rs.getString("semester");
            }
        } catch (Exception e) {
            event[0] = "Error";
            event[1] = e.getMessage();
            event[2] = "Error";
            OutputHandler.logError("Error while getting current event: " + e.getMessage());
        }
        return event;
    }

    // close the current event
    public static boolean closeCurrentEvent() {
        String query = "UPDATE academic_calendar SET is_current = false WHERE is_current = true";
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while closing current event: " + e.getMessage());
        }
        return false;
    }

    // open the next event
    public static boolean openNextEvent(String nextEvent, String semester, String startDate) {
        String query = "INSERT INTO academic_calendar VALUES ('%s', '%s', '%s', true)".formatted(nextEvent, startDate, semester);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while opening next event: " + e.getMessage());
            OutputHandler.logError(query);
        }
        return false;
    }

    public static boolean updateSemester(String currentSemester) {
        String query = "INSERT INTO semester VALUES ('%s', '%s', true)".formatted(currentSemester, currentSemester.split(" ")[1]);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
            return true;
        } catch (Exception e) {
            OutputHandler.logError("Error while updating semester: " + e.getMessage());
            OutputHandler.logError(query);
        }
        return false;

    }

    public static boolean closeCurrentSemester() {
        String query = "UPDATE semester SET is_current_semester = false WHERE is_current_semester = true";
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            OutputHandler.logError("Rows affected: " + rowsAffected);
            OutputHandler.logError(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while closing current semester: " + e.getMessage());
        }
        return false;
    }

    public static boolean isStudentExists(String studentEntryNumber) {
        String query = "SELECT * FROM student WHERE entry_number = '%s'".formatted(studentEntryNumber);
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            return rs.next();
        } catch (Exception e) {
            OutputHandler.logError("Error while checking if student exists: " + e.getMessage());
        }
        return false;
    }

    public static boolean addNewUser(String userName, String password, UserType userType) {
        String query = "INSERT INTO user_authentication VALUES ('%s', '%s', '%s')".formatted(userName, password, userType);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while adding new user: " + e.getMessage());
            OutputHandler.logError(query);
        }
        return false;
    }

    public static boolean addStudent(String studentEntryNumber, String studentName, String studentEmail, int studentBatch, String studentUserName) {
        String query = "INSERT INTO student VALUES ('%s', '%s', '%s', '%s', %d)".formatted(studentEntryNumber, studentName, studentUserName,
                studentEmail, studentBatch);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while adding new student: " + e.getMessage());
            OutputHandler.logError(query);
        }
        return false;

    }

    public static boolean addFaculty(String facultyUserName, String facultyName, String facultyEmail, String facultyDepartment) {
        String query = "INSERT INTO instructor (name, username, email, department) VALUES ('%s', '%s', '%s', '%s')".formatted(facultyName, facultyUserName, facultyEmail, facultyDepartment);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while adding new faculty: " + e.getMessage());
            OutputHandler.logError(query);
        }
        return false;
    }

    public static boolean isFacultyExists(String facultyUserName) {
        String query = "SELECT * FROM instructor WHERE username = '%s'".formatted(facultyUserName);
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            return rs.next();
        } catch (Exception e) {
            OutputHandler.logError("Error while checking if faculty exists: " + e.getMessage());
        }
        return false;
    }

    public static boolean isStaffExists(String staffUserName) {
        String query = "SELECT * FROM office_staff WHERE username = '%s'".formatted(staffUserName);
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            return rs.next();
        } catch (Exception e) {
            OutputHandler.logError("Error while checking if staff exists: " + e.getMessage());
        }
        return false;
    }

    public static boolean addStaff(String staffUserName, String staffName, String staffEmail) {
        String query = "INSERT INTO office_staff (name, email, username) VALUES ('%s', '%s', '%s')".formatted(staffName, staffEmail, staffUserName);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while adding new staff: " + e.getMessage());
            OutputHandler.logError(query);
        }
        return false;
    }

    public static String getEmail(String userName) {
        String query = "SELECT email FROM office_staff WHERE username = '%s'".formatted(userName);
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                return rs.getString("email");
            }
        } catch (Exception e) {
            OutputHandler.logError("Error while getting email: " + e.getMessage());
        }
        return null;
    }

    public static boolean updateName(String userName, String name) {
        String query = "UPDATE office_staff SET name = '%s' WHERE username = '%s'".formatted(name, userName);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while updating name: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateEmail(String userName, String name) {
        String query = "UPDATE office_staff SET email = '%s' WHERE username = '%s'".formatted(name, userName);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while updating email: " + e.getMessage());
        }
        return false;
    }

    public static boolean updatePassword(String userName, String name) {
        String query = "UPDATE user_authentication SET password = '%s' WHERE user_name = '%s'".formatted(name, userName);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (Exception e) {
            OutputHandler.logError("Error while updating password: " + e.getMessage());
        }
        return false;
    }


//    for testing
}
