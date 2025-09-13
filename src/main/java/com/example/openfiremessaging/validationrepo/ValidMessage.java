package com.example.openfiremessaging.validationrepo;

import com.example.openfiremessaging.validation.MessageValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom validation annotation to ensure that a MessageDto is valid.
 * A message is considered valid if:
 * 1. At least one of the content fields (body, media, file) is present.
 * 2. If 'file' is present, 'fileName' must also be present.
 * 3. If 'media' is present, 'mediaName' must also be present.
 *
 * This annotation is processed by the MessageValidator class.
 */
@Target({ElementType.TYPE}) // This annotation can only be applied to classes.
@Retention(RetentionPolicy.RUNTIME) // The annotation will be available at runtime for validation processing.
@Constraint(validatedBy = MessageValidator.class) // Specifies the validator class that implements the logic.
public @interface ValidMessage {

    // Default error message if validation fails.
    String message() default "Invalid message payload";

    // Standard boilerplate for validation annotations.
    Class<?>[] groups() default {};

    // Standard boilerplate for validation annotations.
    Class<? extends Payload>[] payload() default {};
}
