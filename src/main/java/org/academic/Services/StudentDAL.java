package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.cli.OutputHandler;

import java.sql.Connection;
import java.sql.SQLException;

public class StudentDAL {
    private StudentDAL() {
    }

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
}
