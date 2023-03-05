package org.academic.cli;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import org.academic.Database.GiveGradeDTO;
import org.academic.Database.GradeDTO;

public class OutputHandler {
    private static final Path errorLog = Path.of("Error.log");
    private static Path ungradedCSV = Path.of("ungraded.csv");

    private OutputHandler() {

    }

    public static void printS(String message) {
        System.out.print(message);
    }

    public static void print(String message) {
        System.out.println(message);
    }

    public static void error(String message) {
        System.out.println("Error: " + message);
    }

    public static void logError(String message) {
//        logg the error in a file opened Error.log
        try {
            Files.writeString(errorLog, "Time: " + java.time.LocalDateTime.now() + " " + message + " " + System.lineSeparator(), Files.exists(errorLog) ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE); // append to file if it exists, otherwise create it
        } catch (Exception e) {
            System.out.println("Error logging error");
        }

    }
    public static void displayMenu(String[] menuItems) {
        for (int i = 0; i < menuItems.length; i++) {
            System.out.println((i + 1) + ". " + menuItems[i]);
        }
    }

    public static void tableHeader(String[] headers, int[] columnWidths) {
        int length = headers.length;
        for (int i = 0; i < length; i++) {
            System.out.printf("%-" + columnWidths[i] + "s", headers[i]);
        }
        System.out.println();
    }

    public static void tableRow(String[] row, int[] columnWidths) {
        int length = row.length;
        for (int i = 0; i < length; i++) {
            System.out.printf("%-" + columnWidths[i] + "s", row[i]);
        }
        System.out.println();
    }

    public static void tableFooter(int[] columnWidths) {
        for (int columnWidth : columnWidths) {
            System.out.printf("%-" + columnWidth + "s", "-".repeat(columnWidth));
        }
        System.out.println();
    }

    public static void table(String[][] data, String[] headers, int[] columnWidths) {
        tableHeader(headers, columnWidths);
        tableFooter(columnWidths);
        for (String[] row : data) {
            tableRow(row, columnWidths);
        }
    }

    public static void table(String[][] data, String[] headers) {
        int[] columnWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            columnWidths[i] = headers[i].length();
        }
        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                if (row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }
        table(data, headers, columnWidths);
    }

    public static void table(String[][] data) {
        String[] headers = new String[data[0].length];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = "Column " + (i + 1);
        }
        table(data, headers);
    }

    public static void table(String[][] data, int[] columnWidths) {
        String[] headers = new String[data[0].length];
        for (int i = 0; i < headers.length; i++) {
            headers[i] = "Column " + (i + 1);
        }
        table(data, headers, columnWidths);
    }

    // write to file in csv format (comma separated values) 
    public static void writeToFile(String fileName, GiveGradeDTO[] res) {
        ungradedCSV = Path.of(fileName);
        try {
            Files.writeString(ungradedCSV, "StudentID,StudentName,CourseID,Semester,Grade" + System.lineSeparator());
            for (GiveGradeDTO giveGradeDTO : res) {
                Files.writeString(ungradedCSV, giveGradeDTO.studentID() + "," + giveGradeDTO.studentName() + "," + giveGradeDTO.courseID() + "," + giveGradeDTO.semester() + "," + giveGradeDTO.grade() + System.lineSeparator(), Files.exists(ungradedCSV) ? java.nio.file.StandardOpenOption.APPEND : java.nio.file.StandardOpenOption.CREATE);
            }
        } catch (Exception e) {
            System.out.println("Error writing to file");
            OutputHandler.logError("Error writing to file"+e.getMessage());
        }
    }

    public static boolean generateTranscript(String studentEntryNumber, String studentName, String batch, GradeDTO[] studentGrades, double[] allCourseCredits, double totalCredits, double cgpa) {
        
        // Check if the required parameters are not null or empty
        if (studentEntryNumber == null || studentEntryNumber.isEmpty() ||
            studentName == null || studentName.isEmpty() ||
            batch == null || batch.isEmpty() ||
            studentGrades == null || allCourseCredits == null) {
            return false;
        }
        
        // Check if the length of studentGrades array matches the length of allCourseCredits array
        if (studentGrades.length != allCourseCredits.length) {
            return false;
        }
        
        // Check if the total credits is non-negative
        if (totalCredits < 0) {
            return false;
        }
        
        // Check if the cgpa is between 0 and 10
        if (cgpa < 0 || cgpa > 10) {
            return false;
        }
        
        // generate the transcript to a file
        String fileName = studentEntryNumber + "_" + studentName + "_" + batch + ".txt";
        
        return generateTranscriptFile(fileName, studentEntryNumber, studentName, batch, studentGrades, allCourseCredits, totalCredits, cgpa);
    }

    private static boolean generateTranscriptFile(String fileName, String studentEntryNumber, String studentName, String batch, GradeDTO[] studentGrades, double[] allCourseCredits, double totalCredits, double cgpa) {
        try {
            PrintWriter writer = new PrintWriter(fileName);
            // write student information and header
            writer.printf("Student Name: %s%n", studentName);
            writer.printf("Entry Number: %s%n", studentEntryNumber);
            writer.println();
            writer.println("┌"+"─".repeat(12) + "┬" + "─".repeat(60) + "┬" + "─".repeat(8) + "┬" + "─".repeat(8)+"┐");
            writer.printf("│%-12s│%-60s│%8s│%8s│%n", "Code", "Course Name", "Grade", "Credits");
            writer.println("├"+"─".repeat(12) + "┼" + "─".repeat(60) + "┼" + "─".repeat(8) + "┼" + "─".repeat(8)+"┤");
            
            for (int i = 0; i < studentGrades.length; i++) {
                GradeDTO grade = studentGrades[i];
                double credits = allCourseCredits[i];
                writer.printf("│%-12s│%-60s│%8s│%8.2f│%n", grade.course_code(), grade.course_name(), grade.grade(), credits);
            }
            
            writer.println("└"+"─".repeat(12) + "┴" + "─".repeat(60) + "┴" + "─".repeat(8) + "┴" + "─".repeat(8)+"┘");
            writer.printf("%-83s %8.2f%n", "Total Credits:", totalCredits);
            writer.printf("%-74s %8.2f%n", "CGPA:", cgpa);


            writer.close();
            
        } catch (FileNotFoundException e) {
            OutputHandler.logError("Error generating transcript: " + e.getMessage());
            return false;
        }
        return true;
    }



}
