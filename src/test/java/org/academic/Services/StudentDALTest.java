package org.academic.Services;

import org.academic.Database.GradeDTO;
import org.academic.cli.OutputHandler;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.mockito.MockedStatic;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class StudentDALTest {
    private static final String studentEntryNumber = "2020csb1129";
    private static final String studentName = "sukhmeet";
    private static final String studentUserName = "2020csb1129";
    private static final String studentEmail = "2020csb1129@iitrpr.ac.in";
    private static final String studentBatch = "2020";
    private static final String password = "123";


    @Test
    @Order(9)
    void changePassword() {
        boolean result = StudentDAL.changePassword(studentUserName, password, "1234");
        assertTrue(result);
        boolean result2 = StudentDAL.changePassword(studentUserName, "1234", password);
        assertTrue(result2);
    }

    @Test
    @Order(1)
    void getStudentEntryNumber() {
        assertEquals(studentEntryNumber, StudentDAL.getStudentEntryNumber(studentUserName));
    }

    @Test
    @Order(2)
    void getName() {
        assertEquals(studentName, StudentDAL.getName(studentEntryNumber));
    }

    @Test
    @Order(3)
    void getStudentsEntryNumbers() {
        ArrayList<String> studentsEntryNumbers = StudentDAL.getStudentsEntryNumbers(studentBatch);
        assertTrue(studentsEntryNumbers.contains(studentEntryNumber));
    }

    @Test
    @Order(4)
    void getCGPA() {
        MockedStatic<Course_catalog> course_catalogMockedStatic = org.mockito.Mockito.mockStatic(Course_catalog.class);
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS101")).thenReturn("1-1-4-3");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS102")).thenReturn("1-1-4-3");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS103")).thenReturn("1-1-4-3");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS104")).thenReturn("1-1-4-4");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS105")).thenReturn("1-1-4-4");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS106")).thenReturn("1-1-4-4");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS107")).thenReturn("1-1-4-4");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS108")).thenReturn("1-1-4-4");
        course_catalogMockedStatic.when(() -> Course_catalog.getCreditStructure("CS109")).thenReturn("1-1-4-4");

        GradeDTO[] grades = new GradeDTO[9];
        grades[0] = new GradeDTO("CS101", "TEST course", "A", "EVEN 2020");
        grades[1] = new GradeDTO("CS102", "TEST course", "A-", "EVEN 2020");
        grades[2] = new GradeDTO("CS103", "TEST course", "B", "EVEN 2020");
        grades[3] = new GradeDTO("CS104", "TEST course", "B-", "EVEN 2020");
        grades[4] = new GradeDTO("CS105", "TEST course", "C", "EVEN 2020");
        grades[5] = new GradeDTO("CS106", "TEST course", "C-", "EVEN 2020");
        grades[6] = new GradeDTO("CS107", "TEST course", "D", "EVEN 2020");
        grades[7] = new GradeDTO("CS108", "TEST course", "9", "EVEN 2020");
        grades[8] = new GradeDTO("CS109", "TEST course", "F", "EVEN 2020");


        assertEquals(6.212121212121212, StudentDAL.getCGPA(studentEntryNumber,grades));

        course_catalogMockedStatic.verify(() -> Course_catalog.getCreditStructure(any(String.class)), times(9));
        course_catalogMockedStatic.close();
    }

    @Test
    @Order(5)
    void getBatch() {
        assertEquals(studentBatch, StudentDAL.getBatch(studentEntryNumber));
    }

    @Test
    @Order(6)
    void getEmail() {
        assertEquals(studentEmail, StudentDAL.getEmail(studentEntryNumber));
    }

    @Test
    @Order(7)
    void registerForCourse() {
        String courseCode = "CS202";
        String semester = "EVEN 2021";
        boolean result = StudentDAL.registerForCourse(studentEntryNumber, courseCode, semester);
        assertTrue(result);
    }

    @Test
    @Order(8)
    void dropCourse() {
        String courseCode = "CS202";
        String semester = "EVEN 2021";
        boolean result = StudentDAL.dropCourse(studentEntryNumber, courseCode, semester);
        assertTrue(result);
    }
}