package org.academic;

import org.academic.Authentication.Authenticator;
import org.academic.Authentication.Session;
import org.academic.Database.CourseRegisterDTO;
import org.academic.Database.Course_Offerings_DTO;
import org.academic.Database.GradeDTO;
import org.academic.User.Student;
import org.academic.User.UserType;
import org.academic.cli.OutputHandler;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Scanner;


public class Main {
    public static void handleStudent() {
//        TODO: set entry number
        Student student = new Student(Session.getInstance().getUserName(), Session.getInstance().getPassword(), Session.getInstance().getUserName().toUpperCase());
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
            } else if (Session.getInstance().getUserType() == UserType.ADMIN) {
                System.out.println("Admin");
            }
        } else {
            System.out.println("User is not authenticated10");
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
