package org.academic.Services;

import org.academic.Database.Course_CatalogDTO;
import org.academic.cli.OutputHandler;
import org.junit.jupiter.api.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CourseCatalogTest {
    private static final String CourseCode= "CS700";
    private static final String CourseName= "Software Engineering";
    private static final String CreditStructure= "3-0-0-5";
    private static final String PrerequisiteCourseCode= "HS101: A, HS201: A-";
    private static final String PrerequisiteCourseCode1= "HS101";
    private static final String PrerequisiteCourseCode2= "HS201";


    @BeforeAll
    static void setUp() {
        Course_catalog.addCourse(PrerequisiteCourseCode1, PrerequisiteCourseCode1, "3-0-0-2", "");
        Course_catalog.addCourse(PrerequisiteCourseCode2, PrerequisiteCourseCode2, "3-0-0-3", "");
        Course_catalog.addCourse(CourseName, CourseCode, CreditStructure, PrerequisiteCourseCode);
    }

    @AfterAll
    static void tearDown() {
        Course_catalog.deleteCourseFromPrerequisites(CourseCode);
        Course_catalog.deleteCourse(CourseCode);
        Course_catalog.deleteCourse(PrerequisiteCourseCode1);
        Course_catalog.deleteCourse(PrerequisiteCourseCode2);
    }

    @Test
    @Order(7)
    void getCourseName() {
        String courseName = Course_catalog.getCourseName(CourseCode);
        String courseName2 = Course_catalog.getCourseName(PrerequisiteCourseCode1);
        String courseName3 = Course_catalog.getCourseName("XX701");
        assertAll(
                () -> assertEquals(CourseName, courseName),
                () -> assertEquals(PrerequisiteCourseCode1, courseName2),
                () -> assertNull(courseName3)
        );
    }

    @Test
    @Order(6)
    void getCoursePrerequisites() {
//        using CourseCode and PrerequisiteCourseCode1 and PrerequisiteCourseCode2
        String[] coursePrerequisites = Course_catalog.getCoursePrerequisites(CourseCode);
        String[] coursePrerequisites2 = Course_catalog.getCoursePrerequisites(PrerequisiteCourseCode1);
        assertAll(
                () -> assertEquals(2, coursePrerequisites.length),
                () -> assertEquals(PrerequisiteCourseCode1, coursePrerequisites[0]),
                () -> assertEquals(PrerequisiteCourseCode2, coursePrerequisites[1]),
                () -> assertEquals(0, coursePrerequisites2.length)
        );
    }

    @Test
    @Order(5)
    void getCreditStructure() {
        String course_code = "CS700";
        String credit_structure = Course_catalog.getCreditStructure(course_code);
        assertEquals("3-0-0-5", credit_structure);

        String course_code2 = "CS701";
        String credit_structure2 = Course_catalog.getCreditStructure(course_code2);
        assertNull(credit_structure2);
    }

    @Test
    @Order(4)
    void get_courses() {
        Course_CatalogDTO[] courses = Course_catalog.get_courses();


//        it must contain the course cs700
        boolean found = Arrays.stream(courses).anyMatch(course -> course.course_code().equals("CS700"));

//        it must not contain the course cs701
        boolean found2 =Arrays.stream(courses).anyMatch(course -> course.course_code().equals("CS701"));

        assertAll(
                () -> assertTrue(courses.length > 0),
                () -> assertTrue(found),
                () -> assertFalse(found2)
        );

    }

    @Test
    @Order(1)
    void addCourse() {
        String course_code = "CS701";
        String course_name = "Software Engineering Testing";
        String credit_structure = "3-0-0-5";
        String prerequisite_course_code = "";

        boolean result = Course_catalog.addCourse(course_name, course_code, credit_structure, prerequisite_course_code);

        String course_code2 = "CS702";
        String course_name2 = "Software Engineering Testing 2";
        String credit_structure2 = "3-0-0-4.5";
        String prerequisite_course_code2 = "CS701: A";

        boolean result2 = Course_catalog.addCourse(course_name2, course_code2, credit_structure2, prerequisite_course_code2);

        assertAll(
                () -> assertTrue(result),
                () -> assertTrue(result2)
        );
    }
    @Test
    @Order(2)
    void deleteCourse() {
        String course_code = "CS701";
        String course_code2 = "CS702";

        Course_catalog.deleteCourseFromPrerequisites(course_code2);

        Course_catalog.deleteCourse(course_code2);
        Course_catalog.deleteCourse(course_code);
    }

    @Test
    @Order(3)
    void isCourseExists() {
        String course_code = "CS700";
        String course_code2 = "CS702";

        boolean result = Course_catalog.isCourseExists(course_code);
        boolean result2 = Course_catalog.isCourseExists(course_code2);

        assertAll(
                () -> assertTrue(result),
                () -> assertFalse(result2)
        );
    }


}