/* This file will contain the SQL statements to create the tables and insert the data into the tables. */

-- Design a database which comprises of the following concepts:
-- 1. Course Catalog: This contains all the list of courses which can be offered in IIT Ropar. For each
-- course, we have information on its credit structure (L-T-P) and list of prerequisites (if any).
-- 2. Course Offerings: Each semester, a faculty offers one or multiple courses. These courses should
-- be present in the course catalog. With each course offering, the instructors may define constraints
-- on CGPA (e.g., CGPA > 7.0).
-- 3. Student Course Registration: A student registers for one or more courses. However, the number of
-- credits he/she is allowed is governed by the scheme governed by the institute (1.25 times the
-- average of the credits earned in the previous two semesters.
-- 4. Report Generation: Staff in the academic office need to generate various kinds of reports (e.g.,
-- transcripts of students)
-- 5. Grade entry by Course Instructors: Instructors upload the grades of students in a course via a file.
-- 6. User Authentication: All the users must be authenticated before login.


-- 1. Implement the concept of UG curriculum into the application.
-- 2. You should maintain a list of Program Cores and Program Electives and information about BTP
-- Capstone.
-- 3. Note that information in item 2 may change with time. For e.g., a course which was PC for a
-- batch may no longer be a PC for their junior batch.
-- 4. In summary you would have to maintain enough information to track his progress through the UG
-- curriculum.
-- 5. Implement a procedure to check for graduation check. A student is allowed to check if he/she has
-- completed all the Program Core courses, minimum number of program electives and passed the
-- BTP Capstone credits.

-- office staff table
CREATE TABLE office_staff
(
    staff_id SERIAL,
    name     VARCHAR(50) NOT NULL,
    email    VARCHAR(50) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    FOREIGN KEY (username) REFERENCES user_authentication (user_name),
    PRIMARY KEY (staff_id)
);


-- create a table which stores current academic events SEMESTER_START,COURSE_FLOAT_START,COURSE_FLOAT_END,COURSE_REGISTRATION_START,COURSE_REGISTRATION_END,SEMESTER_END
CREATE TABLE academic_calendar
(
    event_name VARCHAR(50) NOT NULL,
    event_date DATE        NOT NULL,
    semester   VARCHAR(50) NOT NULL,
    is_current BOOLEAN     NOT NULL,
    FOREIGN KEY (semester) REFERENCES semester (semester),
    PRIMARY KEY (event_name, semester)
);

-- Create the table ug_curriculum which contains Program Core and Program Electives for particular batch
CREATE TABLE batch
(
    batchYear             INT NOT NULL,
    min_credits           INT NOT NULL,
    min_program_electives INT NOT NULL,
    PRIMARY KEY (batchYear)
);



-- Create the table
CREATE TABLE student
(
    entry_number VARCHAR(50) NOT NULL UNIQUE,
    name         VARCHAR(50) NOT NULL,
    username     VARCHAR(50) NOT NULL UNIQUE,
    email        VARCHAR(50) NOT NULL,
    batch        INT         NOT NULL,
    FOREIGN KEY (batch) REFERENCES batch (batchYear),
    PRIMARY KEY (entry_number),
    -- TODO change
    FOREIGN KEY (username) REFERENCES user_authentication (user_name)
);



-- create the table
CREATE TABLE instructor
(
    instructor_id SERIAL,
    name          VARCHAR(50) NOT NULL,
    username      VARCHAR(50) NOT NULL UNIQUE,
    email         VARCHAR(50) NOT NULL,
    department    VARCHAR(50) NOT NULL,
    PRIMARY KEY (instructor_id),
    FOREIGN KEY (username) REFERENCES user_authentication (user_name)
);



-- create the table
CREATE TABLE semester
(
    semester            VARCHAR(50) NOT NULL UNIQUE,
    academic_year       INT         NOT NULL,
    is_current_semester BOOLEAN     NOT NULL,
    PRIMARY KEY (semester)
);



-- TODO: check prerequisites
-- Create the table
CREATE TABLE course_catalog
(
    course_code      VARCHAR(50) NOT NULL UNIQUE,
    course_name      VARCHAR(50) NOT NULL,
    credit_structure VARCHAR(50) NOT NULL,
    PRIMARY KEY (course_code)
);

-- create prerequisites table
CREATE TABLE prerequisites
(
    course_code              VARCHAR(50) NOT NULL,
    prerequisite_course_code VARCHAR(50) NOT NULL,
    min_grade                VARCHAR(50) NOT NULL,
    FOREIGN KEY (course_code) REFERENCES course_catalog (course_code),
    FOREIGN KEY (prerequisite_course_code) REFERENCES course_catalog (course_code),
    PRIMARY KEY (course_code, prerequisite_course_code)
);



-- Create the table
CREATE TABLE course_offerings
(
    course_code     VARCHAR(50) NOT NULL,
    semester        VARCHAR(50) NOT NULL,
    instructor_id   INT         NOT NULL,
    cgpa_constraint FLOAT       NOT NULL,
    FOREIGN KEY (course_code) REFERENCES course_catalog (course_code),
    FOREIGN KEY (instructor_id) REFERENCES instructor (instructor_id),
    FOREIGN KEY (semester) REFERENCES semester (semester), /* semester is a table which contains all the semesters */
    PRIMARY KEY (course_code, semester)
);


-- Create the table
CREATE TABLE student_course_registration
(
    student_entry_number VARCHAR(50) NOT NULL,
    course_code          VARCHAR(50) NOT NULL,
    semester             VARCHAR(50) NOT NULL,
    status               VARCHAR(50) NOT NULL,
    FOREIGN KEY (student_entry_number) REFERENCES student (entry_number),
    FOREIGN KEY (semester) REFERENCES semester (semester),
    FOREIGN KEY (course_code, semester) REFERENCES course_offerings (course_code, semester),
    PRIMARY KEY (student_entry_number, course_code, semester)
);

-- Create the table
CREATE TABLE grade_entry
(
    student_entry_number VARCHAR(50) NOT NULL,
    course_code          VARCHAR(50) NOT NULL,
    semester             VARCHAR(50) NOT NULL,
    grade                VARCHAR(50) NOT NULL,
    FOREIGN KEY (student_entry_number) REFERENCES student (entry_number),
    FOREIGN KEY (course_code, semester) REFERENCES course_offerings (course_code, semester),
    FOREIGN KEY (semester) REFERENCES semester (semester),
    PRIMARY KEY (student_entry_number, course_code, semester)
);


-- Create the table
CREATE TABLE user_authentication
(
    user_name VARCHAR(50) NOT NULL UNIQUE,
    password  VARCHAR(50) NOT NULL,
    role      VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_name)
);




-- Create the table ug_curriculum which contains Program Core and Program Electives for particular batch
CREATE TABLE ug_curriculum
(
    batch       INT         NOT NULL,
    course_code VARCHAR(50) NOT NULL,
    course_type VARCHAR(50) NOT NULL,
    FOREIGN KEY (course_code) REFERENCES course_catalog (course_code),
    FOREIGN KEY (batch) REFERENCES batch (batchYear),
    PRIMARY KEY (batch, course_code)
);


-- make login_log table
CREATE TABLE login_log
(
    user_name VARCHAR(50) NOT NULL,
    login_time TIMESTAMP NOT NULL,
    logout_time TIMESTAMP ,
    login_id VARCHAR(50) NOT NULL,
    is_logged_in BOOLEAN NOT NULL,
    FOREIGN KEY (user_name) REFERENCES user_authentication (user_name),
    PRIMARY KEY (user_name, login_time)
);

-- create event log table
CREATE TABLE event_log
(
    user_name VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    event TEXT NOT NULL,
    login_id VARCHAR(50) NOT NULL,
    FOREIGN KEY (user_name) REFERENCES user_authentication (user_name),
    PRIMARY KEY (user_name, event_time)
);



INSERT INTO office_staff (name, email, username)
VALUES ('John Doe', 'johndoe@example.com', 'johndoe');

INSERT INTO academic_calendar (event_name, event_date, semester, is_current)
VALUES ('SEMESTER_START', '2023-08-01', 'Autumn 2023', false),
       ('COURSE_FLOAT_START', '2023-07-01', 'Autumn 2023', false),
       ('COURSE_FLOAT_END', '2023-07-15', 'Autumn 2023', false),
       ('COURSE_REGISTRATION_START', '2023-07-16', 'Autumn 2023', false),
       ('COURSE_REGISTRATION_END', '2023-08-05', 'Autumn 2023', false),
       ('SEMESTER_END', '2023-12-15', 'Autumn 2023', true);

INSERT INTO batch (batchYear, min_credits, min_program_electives)
VALUES (2021, 40, 5),
       (2022, 45, 6),
       (2023, 48, 7);

INSERT INTO student (entry_number, name, username, email, batch)
VALUES ('2019EE10001', 'Alice', 'alice', 'alice@example.com', 2021),
       ('2019EE10002', 'Bob', 'bob', 'bob@example.com', 2021),
       ('2020EE10001', 'Charlie', 'charlie', 'charlie@example.com', 2022),
       ('2020EE10002', 'David', 'david', 'david@example.com', 2022),
       ('2021EE10001', 'Eve', 'eve', 'eve@example.com', 2023),
       ('2021EE10002', 'Frank', 'frank', 'frank@example.com', 2023);

INSERT INTO instructor (name, username, email, department)
VALUES ('Dr. X', 'drx', 'drx@example.com', 'Electrical Engineering'),
       ('Dr. Y', 'dry', 'dry@example.com', 'Computer Science'),
       ('Dr. Z', 'drz', 'drz@example.com', 'Mathematics');
       ('Alice Brown', 'alicebrown', 'alicebrown@example.com', 'Computer Science'),
       ('Bob Green', 'bobgreen', 'bobgreen@example.com', 'Electrical Engineering');


INSERT INTO semester (semester, academic_year, is_current_semester)
VALUES ('Even 2021', 2021, false),
       ('Odd 2021', 2021, false),
       ('Even 2022', 2022, false),
       ('Odd 2022', 2021, false),
       ('Even 2023', 2022, false),
       ('Odd 2023', 2022, false),
       ('Even 2024', 2023, true);

INSERT INTO course_catalog (course_code, course_name, credit_structure)
VALUES ('EE101', 'Introduction to Electrical Engineering', '3-1-0-4'),
       ('EE201', 'Electric Circuits', '3-1-0-4'),
       ('EE202', 'Electromagnetic Theory', '3-1-0-4'),
       ('EE301', 'Signals and Systems', '3-0-2-4'),
       ('EE302', 'Digital Signal Processing', '3-0-2-4'),
       ('EE401', 'Communication Systems', '3-0-2-3'),
       ('EE402', 'Digital Communications', '3-0-2-2.5'),
       ('CS101', 'Introduction to Computer Science', '3-1-0-3'),
       ('CS201', 'Data Structures and Algorithms', '3-0-2-3'),
       ('CS202', 'Database Management Systems', '3-0-2-3'),
       ('MA101', 'Calculus and Differential Equations', '3-1-0-3'),
       ('MA201', 'Linear Algebra', '3-1-0-3'),
       ('MA202', 'Probability and Statistics', '3-1-0-3');
       ('CS301', 'Operating Systems', '3-0-2-3'),
       ('CS302', 'Computer Networks', '3-0-2-3'),
       ('CS401', 'Artificial Intelligence', '3-0-2-4'),
       ('CS402', 'Machine Learning', '3-0-2-4'),
       ('MA301', 'Numerical Methods', '3-0-2-3'),
       ('MA302', 'Optimization', '3-0-2-3');


-- Insert data into prerequisites table
INSERT INTO prerequisites (course_code, prerequisite_course_code, min_grade) VALUES
    ('EE201', 'EE101', 'B-'),
    ('EE202', 'EE201', 'B'),
    ('EE301', 'EE201', 'A-'),
    ('EE302', 'EE301', 'A'),
    ('EE401', 'EE302', 'B'),
    ('EE402', 'EE401', 'B'),
    ('CS201', 'CS101', 'B-'),
    ('CS202', 'CS201', 'B'),
    ('CS301', 'CS201', 'A-'),
    ('CS302', 'CS201', 'A'),
    ('CS401', 'CS201', 'B'),
    ('CS402', 'CS201', 'A-'),
    ('MA201', 'MA101', 'B-'),
    ('MA202', 'MA101', 'B'),
    ('MA301', 'MA201', 'A-'),
    ('MA302', 'MA201', 'A');

-- Insert data into course_offerings table
INSERT INTO course_offerings (course_code, semester, instructor_id, cgpa_constraint) VALUES
    ('EE101', 'Even 2022', 'drx', 10),
    ('EE201', 'Odd 2022', 'drx', 7.5),
    ('EE202', 'Even 2023', 'drx', 7.75),
    ('EE301', 'Odd 2023', 'dry', 8),
    ('EE302', 'Even 2024', 'dry', 8.25),
    ('EE401', 'Odd 2024', 'drz', 8.5),
    ('EE402', 'Even 2024', 'drz', 7.75),
    ('CS101', 'Even 2021', 'alicebrown', 10),
    ('CS201', 'Odd 2021', 'alicebrown', 6),
    ('CS202', 'Even 2022', 'bobgreen', 7.5),
    ('MA101', 'Odd 2021', 'drz', 10),
    ('MA201', 'Even 2022', 'drz', 6),
    ('MA202', 'Odd 2022', 'drz', 7.5),
    ('CS301', 'Even 2023', 'alicebrown', 8),
    ('CS302', 'Odd 2023', 'bobgreen', 8),
    ('CS401', 'Even 2024', 'alicebrown', 8.5),
    ('CS402', 'Odd 2024', 'bobgreen', 7.75),
    ('MA301', 'Even 2024', 'drz', 8),
    ('MA302', 'Odd 2024', 'drz', 7.25);


INSERT INTO student_course_registration (student_entry_number, course_code, semester) VALUES
    -- For Even 2021 semester
    ('2019EE10001', 'EE101', 'Even 2021'),
    ('2019EE10001', 'CS101', 'Even 2021'),
    ('2019EE10002', 'EE101', 'Even 2021'),
    ('2019EE10002', 'CS101', 'Even 2021'),
    ('2020EE10001', 'EE202', 'Even 2021'),
    ('2020EE10001', 'MA101', 'Even 2021'),
    ('2020EE10002', 'EE202', 'Even 2021'),
    ('2020EE10002', 'MA101', 'Even 2021'),
    -- For Odd 2021 semester
    ('2019EE10001', 'EE201', 'Odd 2021'),
    ('2019EE10001', 'CS201', 'Odd 2021'),
    ('2019EE10002', 'EE201', 'Odd 2021'),
    ('2019EE10002', 'CS201', 'Odd 2021'),
    ('2020EE10001', 'EE301', 'Odd 2021'),
    ('2020EE10001', 'MA201', 'Odd 2021'),
    ('2020EE10002', 'EE301', 'Odd 2021'),
    ('2020EE10002', 'MA201', 'Odd 2021'),
    -- For Even 2022 semester
    ('2019EE10001', 'EE302', 'Even 2022'),
    ('2019EE10001', 'CS202', 'Even 2022'),
    ('2019EE10002', 'EE302', 'Even 2022'),
    ('2019EE10002', 'CS202', 'Even 2022'),
    ('2020EE10001', 'EE401', 'Even 2022'),
    ('2020EE10001', 'CS301', 'Even 2022'),
    ('2020EE10002', 'EE401', 'Even 2022'),
    ('2020EE10002', 'CS301', 'Even 2022'),
    -- For Odd 2022 semester
    ('2019EE10001', 'EE402', 'Odd 2022'),
    ('2019EE10001', 'CS302', 'Odd 2022'),
    ('2019EE10002', 'EE402', 'Odd 2022'),
    ('2019EE10002', 'CS302', 'Odd 2022'),
    ('2020EE10001', 'MA301', 'Odd 2022'),
    ('2020EE10001', 'MA201', 'Odd 2022'),
    ('2020EE10002', 'MA301', 'Odd 2022'),
    ('2020EE10002', 'MA201', 'Odd 2022'),
    -- For Even 2023 semester
    ('2019EE10001', 'CS401', 'Even 2023'),
    ('2019EE10001', 'MA302', 'Even 2023'),
    ('2019EE10002', 'CS401', 'Even 2023'),
    ('2019EE10002', 'MA302', 'Even 2023'),
    ('2020EE10001', 'EE201', 'Even 2023'),
    ('2020EE10001', 'MA202', 'Even 2023'),
    ('2020EE10002', 'EE201', 'Even 2023'),
    ('2020EE10002', 'MA202', 'Even 2023'),
    -- For Odd 2023 semester
    ('2019EE10001', 'CS402', 'Odd 2023'),
    ('2019EE10001', 'MA101', 'Odd 2023'),


-- Dummy data for user_authentication
INSERT INTO user_authentication (user_name, password, role)
    VALUES ('alice', 'password123', 'student'),
    ('bob', 'password123', 'student'),
    ('charlie', 'password123', 'student'),
    ('david', 'password123', 'student'),
    ('eve', 'password123', 'student'),
    ('drx', 'password123', 'instructor'),
    ('dry', 'password123', 'instructor'),
    ('drz', 'password123', 'instructor'),
    ('alicebrown', 'password123', 'instructor'),
    ('bobgreen', 'password123', 'instructor');

-- Dummy data for ug_curriculum
INSERT INTO ug_curriculum (batch, course_code, course_type)
VALUES (2021, 'EE101', 'Program Core'),
    (2021, 'CS101', 'Program Core'),
    (2021, 'MA101', 'Program Core'),
    (2021, 'EE201', 'Program Core'),
    (2021, 'CS201', 'Program Core'),
    (2021, 'MA201', 'Program Core'),
    (2022, 'EE202', 'Program Core'),
    (2022, 'CS202', 'Program Core'),
    (2022, 'MA202', 'Program Core'),
    (2022, 'EE301', 'Program Core'),
    (2022, 'CS301', 'Program Core'),
    (2022, 'MA301', 'Program Core'),
    (2023, 'EE302', 'Program Core'),
    (2023, 'CS302', 'Program Core'),
    (2023, 'MA302', 'Program Core'),
    (2023, 'EE401', 'Program Elective'),
    (2023, 'EE402', 'Program Elective'),
    (2023, 'CS401', 'Program Elective'),
    (2023, 'CS402', 'Program Elective'),
    (2023, 'MA301', 'Program Elective'),