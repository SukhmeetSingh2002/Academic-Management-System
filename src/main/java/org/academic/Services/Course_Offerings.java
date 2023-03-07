package org.academic.Services;

import org.academic.Database.Course_Offerings_DTO;
import org.academic.Database.GiveGradeDTO;
import org.academic.Database.CourseRegisterDTO;
import org.academic.Database.Connector;
import org.academic.Database.GradeDTO;
import org.academic.cli.OutputHandler;
//import org.academic.Services.Course_catalog;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;


public class Course_Offerings {
    //        make a call to the database to get the course offerings for the semester
    public static Course_Offerings_DTO[] view_course_offerings(String semester) {
        String query = "SELECT * FROM course_offerings WHERE semester = '" + semester + "'";
        Course_Offerings_DTO[] course_offeringDTOS;
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            ArrayList<Course_Offerings_DTO> courseOfferings = new ArrayList<>();
            while (rs.next()) {
//                Get the course code from the course offerings, and use it to get the course name, credit structure and prerequisites from the course catalog
                String courseCode = rs.getString("course_code");
                String courseName = Course_catalog.getCourseName(courseCode);
                String creditStructure = Course_catalog.getCreditStructure(courseCode);
                String[] prerequisites = Course_catalog.getCoursePrerequisites(courseCode);

//                Get the instructor name from the instructor id
                String instructorID = rs.getString("instructor_id");
                String instructorName = InstructorDAL.getName(instructorID);

//                Add the course offerings to the array list
                courseOfferings.add(new Course_Offerings_DTO(courseCode, courseName, instructorName, prerequisites, creditStructure,"NOT ALLOWED"));
            }
//            Convert the array list to an array
            course_offeringDTOS = courseOfferings.toArray(new Course_Offerings_DTO[0]);
        } catch (SQLException e) {
//            If there is an error, return an array with one element containing the error message
            course_offeringDTOS = new Course_Offerings_DTO[1];
            course_offeringDTOS[0] = new Course_Offerings_DTO("Error", "Error", "Error", new String[]{"Error"}, "Error","Error");
            OutputHandler.logError("Error while getting course offerings: " + e.getMessage());
        }
        return course_offeringDTOS;
    }

    //    view course registered
    public static CourseRegisterDTO[] view_course_registered(String student_entry_no) {
        CourseRegisterDTO[] courseRegisterDTOS;
        String query = "SELECT * FROM student_course_registration WHERE student_entry_number = '" + student_entry_no + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            ArrayList<CourseRegisterDTO> courseRegisters = new ArrayList<>();
            while (rs.next()) {
//                Get the course code from the course offerings, and use it to get the course name, credit structure and prerequisites from the course catalog
                String courseCode = rs.getString("course_code");
                String courseName = Course_catalog.getCourseName(courseCode);
                String creditStructure = Course_catalog.getCreditStructure(courseCode);

                String status = rs.getString("status");
                String semester = rs.getString("semester");
                String grade = view_grade_course(student_entry_no, courseCode, semester);
                
                String type = "Program Course";

//                Add the course offerings to the array list
                courseRegisters.add(new CourseRegisterDTO(courseCode, courseName, status, grade, type, semester, creditStructure));
            }
//            Convert the array list to an array
            courseRegisterDTOS = courseRegisters.toArray(new CourseRegisterDTO[0]);

        } catch (SQLException e) {
//            If there is an error, return an array with one element containing the error message
            courseRegisterDTOS = new CourseRegisterDTO[1];
            courseRegisterDTOS[0] = new CourseRegisterDTO("Error", "Error", "Error", "Error", "Error", "Error", "Error");
            OutputHandler.logError("Error while getting course offerings: " + e.getMessage());
        }
        return courseRegisterDTOS;
    }

    //    view grade
    public static String view_grade_course(String student_entry_no, String course_code, String semester) {
        String grade = "none";
        String query = "SELECT grade FROM grade_entry WHERE student_entry_number = '" + student_entry_no + "' AND course_code = '" + course_code + "' AND semester = '" + semester + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                grade = rs.getString("grade");
            }
        } catch (SQLException e) {
            grade = "Error";
            OutputHandler.logError("Error in view grade course: " + e.getMessage());
        }
        return grade;
    }

    //    view All grade
    public static GradeDTO[] view_all_grade(String student_entry_no) {
        String query = "SELECT * FROM grade_entry WHERE student_entry_number = '" + student_entry_no + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            ArrayList<GradeDTO> grades = new ArrayList<>();
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = Course_catalog.getCourseName(courseCode);
                String grade = rs.getString("grade");
                String semester = rs.getString("semester");
                grades.add(new GradeDTO(courseCode, courseName, grade, semester));
            }
            return grades.toArray(new GradeDTO[0]);
        } catch (SQLException e) {
            GradeDTO[] gradeDTOS = new GradeDTO[1];
            gradeDTOS[0] = new GradeDTO("Error", e.getMessage(), "Error", "Error");
            OutputHandler.logError("Error in view all grade: " + e.getMessage());
            return gradeDTOS;
        }
    }

//    get current semester
    public static String get_current_semester() {
        String query = "SELECT * FROM semester where is_current_semester = true";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            if (rs.next()) {
                return rs.getString("semester");
            }
        } catch (SQLException e) {
            OutputHandler.logError("Error in get current semester: " + e.getMessage());
        }
        return "Error";
    }

    // offer course
    public static String offer_course(String course_code, String instructor_id, String semester , Float CGPA_cutoff) {
        String query = "INSERT INTO course_offerings VALUES ('" + course_code + "', '" + semester + "', '" + instructor_id + "', '" + CGPA_cutoff + "')";
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
            return "Course offered successfully";
        } catch (SQLException e) {
            OutputHandler.logError("Error in offer course: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
            return "Something went wrong (Make sure the course code is valid or the course is not already offered in the semester)";
        }
    }

    // get courses offered in a semester by instructor
    public static Course_Offerings_DTO[] get_courses_offered_by_instructor(String instructor_id, String semester) {
        String query = "SELECT * FROM course_offerings WHERE instructor_id = '" + instructor_id + "' AND semester = '" + semester + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            ArrayList<Course_Offerings_DTO> courseOfferings = new ArrayList<>();
            while (rs.next()) {
                String courseCode = rs.getString("course_code");
                String courseName = Course_catalog.getCourseName(courseCode);
                String creditStructure = Course_catalog.getCreditStructure(courseCode);
                String[] prerequisites = Course_catalog.getCoursePrerequisites(courseCode);

                String instructorName = InstructorDAL.getName(instructor_id);
                String CGPA_cutoff = rs.getString("cgpa_constraint");
                
                courseOfferings.add(new Course_Offerings_DTO(courseCode, courseName, instructorName, prerequisites, creditStructure, CGPA_cutoff));
            }
            return courseOfferings.toArray(new Course_Offerings_DTO[0]);
        } catch (SQLException e) {
            OutputHandler.logError("Error in get courses offered by instructor: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
            Course_Offerings_DTO[] courseOfferingsDTOS = new Course_Offerings_DTO[1];
            courseOfferingsDTOS[0] = new Course_Offerings_DTO("Error", e.getMessage(), "Error", new String[0], "Error", "Error");
            return courseOfferingsDTOS;
        }

    }

    // get students data for giving grade to a course
    public static GiveGradeDTO[] get_students_data(String courseCode, String currentSemester) {
        String query = "SELECT * FROM student_course_registration WHERE course_code = '" + courseCode + "' AND semester = '" + currentSemester + "'";
        try {
            Connection conn = Connector.getConnection();
            ResultSet rs = conn.createStatement().executeQuery(query);
            ArrayList<GiveGradeDTO> giveGradeDTOS = new ArrayList<>();
            while (rs.next()) {
                String studentEntryNo = rs.getString("student_entry_number");
                String studentName = StudentDAL.getName(studentEntryNo);

                // check if grade is already given
                String grade = view_grade_course(studentEntryNo, courseCode, currentSemester);
                // OutputHandler.logError("Grade: " + grade);
                // if (!grade.equals("NA")) {
                //     continue;
                // }
                
                giveGradeDTOS.add(new GiveGradeDTO(studentEntryNo, studentName, courseCode, currentSemester, grade));
            }
            return giveGradeDTOS.toArray(new GiveGradeDTO[0]);
        } catch (SQLException e) {
            OutputHandler.logError("Error in get students data: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
            GiveGradeDTO[] giveGradeDTOS = new GiveGradeDTO[1];
            giveGradeDTOS[0] = new GiveGradeDTO("Error", e.getMessage(), "Error", "Error", "Error");
            return giveGradeDTOS;
        }
    }

    // give grade to a student for a course in a semester (grade_entry)
    public static String give_grade(String studentID, String courseCode, String semester, String grade) {
        String query = "INSERT INTO grade_entry VALUES ('" + studentID + "', '" + courseCode + "', '" + semester + "', '" + grade + "')";
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
            return "Grade given successfully";
        } catch (SQLException e) {
            OutputHandler.logError("Error in give grade: " + e.getMessage());
            OutputHandler.logError("Query: " + query);
            return "Error : Something went wrong (Make sure the student is registered for the course in the semester)";
        }
    }

}
