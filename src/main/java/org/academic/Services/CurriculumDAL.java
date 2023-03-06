package org.academic.Services;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;

import org.academic.CourseType;
import org.academic.Database.Connector;
import org.academic.Database.UgCurriculumDTO;
import org.academic.cli.OutputHandler;

public class CurriculumDAL {

    public static UgCurriculumDTO[] getCoursesInUGCurriculum(String studentBatch) {
        String query = "SELECT * FROM ug_curriculum WHERE batch = '" + studentBatch + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            ArrayList<UgCurriculumDTO> ugCurriculum = new ArrayList<>();
            while (rs.next()) {
                String batch = rs.getString("batch");
                String courseCode = rs.getString("course_code");
                String type = rs.getString("course_type");
                String minCredits = CurriculumDAL.getCourseMinCredits(batch);
                String minElectives = CurriculumDAL.getCourseMinElectives(batch);

                UgCurriculumDTO ugCurriculumDTO = new UgCurriculumDTO(batch, courseCode, type, minCredits,
                        minElectives);
                ugCurriculum.add(ugCurriculumDTO);

            }
            return ugCurriculum.toArray(new UgCurriculumDTO[0]);
        } catch (Exception e) {
            OutputHandler.logError("Error while getting courses in ug curriculum: " + e.getMessage());
        }
        return null;

    }

    private static String getCourseMinElectives(String batch) {
        String query = "SELECT * FROM batch WHERE batchyear = '" + batch + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                return rs.getString("min_program_electives");
            }
        } catch (Exception e) {
            OutputHandler.logError("Error while getting course min electives: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
        }
        return null;
    }

    private static String getCourseMinCredits(String batch) {
        String query = "SELECT * FROM batch WHERE batchyear = '" + batch + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                return rs.getString("min_credits");
            }
        } catch (Exception e) {
            OutputHandler.logError("Error while getting course min credits: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
        }
        return null;
    }

    public static boolean addCourseToUGCurriculum(String courseCode, String batch, CourseType courseTypeStr) {
        String query = "INSERT INTO ug_curriculum (course_code, batch, course_type) VALUES ('%s', '%s', '%s')".formatted(
                courseCode, batch, courseTypeStr);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
            return true;
        } catch (Exception e) {
            OutputHandler.logError("Error while adding course to ug curriculum: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
        }
        return false;
    }

    public static boolean addBatch(String batch, int minCredits, int minElectiveCourses) {
        String query = "INSERT INTO batch (batchyear, min_credits, min_program_electives) VALUES ('%s', '%d', '%d')".formatted(
                batch, minCredits, minElectiveCourses);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
            return true;
        } catch (Exception e) {
            OutputHandler.logError("Error while adding batch: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
        }
        return false;
    }
}