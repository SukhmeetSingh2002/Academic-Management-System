package org.academic.Services;

import org.academic.CourseType;
import org.academic.Database.Connector;
import org.academic.Database.UgCurriculumDTO;
import org.academic.cli.OutputHandler;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CurriculumDALTest {

    private static final String batch = "1020";
    private static final String course_code = "CS101";
    @AfterAll
    static void cleanUp() {
        deleteCourseFromUGCurriculum(course_code, batch);
        deleteBatch(batch);
    }

    private static void deleteBatch(String batch) {
        String query = "DELETE FROM batch WHERE batchyear = '%s'".formatted(batch);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            OutputHandler.logError("Error while deleting batch: " + e.getMessage());
        }
    }

    private static void deleteCourseFromUGCurriculum(String courseCode, String batch) {
        String query = "DELETE FROM ug_curriculum WHERE course_code = '%s' AND batch = '%s'".formatted(courseCode, batch);
        try {
            Connection conn = Connector.getConnection();
            conn.createStatement().executeUpdate(query);
        } catch (SQLException e) {
            OutputHandler.logError("Error while deleting course from ug curriculum: " + e.getMessage());
        }
    }

    @Test
    void getCoursesInUGCurriculum() {
        UgCurriculumDTO[] coursesInUGCurriculum = CurriculumDAL.getCoursesInUGCurriculum(batch);
        boolean containsCS101 = false;
        for (UgCurriculumDTO ugCurriculumDTO : coursesInUGCurriculum) {
            if (ugCurriculumDTO.courseCode().equals(course_code)) {
                containsCS101 = true;
                break;
            }
        }
        assertTrue(containsCS101);
    }

    @Test
    @Order(2)
    void addCourseToUGCurriculum() {
        boolean result = CurriculumDAL.addCourseToUGCurriculum(course_code, batch, CourseType.CORE);
        assertTrue(result);
    }

    @Test
    @Order(1)
    void addBatch() {
        boolean result = CurriculumDAL.addBatch(batch,120,10);
        assertTrue(result);
    }
}