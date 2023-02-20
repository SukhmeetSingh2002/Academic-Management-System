package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.cli.OutputHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

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
}
