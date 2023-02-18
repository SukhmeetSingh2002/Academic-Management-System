package org.academic.Database;

public record Course_Offerings_DTO(String course_code, String course_name, String course_instructor, String[] course_prerequisites , String credit_structure) {

}
