package helpers;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;

/**
 * Utility for validating deserialized Stripe API response POJOs using
 * Jakarta Bean Validation (Hibernate Validator 8.x).
 *
 * <p>Constraints are declared directly on the POJO fields via annotations
 * such as {@code @NotNull}, {@code @NotBlank}, {@code @Pattern}, and {@code @Min}.
 *
 * <p>Usage example:
 * <pre>
 *   CustomerResponse customer = response.as(CustomerResponse.class);
 *   PojoValidator.validate(customer);
 * </pre>
 *
 * <p>A failing validation throws an {@link AssertionError} listing every violated
 * constraint, so TestNG marks the test as FAILED with a descriptive message.
 */
public class PojoValidator {

    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    /**
     * Validates all Bean Validation constraints declared on the given POJO.
     *
     * @param pojo the deserialized Stripe response object to validate
     * @param <T>  the POJO type
     * @throws AssertionError with a full list of violations if any constraint fails
     */
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
