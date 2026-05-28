package com.library.config;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Value("${security.password-policy.min-length:8}")
    private int minLength;

    @Value("${security.password-policy.require-uppercase:true}")
    private boolean requireUppercase;

    @Value("${security.password-policy.require-lowercase:true}")
    private boolean requireLowercase;

    @Value("${security.password-policy.require-digit:true}")
    private boolean requireDigit;

    @Value("${security.password-policy.require-special:false}")
    private boolean requireSpecial;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        if (password.length() < minLength) {
            buildConstraintViolation(context, "密码长度至少" + minLength + "位");
            return false;
        }

        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            buildConstraintViolation(context, "密码必须包含大写字母");
            return false;
        }

        if (requireLowercase && !password.matches(".*[a-z].*")) {
            buildConstraintViolation(context, "密码必须包含小写字母");
            return false;
        }

        if (requireDigit && !password.matches(".*\\d.*")) {
            buildConstraintViolation(context, "密码必须包含数字");
            return false;
        }

        if (requireSpecial && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            buildConstraintViolation(context, "密码必须包含特殊字符");
            return false;
        }

        String[] commonPasswords = {"123456", "password", "12345678", "qwerty", "abc123",
                "admin", "letmein", "welcome", "monkey", "1234567"};
        for (String common : commonPasswords) {
            if (password.toLowerCase().contains(common.toLowerCase())) {
                buildConstraintViolation(context, "密码过于简单，请使用更复杂的密码");
                return false;
            }
        }

        return true;
    }

    private void buildConstraintViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message).addConstraintViolation();
    }
}
