package org.academic.Database;

import java.util.Arrays;

public record Course_Offerings_DTO(String course_code, String course_name, String course_instructor, String[] course_prerequisites , String credit_structure, String CGPA_cutoff) {

//    define equals method
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Course_Offerings_DTO courseOfferingsDTO)) {
            return false;
        }
        return course_code.equals(courseOfferingsDTO.course_code) &&
                course_name.equals(courseOfferingsDTO.course_name) &&
                course_instructor.equals(courseOfferingsDTO.course_instructor) &&
                Arrays.equals(course_prerequisites, courseOfferingsDTO.course_prerequisites) &&
                credit_structure.equals(courseOfferingsDTO.credit_structure) &&
                CGPA_cutoff.equals(courseOfferingsDTO.CGPA_cutoff);
    }
}
