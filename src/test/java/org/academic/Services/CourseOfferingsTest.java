package org.academic.Services;

import org.academic.Database.*;
import org.academic.cli.OutputHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseOfferingsTest {
    private static final String semester = "EVEN 2021";

//    insert into course_offerings values('2020csb1129','CS101', 'EVEN 2021', 'A-');
    @AfterAll
    static void cleanUp() {
        deleteGrade("2020csb1129", "MA302", semester);
        deleteCourseOffering("MA302", semester);
    }

    private static void deleteGrade(String s, String cs101, String semester) {
        String query = "DELETE FROM grade_entry WHERE student_entry_number = '%s' AND course_code = '%s' AND semester = '%s'".formatted(s, cs101, semester);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            OutputHandler.logError("Error while deleting grade: " + e.getMessage());
        }
    }

    private static void deleteCourseOffering(String ma302, String semester) {
        String query = "DELETE FROM course_offerings WHERE course_code = '%s' AND semester = '%s' and instructor_id = '1'".formatted(ma302, semester);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            OutputHandler.logError("Error while deleting course offering: " + e.getMessage());
        }
    }

    @Test
    @org.junit.jupiter.api.Order(2)
    void view_course_offerings() {
        Course_Offerings_DTO[] viewCourseOfferings = Course_Offerings.view_course_offerings(semester);
        boolean containsMA302 = false;
        for (Course_Offerings_DTO courseOfferingsDTO : viewCourseOfferings) {
            if (courseOfferingsDTO.course_code().equals("MA302")) {
                containsMA302 = true;
                break;
            }
        }
        assertTrue(containsMA302);

    }

    @Test
    void view_course_registered() {
        CourseRegisterDTO[] viewCourseRegistered = Course_Offerings.view_course_registered("2020csb1129");
        assertEquals(1, viewCourseRegistered.length);
    }

    @Test
    @org.junit.jupiter.api.Order(7)
    void view_grade_course() {
        String viewGradeCourse = Course_Offerings.view_grade_course("2020csb1129", "MA302", semester);
        boolean contains = false;
        if (viewGradeCourse.equals("A")) {
            contains = true;
        }
        assertTrue(contains);
    }

    @Test
    @org.junit.jupiter.api.Order(6)
    void view_all_grade() {
        GradeDTO[] viewAllGrade = Course_Offerings.view_all_grade("2020csb1129");
        boolean contains = false;
        for (GradeDTO grade : viewAllGrade) {
            if (grade.course_code().equals("CS101")) {
                contains = true;
                break;
            }
        }
        assertTrue(contains);
    }

    @Test
    void get_current_semester() {
        String getCurrentSemester = Course_Offerings.get_current_semester();
        assertEquals("EVEN 2021", getCurrentSemester);
    }

    @Test
    @org.junit.jupiter.api.Order(1)
    void offer_course() {
        String offerCourse = Course_Offerings.offer_course("MA302", "1", semester, 7F);
        assertEquals("Course offered successfully", offerCourse);
    }

    @Test
    @org.junit.jupiter.api.Order(3)
    void get_courses_offered_by_instructor() {
        Course_Offerings_DTO[] getCoursesOfferedByInstructor = Course_Offerings.get_courses_offered_by_instructor("1", semester);
//        it must contain MA302
        boolean contains = false;
        for (Course_Offerings_DTO course : getCoursesOfferedByInstructor) {
            if (course.course_code().contains("MA302")) {
                contains = true;
                break;
            }
        }
        assertTrue(contains);
    }

    @Test
    @org.junit.jupiter.api.Order(5)
    void get_students_data() {
        GiveGradeDTO[] getStudentsData = Course_Offerings.get_students_data("MA302", semester);
        boolean contains = false;
        for (GiveGradeDTO student : getStudentsData) {
            if (student.studentID().equals("2020csb1129")) {
                contains = true;
                break;
            }
        }
        assertFalse(contains);
    }

    @Test
    @org.junit.jupiter.api.Order(4)
    void give_grade() {
        String giveGrade = Course_Offerings.give_grade("2020csb1129", "MA302", semester, "A");
        assertEquals("Grade given successfully", giveGrade);
    }
}