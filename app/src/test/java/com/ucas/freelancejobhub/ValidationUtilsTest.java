package com.ucas.freelancejobhub;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.ucas.freelancejobhub.utils.ValidationUtils;

import org.junit.Test;

public class ValidationUtilsTest {
    @Test
    public void emailValidation_matchesProjectSpecification() {
        assertTrue(ValidationUtils.isValidEmail("freelancer@example.com"));
        assertFalse(ValidationUtils.isValidEmail("freelancer@invalid"));
    }

    @Test
    public void strongPassword_requiresUpperLowerAndNumber() {
        assertTrue(ValidationUtils.isStrongPassword("Contract9"));
        assertFalse(ValidationUtils.isStrongPassword("contract"));
    }
}
