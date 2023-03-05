/* This file will contain the SQL statements to create the tables and insert the data into the tables. */

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

-- insert into office_staff values (1, 'Rajiv', 'Rajiv@iitrpr.ac.in', 'staff');

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

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_batch_data(batchYear INT, min_credits INT, min_program_electives INT) AS
$$
BEGIN
    INSERT INTO batch VALUES (batchYear, min_credits, min_program_electives);
END;
$$ LANGUAGE plpgsql;

call insert_batch_data(2019, 120, 3);
call insert_batch_data(2020, 120, 3);


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

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_student_data(entry_number VARCHAR(50), name VARCHAR(50), email VARCHAR(50),
                                                batch INT, username VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO student VALUES (entry_number, name, username, email, batch);
END;


call insert_student_data('2019CS101', 'John', '2019csb101@iitrpr.ac.in', 2019);


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

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_instructor_data(instructor_id INT, name VARCHAR(50), email VARCHAR(50),
                                                   department VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO instructor VALUES (instructor_id, name, email, department);
END;
$$ LANGUAGE plpgsql;

call insert_instructor_data(1, 'John', 'John@gmail.com', 'CSE');
call insert_instructor_data(2, 'Barry', 'Barry@gmal.com', 'CSE');
call insert_instructor_data(3, 'Clark', 'Clark@gmail.com', 'CSE');

-- create the table
CREATE TABLE semester
(
    semester            VARCHAR(50) NOT NULL UNIQUE,
    academic_year       INT         NOT NULL,
    is_current_semester BOOLEAN     NOT NULL,
    PRIMARY KEY (semester)
);

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_semester_data(semesterS VARCHAR(50), academic_year INT,
                                                 is_current_semester BOOLEAN) AS
$$
BEGIN
    INSERT INTO semester VALUES (semesterS, academic_year, is_current_semester);
END;
$$ LANGUAGE plpgsql;

-- update the current semester stored procedure pgSQL
CREATE OR REPLACE PROCEDURE update_current_semester(semesterP VARCHAR(50), academic_yearP INT) AS
$$
BEGIN
    UPDATE semester SET is_current_semester = FALSE WHERE is_current_semester = TRUE;
    UPDATE semester SET is_current_semester = TRUE WHERE semester = semesterP AND academic_year = academic_yearP;
END;
$$ LANGUAGE plpgsql;


-- Procedure to insert the data from csv file pgSQL
-- CREATE OR REPLACE PROCEDURE insert_student_data_from_csv(csv_file_path VARCHAR(100)) AS $$
-- DECLARE
--     csv_file RECORD;
-- BEGIN
--     FOR csv_file IN SELECT * FROM csv_to_recordset(csv_file_path) LOOP
--         INSERT INTO student VALUES (csv_file.entry_number, csv_file.name, csv_file.email);
--     END LOOP;
-- END;
-- $$ LANGUAGE plsql;


call insert_student_data(1, 'John', 'john@wqer.com');

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


-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_course_catalog_data(course_code VARCHAR(50), course_name VARCHAR(50),
                                                       credit_structure VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO course_catalog VALUES (course_code, course_name, credit_structure);
END;
$$ LANGUAGE plpgsql;

-- insert prerequisites
CREATE OR REPLACE PROCEDURE insert_prerequisites_data(course_code VARCHAR(50), prerequisite_course_code VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO prerequisites VALUES (course_code, prerequisite_course_code);
END;
$$ LANGUAGE plpgsql;

call insert_course_catalog_data('CS 101', 'Introduction to Computer Science', '3-1-0');
call insert_course_catalog_data('CS 102', 'Introduction to Computer Science Lab', '0-0-3');
call insert_course_catalog_data('CS 201', 'Data Structures and Algorithms', '3-1-0');
call insert_prerequisites_data('CS 201', 'CS 101');
call insert_prerequisites_data('CS 201', 'CS 102');
call insert_prerequisites_data('CS 201', 'CS 103');
call insert_prerequisites_data('CS 201', 'CS 104');

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

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_course_offerings_data(course_code VARCHAR(50), semester VARCHAR(50),
                                                         instructor_id INT, cgpa_constraint FLOAT) AS
$$
BEGIN
    INSERT INTO course_offerings VALUES (course_code, semester, instructor_id, cgpa_constraint);
END;
$$ LANGUAGE plpgsql;

call insert_course_offerings_data('CS 101', 'Fall 2023', 1, 7.0);
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

-- Insert the data procedure to insert the data into the table pgSQL

CREATE OR REPLACE PROCEDURE insert_student_course_registration_data(student_entry_number VARCHAR(50),
                                                                    course_code VARCHAR(50),
                                                                    semester VARCHAR(50),
                                                                    status VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO student_course_registration VALUES (student_entry_number, course_code, semester, status);
END;
$$ LANGUAGE plpgsql;

call insert_student_course_registration_data('2020CS103', 'CS 101', 'Fall 2023', 'ON_GOING');
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

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_grade_entry_data(student_entry_number VARCHAR(50), course_code VARCHAR(50),
                                                    semester VARCHAR(50), grade VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO grade_entry VALUES (student_entry_number, course_code, semester, grade);
END;
$$ LANGUAGE plpgsql;

call insert_grade_entry_data('2019CS101', 'CS 101', 'Fall 2023', 'A');

-- Create the table
CREATE TABLE user_authentication
(
    user_name VARCHAR(50) NOT NULL UNIQUE,
    password  VARCHAR(50) NOT NULL,
    role      VARCHAR(50) NOT NULL,
    PRIMARY KEY (user_name)
);

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_user_authentication_data(user_name VARCHAR(50), password VARCHAR(50), role VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO user_authentication VALUES (user_name, password, role);
END;
$$ LANGUAGE plpgsql;

call insert_user_authentication_data('John', '123', 'Student');


-- create a stored procedure to deregister a course
-- CREATE OR REPLACE PROCEDURE deregister_course(_student_entry_number INT, _course_code VARCHAR(50),
--                                               _semester VARCHAR(50)) AS
-- $$
-- BEGIN
--     DELETE
--     FROM student_course_registration
--     WHERE student_entry_number = _student_entry_number
--       AND course_code = _course_code
--       AND semester = _semester;
-- END;
-- $$ LANGUAGE plpgsql;

-- call deregister_course(1, 'CS 101', 'Fall 2019');

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

-- Insert the data procedure to insert the data into the table pgSQL
CREATE OR REPLACE PROCEDURE insert_ug_curriculum_data(batch INT, course_code VARCHAR(50), course_type VARCHAR(50)) AS
$$
BEGIN
    INSERT INTO ug_curriculum VALUES (batch, course_code, course_type);
END;
$$ LANGUAGE plpgsql;

call insert_ug_curriculum_data(2019, 'CS 101', 'PC');
call insert_ug_curriculum_data(2019, 'CS 102', 'PE');
call insert_ug_curriculum_data(2019, 'CS 103', 'PE');
call insert_ug_curriculum_data(2019, 'CS 104', 'PE');


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