package org.academic.Database;

import org.academic.cli.OutputHandler;

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
                is_course_prerequisites_equals(courseOfferingsDTO.course_prerequisites) &&
                credit_structure.equals(courseOfferingsDTO.credit_structure) &&
                CGPA_cutoff.equals(courseOfferingsDTO.CGPA_cutoff);
    }

    private boolean is_course_prerequisites_equals(String[] coursePrerequisites) {
//        check if both are null
        if (course_prerequisites == null && coursePrerequisites == null) {
            return true;
        }
//        check if one is null and other is not
        if (course_prerequisites == null || coursePrerequisites == null) {
            return false;
        }
//        compare each element using for loop
        for (int i = 0; i < course_prerequisites.length; i++) {
            if (!course_prerequisites[i].equals(coursePrerequisites[i])) {
                return false;
            }
        }
        return true;
    }

}
