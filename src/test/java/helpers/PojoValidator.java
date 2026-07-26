package helpers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

public class PojoValidator {

    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    public static <T> void validate(T pojo) {
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(pojo);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("POJO validation failed for [")
                    .append(pojo.getClass().getSimpleName())
                    .append("] with ")
                    .append(violations.size())
                    .append(" violation(s):\n");

            for (ConstraintViolation<T> v : violations) {
                sb.append("  ❌  field='")
                        .append(v.getPropertyPath())
                        .append("'  constraint='")
                        .append(v.getMessage())
                        .append("'  actual='")
                        .append(v.getInvalidValue())
                        .append("'\n");
            }
            throw new AssertionError(sb.toString());
        }
    }
}
