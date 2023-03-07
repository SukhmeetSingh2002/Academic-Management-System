package org.academic.User;

import org.academic.Authentication.Authenticator;
import org.academic.Authentication.Session;
import org.academic.Database.Course_CatalogDTO;
import org.academic.Database.Course_Offerings_DTO;
import org.academic.Database.GiveGradeDTO;
import org.academic.Database.GradeDTO;
import org.academic.Services.Course_Offerings;
import org.academic.Services.Course_catalog;
import org.academic.cli.OutputHandler;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class FacultyTest {
    private Faculty faculty;

    @BeforeEach
    void setUp() {
//        login as faculty
        try {
            Authenticator.authenticate("vg","132");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        faculty = new Faculty("vg","132", Session.getInstance().getFacultyID());
    }

    @AfterEach
    void tearDown() throws SQLException {
        Authenticator.logout();
    }

    @Test
    void getUserName() {
        assertEquals("vg", faculty.getUserName());
    }

    @Test
    void getPassword() {
        assertEquals("132", faculty.getPassword());
    }

    @Test
    void getOptions() {
        assertArrayEquals(new String[]{"Add a course offering", "Give grades to students", "View course offering" , "Edit Profile", "Logout"}, faculty.getOptions());
    }

    @Test
    void updateProfile() {
//        TODO: implement
    }

    @Test
    void addCourseSuccess() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = org.mockito.Mockito.mockStatic(Course_Offerings.class);

        String expected = "Course offered successfully";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.offer_course("CSE 101", Session.getInstance().getFacultyID(), "EVEN 2021" , 7F))
                .thenReturn(expected);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");

        String actual = faculty.addCourse("CSE 101", 7F);
        assertEquals(expected, actual);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.offer_course("CSE 101", Session.getInstance().getFacultyID(), "EVEN 2021" , 7F));
        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester);
        courseOfferingsMockedStatic.close();
    }

//    course offering failed
    @Test
    void addCourseFailure() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = org.mockito.Mockito.mockStatic(Course_Offerings.class);

        String expected = "Something went wrong (Make sure the course code is valid or the course is not already offered in the semester)";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.offer_course("CSE 101", Session.getInstance().getFacultyID(), "EVEN 2021" , 7F))
                .thenReturn(expected);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");

        String actual = faculty.addCourse("CSE 101", 7F);
        assertEquals(expected, actual);


        courseOfferingsMockedStatic.verify(() -> Course_Offerings.offer_course("CSE 101", Session.getInstance().getFacultyID(), "EVEN 2021" , 7F));
        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester);
        courseOfferingsMockedStatic.close();
    }

//    not authenticated
    @Test
    void addCourseNotAuthenticated() throws SQLException {
        Authenticator.logout();
        String expected = "You are not authorized to perform this operation";
        String actual = faculty.addCourse("CSE 101", 7F);
        assertEquals(expected, actual);
    }


    @Test
    void getCoursesSuccess() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = org.mockito.Mockito.mockStatic(Course_Offerings.class);

        Course_Offerings_DTO[] expected = new Course_Offerings_DTO[1];
        expected[0] = new Course_Offerings_DTO("CSE 101", "Introduction to Computer Science", "vg", new String[]{"MAT 101"}, "3-4-1-2", "7.0");

        courseOfferingsMockedStatic.when(() -> Course_Offerings.get_courses_offered_by_instructor(Session.getInstance().getFacultyID(), "EVEN 2021"))
                .thenReturn(expected);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");

        Course_Offerings_DTO[] actual = faculty.getCourses();
        assertEquals(expected, actual);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.get_courses_offered_by_instructor(Session.getInstance().getFacultyID(), "EVEN 2021"));
        courseOfferingsMockedStatic.verify(Course_Offerings::get_current_semester);
        courseOfferingsMockedStatic.close();
    }

//    not authenticated
    @Test
    void getCoursesNotAuthenticated() throws SQLException {
        Authenticator.logout();
        Course_Offerings_DTO[] expected = new Course_Offerings_DTO[1];
        expected[0] =  new Course_Offerings_DTO("Error", "You are not authorized to view the courses offered", "Error", new String[]{"Error"}, "Error","Error");
        Course_Offerings_DTO[] actual = faculty.getCourses();
        assertArrayEquals(expected, actual);
    }

    @Test
    void downloadDataSuccess() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = org.mockito.Mockito.mockStatic(Course_Offerings.class);

        GiveGradeDTO[] expected = new GiveGradeDTO[1];
        expected[0] = new GiveGradeDTO("2020csb1129","Sukhmeet Singh","CS101","EVEN 2021","A");

        courseOfferingsMockedStatic.when(() -> Course_Offerings.get_students_data("CSE 101", "EVEN 2021"))
                .thenReturn(expected);
        courseOfferingsMockedStatic.when(Course_Offerings::get_current_semester).thenReturn("EVEN 2021");

        GiveGradeDTO[] actual = faculty.downloadData("CSE 101");
        assertArrayEquals(expected, actual);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.get_students_data("CSE 101", "EVEN 2021"));
        courseOfferingsMockedStatic.close();
    }

//    not authenticated
    @Test
    void downloadDataNotAuthenticated() throws SQLException {
        Authenticator.logout();
        GiveGradeDTO[] expected = new GiveGradeDTO[1];
        expected[0] = new GiveGradeDTO("Error", "You are not authorized to download the data", "Error", "Error", "Error");
        GiveGradeDTO[] actual = faculty.downloadData("CSE 101");
        assertArrayEquals(expected, actual);
    }


    @Test
    void editAndUploadDataSuccess() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = org.mockito.Mockito.mockStatic(Course_Offerings.class);
        String[] inputDataCSV = new String[2];
        inputDataCSV[0] = "Student ID, Student name, Course ID, Semester, Grade";
        inputDataCSV[1] = "2020csb1129,Sukhmeet Singh,CS101,EVEN 2021,A";
        String expected = "Data uploaded successfully";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.give_grade("2020csb1129","CS101","EVEN 2021","A"))
                .thenReturn("Grade given successfully");

        String actual = faculty.editAndUploadData(inputDataCSV);
        assertEquals(expected, actual);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.give_grade("2020csb1129","CS101","EVEN 2021","A"));
        courseOfferingsMockedStatic.close();


    }

//    not authenticated
    @Test
    void editAndUploadDataNotAuthenticated() throws SQLException {
        Authenticator.logout();
        String[] inputDataCSV = new String[2];
        inputDataCSV[0] = "Student ID, Student name, Course ID, Semester, Grade";
        inputDataCSV[1] = "2020csb1129,Sukhmeet Singh,CS101,EVEN 2021,A";
        String expected = "You are not authorized to upload the data";
        String actual = faculty.editAndUploadData(inputDataCSV);
        assertEquals(expected, actual);
    }

//    Failure
    @Test
    void editAndUploadDataFailure() {
        MockedStatic<Course_Offerings> courseOfferingsMockedStatic = org.mockito.Mockito.mockStatic(Course_Offerings.class);
        String[] inputDataCSV = new String[2];
        inputDataCSV[0] = "Student ID, Student name, Course ID, Semester, Grade";
        inputDataCSV[1] = "2020csb1129,Sukhmeet Singh,CS101,EVEN 2021,A";

        String expected = "Error in uploading the data for student with ID: 2020csb1129 for course: CS101 in semester: EVEN 2021 with grade: A. Please try again\n" + "Error : Something went wrong (Make sure the student is registered for the course in the semester)";

        courseOfferingsMockedStatic.when(() -> Course_Offerings.give_grade("2020csb1129","CS101","EVEN 2021","A"))
                .thenReturn("Error : Something went wrong (Make sure the student is registered for the course in the semester)");

        String actual = faculty.editAndUploadData(inputDataCSV);
        assertEquals(expected, actual);

        courseOfferingsMockedStatic.verify(() -> Course_Offerings.give_grade("2020csb1129","CS101","EVEN 2021","A"));
        courseOfferingsMockedStatic.close();

    }

    @Test
    void viewCoursesCatalogSuccess() {
        MockedStatic<Course_catalog> courseCatalogMockedStatic = org.mockito.Mockito.mockStatic(Course_catalog.class);

        Course_CatalogDTO[] expected = new Course_CatalogDTO[1];
        expected[0] = new Course_CatalogDTO("CSE 101", "Introduction to Computer Science", new String[]{"MAT 101"}, "3-4-1-2");

        courseCatalogMockedStatic.when(Course_catalog::get_courses).thenReturn(expected);

        Course_CatalogDTO[] actual = faculty.viewCoursesCatalog();
        assertArrayEquals(expected, actual);

        courseCatalogMockedStatic.verify(Course_catalog::get_courses);
        courseCatalogMockedStatic.close();
    }

//    not authenticated
    @Test
    void viewCoursesCatalogNotAuthenticated() throws SQLException {
        Authenticator.logout();
        Course_CatalogDTO[] expected = new Course_CatalogDTO[1];
        expected[0] = new Course_CatalogDTO("Error", "You are not authorized to view the course catalog", new String[]{"Error"}, "Error");
        Course_CatalogDTO[] actual = faculty.viewCoursesCatalog();
        assertArrayEquals(expected, actual);
    }

}