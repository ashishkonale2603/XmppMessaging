package com.example.openfiremessaging.validation;

import com.example.openfiremessaging.dto.MessageDto;
import com.example.openfiremessaging.validationrepo.ValidMessage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

/**
 * Validator for the @ValidMessage annotation. Implements the business rules
 * for a valid MessageDto.
 */
public class MessageValidator implements ConstraintValidator<ValidMessage, MessageDto> {

    @Override
    public void initialize(ValidMessage constraintAnnotation) {
        // No initialization needed for this validator.
    }

    @Override
    public boolean isValid(MessageDto messageDto, ConstraintValidatorContext context) {
        if (messageDto == null) {
            return false; // Or true, depending on whether null objects are considered valid.
        }

        boolean isBodyPresent = StringUtils.hasText(messageDto.getBody());
        boolean isMediaPresent = StringUtils.hasText(messageDto.getMedia());
        boolean isFilePresent = StringUtils.hasText(messageDto.getFile());

        boolean isMediaNamePresent = StringUtils.hasText(messageDto.getMediaName());
        boolean isFileNamePresent = StringUtils.hasText(messageDto.getFileName());

        boolean isValid = true;

        // Rule 1: At least one of body, media, or file must be present.
        if (!isBodyPresent && !isMediaPresent && !isFilePresent) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Message is empty. Please provide a body, media, or file.")
                    .addConstraintViolation();
            isValid = false;
        }

        // Rule 2: If media content is present, mediaName is required.
        if (isMediaPresent && !isMediaNamePresent) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("mediaName is required when media content is provided.")
                    .addPropertyNode("mediaName") // Associates the error with the 'mediaName' field.
                    .addConstraintViolation();
            isValid = false;
        }

        // Rule 3: If file content is present, fileName is required.
        if (isFilePresent && !isFileNamePresent) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("fileName is required when file content is provided.")
                    .addPropertyNode("fileName") // Associates the error with the 'fileName' field.
                    .addConstraintViolation();
            isValid = false;
        }

        return isValid;
    }
}
