package org.academic.User;

import org.academic.Authentication.Authenticator;
import org.academic.Database.CourseRegisterDTO;
import org.academic.Database.Course_Offerings_DTO;
import org.academic.Database.GradeDTO;
import org.academic.Services.Course_Offerings;
import org.academic.Services.Course_catalog;
import org.academic.Services.StudentDAL;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StudentTest {

    private static Student student;
//    set session before running this test
    @BeforeEach
    void before(){
        try {
            Authenticator.authenticate("2020csb1129","123");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        student = new Student("2020csb1129", "123", "2020csb1129");
    }

    @AfterAll
    static void after() throws SQLException {
        Authenticator.logout();
    }
    @Test
    void viewGrades() {
//        use mockito to mock the database
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        GradeDTO[] expectedGrades = new GradeDTO[1];
        expectedGrades[0] = new GradeDTO("CS101", "Introduction to Computer Science", "A", "EVEN 2021");

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_all_grade("2020csb1129")).thenReturn(expectedGrades);
        GradeDTO[] grades = student.viewGrades();
        assertAll(
                () -> assertEquals(1, grades.length),
                () -> assertEquals(expectedGrades[0], grades[0])
        );

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_all_grade("2020csb1129"));
        courseOfferingsMockedStatic.close();
    }

    @Test
    void viewGradeUnauthenticated() throws SQLException {
        Authenticator.logout();
        GradeDTO[] expectedGrades = new GradeDTO[1];
        expectedGrades[0] = new GradeDTO("Error", "You are not authorized to view the grades", "Error", "Error");
        GradeDTO[] grades = student.viewGrades();
        assertAll(
                () -> assertEquals(1, grades.length),
                () -> assertEquals(expectedGrades[0], grades[0])
        );

    }

    @Test
    void viewCoursesRegistered() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        CourseRegisterDTO[] expectedCourses = new CourseRegisterDTO[1];
        expectedCourses[0] = new CourseRegisterDTO("CS101", "Introduction to Computer Science", "ONGOING", "A", "CORE", "EVEN 2021", "3-1-0-4");

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(expectedCourses);

//        test the method
        CourseRegisterDTO[] courses = student.viewCoursesRegistered();
        assertAll(
                () -> assertEquals(1, courses.length),
                () -> assertEquals(expectedCourses[0], courses[0])
        );

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.close();

    }

    @Test
    void viewCoursesRegisteredUnauthenticated() throws SQLException {
        Authenticator.logout();
        CourseRegisterDTO[] expectedCourses = new CourseRegisterDTO[1];
        expectedCourses[0] = new CourseRegisterDTO("Error", "You are not authorized to view the courses registered", "Error", "Error", "Error", "Error", "Error");

        CourseRegisterDTO[] courses = student.viewCoursesRegistered();
        assertAll(
                () -> assertEquals(1, courses.length),
                () -> assertEquals(expectedCourses[0], courses[0])
        );

    }


    @Test
    void getUserName() {
        assertAll(
                () -> assertEquals("2020csb1129", student.getUserName()),
                () -> assertNotEquals("2020csb1128", student.getUserName())
        );
    }

    @Test
    void getPassword() {
        assertAll(
                () -> assertEquals("123", student.getPassword()),
                () -> assertNotEquals("1234", student.getPassword())
        );
    }

    @Test
    void testGetOptions() {
        String[] expectedOptions = new String[]{"Enroll in a course", "Drop a course", "View prerequisites", "View courses offered", "View courses registered", "View grades", "Edit profile", "Logout"};
        String[] actualOptions = student.getOptions();
        assertEquals(Arrays.toString(expectedOptions), Arrays.toString(actualOptions));
    }


    @Test
    void testEnrollInCourseNotOffered() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        String expectedMessage = "The course is not offered in the current semester";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        String actualMessage = student.enrollInCourse("CS103");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.close();


    }

//    enroll already registered course
    @Test
    void testEnrollInCourseAlreadyRegistered() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[1];
        registeredCourses[0] = new CourseRegisterDTO("CS101", "Introduction to Computer Science", "ONGOING", "A", "CORE", "EVEN 2021", "3-1-0-4");

        String expectedMessage = "You have already registered for the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);

        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.close();
    }

//    enroll in course with already passed course in previous semester
    @Test
    void testEnrollInCourseWithPassedCourse() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[1];
        registeredCourses[0] = new CourseRegisterDTO("CS102", "Introduction to Computer Science", "PASSED", "A", "CORE", "EVEN 2021", "3-1-0-4");

        GradeDTO[] grades = new GradeDTO[1];
        grades[0] = new GradeDTO("CS101", "Introduction to Computer Science", "A", "ODD 2020");

        String expectedMessage = "You have already passed the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_all_grade("2020csb1129")).thenReturn(grades);

        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.close();
    }

//    enroll in course with failed course in previous semester
    @Test
    void testEnrollInCourseWithFailedCourseSuccess() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        MockedStatic<Course_catalog> courseCatalogMockedStatic = mockStatic(Course_catalog.class);
        MockedStatic<StudentDAL> studentDALMockedStatic = mockStatic(StudentDAL.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[1];
        registeredCourses[0] = new CourseRegisterDTO("CS102", "Introduction to Computer Science", "ONGOING", "A", "CORE", "EVEN 2021", "3-1-0-4");


        GradeDTO[] grades = new GradeDTO[2];
        grades[0] = new GradeDTO("CS101", "Introduction to Computer Science", "F", "ODD 2020");
        grades[1] = new GradeDTO("CS102", "Introduction to Computer Science", "A", "ODD 2020");

        String expectedMessage = "You have successfully enrolled in the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_all_grade("2020csb1129")).thenReturn(grades);

        courseCatalogMockedStatic.when(() -> Course_catalog.getCoursePrerequisites("CS101")).thenReturn(new String[]{});
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS101")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS102")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS103")).thenReturn("3-1-0-4");


        studentDALMockedStatic.when(() -> StudentDAL.registerForCourse("2020csb1129", "CS101", "EVEN 2021")).thenReturn(true);

        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_all_grade("2020csb1129"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() -> Course_catalog.getCoursePrerequisites("CS101"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS101"), atLeastOnce());
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS102"), atLeastOnce());
        courseCatalogMockedStatic.close();

        studentDALMockedStatic.verify(() -> StudentDAL.registerForCourse("2020csb1129", "CS101", "EVEN 2021"));
        studentDALMockedStatic.close();

    }

//    not passed prerequisites
    @Test
    void testEnrollInCourseNotPassedPrerequisites() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        MockedStatic<Course_catalog> courseCatalogMockedStatic = mockStatic(Course_catalog.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[1];
        registeredCourses[0] = new CourseRegisterDTO("CS202", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2020", "3-1-0-4");

        GradeDTO[] grades = new GradeDTO[3];
        grades[0] = new GradeDTO("CS102", "Introduction to Computer Science", "F", "ODD 2020");
        grades[1] = new GradeDTO("CS103", "Introduction to Computer Science", "A", "ODD 2020");
        grades[2] = new GradeDTO("CS104", "Introduction to Computer Science", "A", "ODD 2020");

        String expectedMessage = "You have not passed the prerequisites for the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_all_grade("2020csb1129")).thenReturn(grades);

        courseCatalogMockedStatic.when(() -> Course_catalog.getCoursePrerequisites("CS101")).thenReturn(new String[]{"CS102"});
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS101")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS102")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS103")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS104")).thenReturn("3-1-0-4");


        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_all_grade("2020csb1129"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() -> Course_catalog.getCoursePrerequisites("CS101"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS101"), atLeastOnce());
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS102"), atLeastOnce());
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS103"), atLeastOnce());
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS104"), atLeastOnce());
        courseCatalogMockedStatic.close();

    }

//    courses in previo To previos semester
    @Test
    void testEnrollInCourseCoursesInPreviousSemester() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        MockedStatic<Course_catalog> courseCatalogMockedStatic = mockStatic(Course_catalog.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[1];
        registeredCourses[0] = new CourseRegisterDTO("CS202", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2020", "3-1-0-4");

        GradeDTO[] grades = new GradeDTO[3];
        grades[0] = new GradeDTO("CS102", "Introduction to Computer Science", "F", "EVEN 2020");
        grades[1] = new GradeDTO("CS103", "Introduction to Computer Science", "A", "EVEN 2020");
        grades[2] = new GradeDTO("CS104", "Introduction to Computer Science", "A", "EVEN 2020");

        String expectedMessage = "You have not passed the prerequisites for the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_all_grade("2020csb1129")).thenReturn(grades);

        courseCatalogMockedStatic.when(() -> Course_catalog.getCoursePrerequisites("CS101")).thenReturn(new String[]{"CS102"});
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS101")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS102")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS103")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS104")).thenReturn("3-1-0-4");


        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester, atLeastOnce());
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_all_grade("2020csb1129"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() -> Course_catalog.getCoursePrerequisites("CS101"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS101"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS102"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS103"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS104"));
        courseCatalogMockedStatic.close();
    }

//    passed all prerequisites
    @Test
    void testEnrollInCoursePassedAllPrerequisites() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        MockedStatic<Course_catalog> courseCatalogMockedStatic = mockStatic(Course_catalog.class);
        MockedStatic<StudentDAL> studentDALMockedStatic = mockStatic(StudentDAL.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[1];
        registeredCourses[0] = new CourseRegisterDTO("CS102", "Introduction to Computer Science", "ONGOING", "A", "CORE", "EVEN 2021", "3-1-0-4");


        GradeDTO[] grades = new GradeDTO[2];
        grades[0] = new GradeDTO("CS101", "Introduction to Computer Science", "F", "ODD 2020");
        grades[1] = new GradeDTO("CS102", "Introduction to Computer Science", "A", "ODD 2020");

        String expectedMessage = "You have successfully enrolled in the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_all_grade("2020csb1129")).thenReturn(grades);

        courseCatalogMockedStatic.when(() -> Course_catalog.getCoursePrerequisites("CS101")).thenReturn(new String[]{"CS102"});
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS101")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS102")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS103")).thenReturn("3-1-0-4");


        studentDALMockedStatic.when(() -> StudentDAL.registerForCourse("2020csb1129", "CS101", "EVEN 2021")).thenReturn(true);

        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_all_grade("2020csb1129"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() -> Course_catalog.getCoursePrerequisites("CS101"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS101"), atLeastOnce());
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS102"), atLeastOnce());
        courseCatalogMockedStatic.close();

        studentDALMockedStatic.verify(() -> StudentDAL.registerForCourse("2020csb1129", "CS101", "EVEN 2021"));
        studentDALMockedStatic.close();

    }


//    exceed credit limit
    @Test
    void testEnrollInCourseExceedCreditLimit() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        MockedStatic<Course_catalog> courseCatalogMockedStatic = mockStatic(Course_catalog.class);
        Course_Offerings_DTO[] offeredCourses = new Course_Offerings_DTO[1];
        offeredCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "CORE", new String[]{""}, "3-1-0-4", "6");

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[3];
        registeredCourses[0] = new CourseRegisterDTO("CS102", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");
        registeredCourses[1] = new CourseRegisterDTO("CS103", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");
        registeredCourses[2] = new CourseRegisterDTO("CS104", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");

        GradeDTO[] grades = new GradeDTO[3];
        grades[0] = new GradeDTO("HS101", "Introduction to Computer Science", "B-", "ODD 2020");
        grades[1] = new GradeDTO("HS102", "Introduction to Computer Science", "B-", "ODD 2020");
        grades[2] = new GradeDTO("HS103", "Introduction to Computer Science", "B-", "ODD 2020");


        String expectedMessage = "You are not allowed to register for the course as it will exceed the allowed credits";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings("EVEN 2021")).thenReturn(offeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_all_grade("2020csb1129")).thenReturn(grades);

        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS101")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("HS101")).thenReturn("3-1-0-4");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("HS102")).thenReturn("3-1-0-3");
        courseCatalogMockedStatic.when(() -> Course_catalog.getCreditStructure("HS103")).thenReturn("3-1-0-3");


        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings("EVEN 2021"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_all_grade("2020csb1129"));
        courseOfferingsMockedStatic.close();

        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("CS101"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("HS101"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("HS102"));
        courseCatalogMockedStatic.verify(() -> Course_catalog.getCreditStructure("HS103"));
        courseCatalogMockedStatic.close();

    }

//    enroll Unauthenticated
    @Test
    void testEnrollInCourseUnauthenticated() throws SQLException {
        Authenticator.logout();
        String expectedMessage = "You are not authorized to enroll in the course";

        String actualMessage = student.enrollInCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

    }


    @Test
    void viewPrerequisites() {
        MockedStatic< Course_catalog> courseCatalogMockedStatic = mockStatic(Course_catalog.class);
        String[] expectedPrerequisites = new String[1];
        expectedPrerequisites[0] = "CS101";

        courseCatalogMockedStatic.when(() -> Course_catalog.getCoursePrerequisites("CS102")).thenReturn(expectedPrerequisites);

        String[] prerequisites = student.viewPrerequisites("CS102");
        assertAll(
                () -> assertEquals(expectedPrerequisites.length, prerequisites.length),
                () -> assertEquals(expectedPrerequisites[0], prerequisites[0])
        );

        courseCatalogMockedStatic.verify(() -> Course_catalog.getCoursePrerequisites("CS102"));
        courseCatalogMockedStatic.close();

    }

    @Test
    void viewPrerequisitesUnauthenticated() throws SQLException {
        Authenticator.logout();
        String[] expectedPrerequisites = new String[1];
        expectedPrerequisites[0] = "You are not authorized to view the prerequisites";

        String[] prerequisites = student.viewPrerequisites("CS102");
        assertAll(
                () -> assertEquals(expectedPrerequisites.length, prerequisites.length),
                () -> assertEquals(expectedPrerequisites[0], prerequisites[0])
        );

    }

    @Test
    void dropCourse() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        MockedStatic<StudentDAL> studentDALMockedStatic = mockStatic(StudentDAL.class);

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[3];
        registeredCourses[0] = new CourseRegisterDTO("CS102", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");
        registeredCourses[1] = new CourseRegisterDTO("CS103", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");
        registeredCourses[2] = new CourseRegisterDTO("CS104", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");

        String expectedMessage = "You have successfully dropped the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");
        studentDALMockedStatic.when(() -> StudentDAL.dropCourse("2020csb1129", "CS102", "EVEN 2021")).thenReturn(true);

        String actualMessage = student.dropCourse("CS102");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester);
        studentDALMockedStatic.verify(() -> StudentDAL.dropCourse("2020csb1129", "CS102", "EVEN 2021"));
        courseOfferingsMockedStatic.close();
        studentDALMockedStatic.close();


    }

//    drop not registered course
    @Test
    void testDropCourseNotRegistered() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);

        CourseRegisterDTO[] registeredCourses = new CourseRegisterDTO[3];
        registeredCourses[0] = new CourseRegisterDTO("CS102", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");
        registeredCourses[1] = new CourseRegisterDTO("CS103", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");
        registeredCourses[2] = new CourseRegisterDTO("CS104", "Introduction to Computer Science", "ONGOING", "NA", "CORE", "EVEN 2021", "3-1-0-4");

        String expectedMessage = "You have not registered for the course";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_registered("2020csb1129")).thenReturn(registeredCourses);

        String actualMessage = student.dropCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_registered("2020csb1129"));
        courseOfferingsMockedStatic.close();
    }

//    drop unauthenticated
    @Test
    void testDropCourseUnauthenticated() throws SQLException {
        Authenticator.logout();
        String expectedMessage = "You are not authorized to drop the course";

        String actualMessage = student.dropCourse("CS101");
        assertEquals(expectedMessage, actualMessage);

    }

    @Test
    void viewCoursesOffered() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = mockStatic(Course_Offerings.class);
        Course_Offerings_DTO[] expectedCourses = new Course_Offerings_DTO[1];
        expectedCourses[0] = new Course_Offerings_DTO("CS101", "Introduction to Computer Science", "EVEN 2021", new String[]{"CS101"}, "3-1-0-4", "6");

        String semester = "EVEN 2021";
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn(semester);
        courseOfferingsMockedStatic.when(() -> Course_Offerings.view_course_offerings(semester)).thenReturn(expectedCourses);

        Course_Offerings_DTO[] actual_courses = student.viewCoursesOffered();
        assertAll(
                () -> assertEquals(expectedCourses.length, actual_courses.length),
                () -> assertEquals(expectedCourses[0], actual_courses[0])
        );

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.view_course_offerings(semester));
        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester);
        courseOfferingsMockedStatic.close();

    }

    @Test
    void viewCoursesOfferedUnauthenticated() throws SQLException {
        Authenticator.logout();
        Course_Offerings_DTO[] expectedCourses = new Course_Offerings_DTO[1];
        expectedCourses[0] = new Course_Offerings_DTO("Error", "You are not authorized to view the courses offered", "Error", new String[]{"Error"}, "Error", "Error");

        Course_Offerings_DTO[] actual_courses = student.viewCoursesOffered();
        assertAll(
                () -> assertEquals(expectedCourses.length, actual_courses.length),
                () -> assertEquals(expectedCourses[0], actual_courses[0])
        );

    }

//    update profile
    @Test
    void testUpdateProfileSuccess() {
        MockedStatic<StudentDAL> studentDALMockedStatic = mockStatic(StudentDAL.class);
        String expectedMessage = "Password updated successfully";

        studentDALMockedStatic.when(() -> StudentDAL.changePassword("2020csb1129", "123", "1234568")).thenReturn(true);

        String actualMessage = student.updateProfile("1234568");
        assertEquals(expectedMessage, actualMessage);

        studentDALMockedStatic.verify(() -> StudentDAL.changePassword("2020csb1129", "123", "1234568"));
        studentDALMockedStatic.close();
    }

//    null password
    @Test
    void testUpdateProfileNullPassword() {
        String expectedMessage = "Password is null";

        String actualMessage = student.updateProfile(null);
        assertEquals(expectedMessage, actualMessage);
    }

//    password update fail
    @Test
    void testUpdateProfileFail() {
        MockedStatic<StudentDAL> studentDALMockedStatic = mockStatic(StudentDAL.class);
        String expectedMessage = "Password update failed";

        String actualMessage = student.updateProfile("1234568");
        assertEquals(expectedMessage, actualMessage);
    }
}