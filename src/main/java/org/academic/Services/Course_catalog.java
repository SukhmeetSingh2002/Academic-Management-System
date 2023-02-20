package org.academic.Services;

import org.academic.Database.Connector;
import org.academic.Database.Course_CatalogDTO;
import org.academic.cli.OutputHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class Course_catalog {

    private Course_catalog() {
    }

    public static String getCourseName(String course_code) {
        String courseName = null;
        String query = "SELECT * FROM course_catalog WHERE course_code = '" + course_code + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                courseName = rs.getString("course_name");
            }
        } catch (SQLException e) {
            courseName = "Error";
        }
        return courseName;

    }

    public static String[] getCoursePrerequisites(String course_code) {
//        TODO: add grade to the prerequisites
        ArrayList<String> coursePrerequisites = new ArrayList<>();
        String query = "SELECT * FROM prerequisites WHERE course_code = '" + course_code + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                coursePrerequisites.add(rs.getString("prerequisite_course_code"));
            }
        } catch (SQLException e) {
            coursePrerequisites.add("No Prerequisites");
        }
        return coursePrerequisites.toArray(new String[0]);

    }

    public static String getCreditStructure(String course_code) {
        String creditStructure = null;
        String query = "SELECT * FROM course_catalog WHERE course_code = '" + course_code + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                creditStructure = rs.getString("credit_structure");
            }
        } catch (SQLException e) {
            creditStructure = "Error";
        }
        return creditStructure;

    }

    public static Course_CatalogDTO[] get_courses() {
        String query = "SELECT * FROM course_catalog";
        ArrayList<Course_CatalogDTO> courses = new ArrayList<>();
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            while (rs.next()) {
                String course_code = rs.getString("course_code");
                String course_name = rs.getString("course_name");
                String[] course_prerequisites = getCoursePrerequisites(course_code);
                String credit_structure = rs.getString("credit_structure");

                courses.add(new Course_CatalogDTO(course_code, course_name, course_prerequisites, credit_structure));
            }
        } catch (SQLException e) {
            OutputHandler.logError("Error in getting courses"+e.getMessage());
            courses = new ArrayList<>();
            courses.add(new Course_CatalogDTO("Error", "Error", new String[]{"Error"}, "Error"));
        }
        return courses.toArray(new Course_CatalogDTO[0]);
    }

}
