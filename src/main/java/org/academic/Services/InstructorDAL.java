package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.cli.OutputHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;


public class InstructorDAL {
    private InstructorDAL() {
    }

    public static String getName(String instructorId) {
        String instructorName = null;
        String query = "SELECT * FROM instructor WHERE instructor_id = '" + instructorId + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                instructorName = rs.getString("name");
            }
        } catch (SQLException e) {
            instructorName = "Error";
            OutputHandler.logError("Get name error:"+e.getMessage());
        }
        return instructorName;
    }
}
