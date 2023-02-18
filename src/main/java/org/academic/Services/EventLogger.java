package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.cli.OutputHandler;

import java.sql.Connection;
import java.sql.SQLException;

public class EventLogger {

    //    Make the constructor private so that this class cannot be instantiated
    private EventLogger() {
    }

    public static void logEvent(String username, String message, String eventTime, String loginID) {
        String query = "INSERT INTO event_log VALUES ('%s', '%s', '%s', '%s')".formatted(username, eventTime, message, loginID);
        OutputHandler.logError(query);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            OutputHandler.logError("Error while logging event: " + e.getMessage());
        }

    }


}
