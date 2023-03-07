# Academic Management System

This is a Java-based Academic Management System that helps educational institutions manage their students, courses, and instructors.

## Description

The Academic Management System is a command-line application built in Java that allows academic institutions to manage their courses, curriculum, and student grades. It provides functionalities for students, faculty, and office staff to interact with the system.

The application is built using Java and PostgreSQL for the database management system. It uses JUnit and Mockito for testing and JaCoCo for code coverage.

## Prerequisites

To build and run the project, you'll need:

- Java 11 or higher
- Gradle 7 or higher

## Building the Project

To build the project, navigate to the project root directory and run:


``` bash
./gradlew build
```

This will build the project and run all tests.

## Running the Project

To run the project, navigate to the project root directory and run:

```bash
./gradlew run --console=plain -q
```


## Testing

The project includes automated tests that can be run using Gradle. To run tests, navigate to the project root directory and run:

```bash
./gradlew test
```

This will run all tests and generate a test report in the build/reports/tests/test directory.

## Features

- User authentication and session management
- Add, view, and drop courses
- Register and drop courses
- Grade submission and retrieval
- Curriculum management

---
## Project Structure

The project is structured as follows:

- `src/main/java/org/academic/Authentication`: Contains classes for user authentication and session management.
- `src/main/java/org/academic/cli`: Contains classes for handling input and output.
- `src/main/java/org/academic/Database`: Contains classes for interacting with the PostgreSQL database.
- `src/main/java/org/academic/Services`: Contains classes for providing services to different types of users.
- `src/main/java/org/academic/User`: Contains classes for different types of users.
- `src/main/java/org/academic/Main`.java: The entry point for the application.
- `src/test/java/org/academic:` Contains test classes.