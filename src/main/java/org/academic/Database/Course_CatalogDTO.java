package org.academic.Database;

import java.util.Arrays;
import java.util.Objects;

public record Course_CatalogDTO(String course_code, String course_name, String[] course_prerequisites, String credit_structure) {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Course_CatalogDTO that = (Course_CatalogDTO) o;
        return course_code.equals(that.course_code) && course_name.equals(that.course_name) && Arrays.equals(course_prerequisites, that.course_prerequisites) && credit_structure.equals(that.credit_structure);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(course_code, course_name, credit_structure);
        result = 31 * result + Arrays.hashCode(course_prerequisites);
        return result;
    }
}
