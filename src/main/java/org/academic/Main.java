package org.academic;

import org.academic.Authentication.Authenticator;
import org.academic.Authentication.Session;
import org.academic.User.Student;

import java.sql.SQLException;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Hello world!");
//        take input from user
        System.out.println("Enter username: ");
        String username = scanner.nextLine();
        System.out.println("Username is: " + username);
        System.out.println("Enter password: ");
        String password = scanner.nextLine();

        boolean isAuthenticated = false;
        try {
            isAuthenticated = Authenticator.authenticate(username, password);
        } catch (SQLException e) {
            System.out.println("Error authenticating user");
        }

        if (isAuthenticated) {
            System.out.println("User is authenticated");
            System.out.println("User type: " + Session.getInstance().getUserType());
            System.out.println("User name: " + Session.getInstance().getUserName());
            System.out.println("User password: " + Session.getInstance().getPassword());
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
