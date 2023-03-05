package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.Database.GradeDTO;
import org.academic.cli.OutputHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StudentDAL {
    private StudentDAL() {
    }

    // TODO: add this in auth detatils
    public static boolean changePassword(String userName, String oldPassword, String newPassword) {
        String query = "UPDATE user_authentication SET password = '%s' WHERE user_name = '%s' AND password = '%s'".formatted(newPassword, userName, oldPassword);
        OutputHandler.logError(query);
        try {
            Connection conn = Connector.getConnection();
            int rowsAffected = conn.createStatement().executeUpdate(query);
            return rowsAffected == 1;
        } catch (SQLException e) {
            OutputHandler.logError("Error while changing password: " + e.getMessage());
            return false;
        }
    }

    public static String getStudentEntryNumber(String username) {
        String studentEntryNumber = null;
        String query = "SELECT * FROM student WHERE username = '" + username + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                studentEntryNumber = rs.getString("entry_number");
            }
        } catch (SQLException e) {
            studentEntryNumber = "Error";
            OutputHandler.logError("Get student entry number error:"+e.getMessage());
        }
        return studentEntryNumber;
    }

    public static String getName(String studentEntryNo) {
        String name = null;
        String query = "SELECT * FROM student WHERE entry_number = '" + studentEntryNo + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                name = rs.getString("name");
            }
        } catch (SQLException e) {
            name = "Error";
            OutputHandler.logError("Get student name error: "+e.getMessage());
        }
        return name;
    }

    public static ArrayList<String> getStudentsEntryNumbers(String batch) {
        ArrayList<String> studentsEntryNumbers = new ArrayList<>();
        String query = "SELECT * FROM student WHERE batch = '" + batch + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                studentsEntryNumbers.add(rs.getString("entry_number"));
            }
        } catch (SQLException e) {
            OutputHandler.logError("Get students entry numbers error: "+e.getMessage());
        }
        return studentsEntryNumbers;
    }

    public static double getCGPA(String studentEntryNumber, GradeDTO[] studentGrades) {
        // get the total credits
        double totalCredits = 0;
        double[] allCourseCredits = new double[studentGrades.length];
        for (int i = 0; i < studentGrades.length; i++) {
            GradeDTO grade = studentGrades[i];

            // get the credits for the course
            String credits = Course_catalog.getCreditStructure(grade.course_code());

            if (credits == null) {
                return -1;
            }

            // extract the credits from the string and push it to the array
            String[] creditParts = credits.split("-");
            double courseCredits = Double.parseDouble(creditParts[3]);
            allCourseCredits[i] = courseCredits;
            totalCredits += courseCredits;
        }

        // calculate the gpa
        double gpa = 0.0;
        for (int i = 0; i < studentGrades.length; i++) {
            GradeDTO grade = studentGrades[i];
            String gradeValue = grade.grade();
            double courseCredits = allCourseCredits[i];

            // get the gpa for the grade
            double gradeGPA = convertGPA(gradeValue);

            if (gradeGPA == -1) {
                return -1;
            }

            gpa += gradeGPA * courseCredits;
        }
        gpa /= totalCredits;
        return gpa;
    }

    private static double convertGPA(String gradeValue) {
        switch (gradeValue) {
            case "A":
                return 10;
            case "A-":
                return 9;
            case "B":
                return 8;
            case "B-":
                return 7;
            case "C":
                return 6;
            case "C-":
                return 5;
            case "D":
                return 4;
            case "F":
                return 0;
            default:
                // if it is already a gpa between 0 and 10 then return it
                try {
                    int gpa = Integer.parseInt(gradeValue);
                    if (gpa >= 0 && gpa <= 10) {
                        return gpa;
                    }
                } catch (NumberFormatException e) {
                    OutputHandler.logError("Error while converting gpa: " + e.getMessage());
                }
                return -1;
        }
    }

    public static String getBatch(String studentEntryNumber) {
        String batch = null;
        String query = "SELECT * FROM student WHERE entry_number = '" + studentEntryNumber + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                batch = rs.getString("batch");
            }
        } catch (SQLException e) {
            OutputHandler.logError("Get student batch error: "+e.getMessage());
        }
        return batch;
    }

    public static String getEmail(String studentEntryNumber) {
        String email = null;
        String query = "SELECT * FROM student WHERE entry_number = '" + studentEntryNumber + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                email = rs.getString("email");
            }
        } catch (SQLException e) {
            OutputHandler.logError("Get student email error: "+e.getMessage());
        }
        return email;
    }
}
