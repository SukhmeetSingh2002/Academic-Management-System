package org.academic;

import org.academic.Authentication.Authenticator;
import org.academic.Authentication.Session;
import org.academic.Database.CourseRegisterDTO;
import org.academic.Database.Course_CatalogDTO;
import org.academic.Database.Course_Offerings_DTO;
import org.academic.Database.GiveGradeDTO;
import org.academic.Database.GradeDTO;
import org.academic.User.Faculty;
import org.academic.User.Student;
import org.academic.User.UserType;
import org.academic.cli.InputHandler;
import org.academic.cli.OutputHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;


public class Main {
    // TODO: add this.error to DTOs
    private static void handleStudent() {
//        TODO: set entry number
        Student student = new Student(Session.getInstance().getUserName(), Session.getInstance().getPassword(), Session.getInstance().getStudentEntryNumber());
        String[] options = student.getOptions();
        Scanner scanner = new Scanner(System.in);
        while (true) {
            int option = takeOptions(options, scanner);
            switch (option) {
                case 0, 8 -> {
                    OutputHandler.print("Logging out...");
                    try {
                        Authenticator.logout();
                        OutputHandler.print("Logged out successfully");
                    } catch (SQLException e) {
                        OutputHandler.print("Error logging out");
                        OutputHandler.logError("Error logging out: " + e.getMessage());
                    }
                    OutputHandler.print("Exiting...");
                    return;
                }
//                enroll in a course
                case 1 -> {
                    System.out.println("Enroll in a course");
                    System.out.print("Enter course code: ");
                    String courseCode = scanner.nextLine();
                    System.out.println("Course code is: " + courseCode);
                    String res = student.enrollInCourse(courseCode);
                    System.out.println(res);
                }
//                drop a course
                case 2 -> {
                    System.out.println("Drop a course");
                    System.out.print("Enter course code: ");
                    String courseCode = scanner.nextLine();
                    System.out.println("Course code is: " + courseCode);
                    String res = student.dropCourse(courseCode);
                    System.out.println(res);
                }
//                view prerequisites
                case 3 -> {
                    OutputHandler.print("View prerequisites");
                    System.out.print("Enter course code: ");
                    String courseCode = scanner.nextLine();
                    System.out.println("Course code is: " + courseCode);
                    String[] prerequisites = student.viewPrerequisites(courseCode);
                    if (prerequisites == null) {
                        OutputHandler.error("Something went wrong");
                        break;
                    }
                    if (prerequisites.length == 0) {
                        OutputHandler.print("No prerequisites found");
                        break;
                    }
                    OutputHandler.print("Prerequisites are: " + Arrays.toString(prerequisites));

                }
//                view courses offered
                case 4 -> {
                    System.out.println("View courses offered");
                    Course_Offerings_DTO[] courses = student.viewCoursesOffered();

//                    print the courses
                    if (courses == null || courses.length == 0) {
                        OutputHandler.print("No courses found");
                        break;
                    } else if (courses[0].course_code().equals("Error")) {
                        OutputHandler.error("Something went wrong");
                        break;
                    }
//                    use OutputHandler to print the courses
                    String[][] coursesTable = new String[courses.length][];
                    for (int i = 0; i < courses.length; i++) {
                        coursesTable[i] = new String[]{courses[i].course_code(), courses[i].course_name(), courses[i].course_instructor(), courses[i].credit_structure(), Arrays.toString(courses[i].course_prerequisites())};
                    }
                    OutputHandler.table(coursesTable, new String[]{"Course code", "Course name", "Instructor", "Credit", "Prerequisites"}, new int[]{12, 40, 15, 10, 15});


                }
//                view courses registered by student
                case 5 -> {
                    OutputHandler.print("View courses registered by student");
                    CourseRegisterDTO[] courses = student.viewCoursesRegistered();
                    if (courses == null || courses.length == 0) {
                        OutputHandler.print("No courses found");
                        break;
                    } else if (courses[0].course_code().equals("Error")) {
                        OutputHandler.error("Something went wrong");
                        OutputHandler.logError(courses[0].course_name());
                        break;
                    }
//                    use OutputHandler to print the courses
                    String[][] coursesTable = new String[courses.length][];
                    for (int i = 0; i < courses.length; i++) {
                        coursesTable[i] = new String[]{courses[i].course_code(), courses[i].course_name(), courses[i].status(), courses[i].grade(), courses[i].type(), courses[i].semester(), courses[i].credit_structure()};
                    }
                    OutputHandler.table(coursesTable, new String[]{"Course code", "Course name", "Status", "Grade", "Type", "Semester", "Credit"
                    }, new int[]{12, 40, 10, 10, 15, 10, 10});

                }
//                view grades
                case 6 -> {
//                    TODO: calculate GPA
                    OutputHandler.print("View grades");
                    GradeDTO[] grades = student.viewGrades();
                    if (grades == null || grades.length == 0) {
                        OutputHandler.print("No grades found");
                        break;
                    } else if (grades[0].course_code().equals("Error")) {
                        OutputHandler.error("Something went wrong");
                        OutputHandler.logError(grades[0].course_name());
                        break;
                    }
//                    use OutputHandler to print the courses
                    String[][] gradesTable = new String[grades.length][];
                    for (int i = 0; i < grades.length; i++) {
                        gradesTable[i] = new String[]{grades[i].course_code(), grades[i].course_name(), grades[i].grade(), grades[i].semester()};
                    }
                    OutputHandler.table(gradesTable, new String[]{"Course code", "Course name", "Grade", "Semester"}, new int[]{12, 40, 10, 10});


                }
//                edit profile
                case 7 -> {
//                    change password
                    OutputHandler.print("Enter old password: ");
                    String oldPassword = scanner.nextLine();
                    if (!oldPassword.equals(student.getPassword())) {
                        OutputHandler.error("Old password is incorrect");
                        break;
                    }
                    OutputHandler.print("Enter new password: ");
                    String newPassword = scanner.nextLine();
                    OutputHandler.print("Confirm new password: ");
                    String confirmPassword = scanner.nextLine();
                    if (!newPassword.equals(confirmPassword)) {
                        OutputHandler.error("Passwords do not match");
                        break;
                    }
                    String res = student.updateProfile(newPassword);
                    OutputHandler.print(res);

                }
                default -> System.out.println("Invalid option");
            }
        }
    }

    private static int takeOptions(String[] options, Scanner scanner) {
        System.out.print("\n-------------------------\n");
        System.out.println("Press enter key to continue...(q to quit)");
//        check if user want to quit

        String input = scanner.nextLine();
        if (input.equals("q")) return 0;


        OutputHandler.displayMenu(options);

        System.out.print("Enter option: ");
        while (!scanner.hasNextInt()) {
            System.out.println("Invalid option");
            System.out.print("Enter option: ");
            scanner.next();
        }
        int option = scanner.nextInt();
        scanner.nextLine(); // consume the newline
        return option;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Hello world!");
//        take input from user
        System.out.print("Enter username: ");
        String username = scanner.nextLine();
        System.out.println("Username is: " + username);
//        TODO: take password as input and don't show it on console
        System.out.print("Enter password: ");
        String password = scanner.nextLine();

//        convert to lowercase
        username = username.toLowerCase();
        password = password.toLowerCase();

        boolean isAuthenticated = false;
        try {
            isAuthenticated = Authenticator.authenticate(username, password);
        } catch (SQLException e) {
            System.out.println("Error authenticating user");
            OutputHandler.logError("Error authenticating user: "+e.getMessage());
        }

        if (isAuthenticated) {
            System.out.println("User is authenticated");
            System.out.println("User type: " + Session.getInstance().getUserType());
            System.out.println("User name: " + Session.getInstance().getUserName());
            System.out.println("User password: " + Session.getInstance().getPassword());
            if (Session.getInstance().getUserType() == UserType.STUDENT) {
                handleStudent();
            } else if (Session.getInstance().getUserType() == UserType.FACULTY) {
                System.out.println("Faculty");
                handleFaculty();
            } else if (Session.getInstance().getUserType() == UserType.ADMIN) {
                System.out.println("Admin");
            }
        } else {
            System.out.println("User is not authenticated10");
        }


    }

    private static void handleFaculty() {
        // TODO: add username colum ub student and faculty table
        Faculty faculty = new Faculty(Session.getInstance().getUserName(), Session.getInstance().getPassword(),Session.getInstance().getFacultyID());
        String[] options = faculty.getOptions();

        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            int option = takeOptions(options, scanner);
            switch (option) {
//                view courses
                case 0, 8 -> {
                    OutputHandler.print("Logging out...");
                    try {
                        Authenticator.logout();
                        OutputHandler.print("Logged out successfully");
                    } catch (SQLException e) {
                        OutputHandler.print("Error logging out");
                        OutputHandler.logError("Error logging out: " + e.getMessage());
                    }
                    OutputHandler.print("Exiting...");
                    return;
                }
                case 1 -> {
                    OutputHandler.print("Add course");
                    OutputHandler.print("Enter course code: ");
                    String courseCode = scanner.nextLine();
                    
                    OutputHandler.print("Enter min CGPA: ");
                    Float creditLimit = scanner.nextFloat();
                    
                    String res = faculty.addCourse(courseCode,creditLimit);
                    OutputHandler.print(res);
                }
                case 2 -> {
                    OutputHandler.print("Give grades");
                    // users has 3 options 
                    // 1. view his courses
                    // 2. download data
                    // 3. edit and upload data
                    String[] options2 = new String[]{"View courses", "Download data", "Edit and upload data"};
                    int option2 = takeOptions(options2, scanner);

                    switch (option2) {
                        case 1 -> {
                            OutputHandler.print("View courses");
                            Course_Offerings_DTO[] courses = faculty.getCourses();

                            if (courses.length == 0) {
                                OutputHandler.print("No courses found");
                                break;
                            } else if (courses.length == 1 && courses[0].course_code().equals("error")) {
                                OutputHandler.print("Error fetching courses");
                                OutputHandler.logError("Error fetching courses: " + courses[0].course_name());
                                break;
                            }

                            String[][] coursesTable = new String[courses.length][];
                            for (int i = 0; i < courses.length; i++) {
                                coursesTable[i] = new String[]{courses[i].course_code(), courses[i].course_name(), courses[i].course_instructor(), courses[i].credit_structure(), courses[i].CGPA_cutoff(),Arrays.toString(courses[i].course_prerequisites())};
                            }
                            OutputHandler.table(coursesTable, new String[]{"Course code", "Course name", "Course instructor", "Credits", "Min. CGPA", "Prereq"}, new int[]{12, 40, 20, 10, 10, 10});
                        }
                        case 2 -> {
                            OutputHandler.print("Download data");
                            
                            OutputHandler.print("Enter course code: ");
                            String courseCode = scanner.nextLine();

                            OutputHandler.printS("Enter file name: (Default: <course_code>_<semester>_<year>.csv)");
                            String fileName = scanner.nextLine();

                            if (fileName.equals("")) {
                                fileName = courseCode + "_" + Session.getInstance().getCurrentSemester() + ".csv";
                            }

                            GiveGradeDTO[] res = faculty.downloadData(courseCode);

                            // before writing to file, check if res is null
                            if (res == null || res.length==0) {
                                OutputHandler.print("Error downloading data: No data found");
                                break;
                            }
                            // print the data to console before writing to file
                            OutputHandler.print("Data downloaded successfully");
                            String[][] data = new String[res.length][];
                            for (int i = 0; i < res.length; i++) {
                                data[i] = new String[]{res[i].studentID(), res[i].studentName(), res[i].courseID(), res[i].semester(), res[i].grade()};
                            }
                            OutputHandler.table(data, new String[]{"Student ID", "Student name", "Course ID", "Semester", "Grade"}, new int[]{12, 20, 20, 10, 10});

                            // write to file using OutputHandler
                            OutputHandler.writeToFile(fileName, res);

                        }
                        case 3 -> {
                            OutputHandler.print("Edit and upload data");
                            OutputHandler.printS("Enter course code: ");
                            String courseCode = scanner.nextLine();

                            OutputHandler.printS("Enter file name: (Default: <course_code>_<semester>_<year>.csv)");
                            String fileName = scanner.nextLine();

                            if (fileName.equals("")) fileName = courseCode + "_" + Session.getInstance().getCurrentSemester() + ".csv";

                            // read from file inputHandler
                            String[] data = InputHandler.readCsvFile(fileName);
                            if (data == null) {
                                OutputHandler.print("Error reading file");
                                break;
                            }
                            // print the data to console before writing to file
                            // first line is the header so skip it and start from 1 
                            OutputHandler.print("Data read successfully");
                            // String[][] data2 = new String[data.length - 1][];
                            // for (int i = 1; i < data.length; i++) {
                            //     data2[i - 1] = data[i].split(",");
                            // }


                            // String[] header = data[0].split(",");
                            // OutputHandler.table(data2, header , new int[]{12, 20, 20, 10, 10});


                            // TODO: check if the data is valid
                            // for (int i = 1; i < data.length - 1; i++) {
                            //     String[] row = data[i].split(",");
                            //     if (row.length != 5) {
                            //         OutputHandler.print("Error: Invalid data format");
                            //         break;
                            //     }
                            //     // check if student id is valid
                            //     if (!row[0].matches("[0-9]{9}")) {
                            //         OutputHandler.print("Error: Invalid student ID");
                            //         break;
                            //     }
                            //     // check if course id is valid
                            //     if (!row[2].matches("[A-Z]{3}[0-9]{3}")) {
                            //         OutputHandler.print("Error: Invalid course ID");
                            //         break;
                            //     }
                            //     // check if semester is valid
                            //     if (!row[3].matches("[0-9]{4}[A-Z]{1}")) {
                            //         OutputHandler.print("Error: Invalid semester");
                            //         break;
                            //     }
                            //     // check if grade is valid
                            //     if (!row[4].matches("[A-F]{1}")) {
                            //         OutputHandler.print("Error: Invalid grade");
                            //         break;
                            //     }
                            // }
                            

                            String res = faculty.editAndUploadData(data);
                            OutputHandler.print(res);
                        }
                        default -> System.out.println("Invalid option");
                    }
                }
                case 3 -> {
                        OutputHandler.print("View courses catalog");
                        Course_CatalogDTO[] courses = faculty.viewCoursesCatalog();

                        if (courses.length == 0) {
                            OutputHandler.print("No courses found");
                            break;
                        } else if (courses.length == 1 && courses[0].course_code().equals("error")) {
                            OutputHandler.print("Error fetching courses");
                            OutputHandler.logError("Error fetching courses: " + courses[0].course_name());
                            break;
                        }

                        String[][] coursesTable = new String[courses.length][];
                        for (int i = 0; i < courses.length; i++) {
                            coursesTable[i] = new String[]{courses[i].course_code(), courses[i].course_name(), courses[i].credit_structure(), Arrays.toString(courses[i].course_prerequisites())};
                        }
                        OutputHandler.table(coursesTable, new String[]{"Course code", "Course name", "Credit structure", "Prerequisites"}, new int[]{12, 40, 10, 10});
                    }
                }

            }
        
        
        
    }
}

//└── src
//    ├── main
//    │   ├── java
//    │   │   └── org
//    │   │       └── academic
//    │   │           ├── authentication
//    │   │           │   └── User.java
//    │   │           ├── cli
//    │   │           │   ├── menu
//    │   │           │   │   ├── AdminMenu.java
//    │   │           │   │   ├── FacultyMenu.java
//    │   │           │   │   └── StudentMenu.java
//    │   │           │   └── Navigation.java
//    │   │           ├── courses
//    │   │           │   ├── Course.java
//    │   │           │   ├── CourseCatalog.java
//    │   │           │   └── CourseOffering.java
//    │   │           ├── database
//    │   │           │   └── Database.java
//    │   │           ├── reports
//    │   │           │   └── Transcript.java
//    │   │           ├── students
//    │   │           │   ├── Student.java
//    │   │           │   └── StudentRecord.java
//    │   │           └── Main.java
//    │   └── resources
//    │       └── database.properties
