package com.example.caloriecounter.utils;

import com.example.caloriecounter.AbstractTest;
import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.constants.CommonConstants;
import com.example.caloriecounter.models.User;
import io.micrometer.core.instrument.config.validate.Validated;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rollback(false)
@SpringBootTest
public class ValidationUtilsTest extends AbstractTest {
    @Test
    public void validateEmailHappyTest() {
        assertDoesNotThrow(()-> {
            ValidationUtils.validateEmail("users@email.com");
        });
    }
    @Test
    public void validateEmailFailTest() {
        ValidationException validationException = assertThrows(ValidationException.class, ()-> {
            ValidationUtils.validateEmail("email.com");
        });
        String expectedMessage = CommonConstants.INVALID_EMAIL;
        String actualMessage = validationException.getMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
    @Test
    public void validateNonNullHappyTest() {
        List<User> users = getUsersWithDifferentRoles();
        assertDoesNotThrow(()-> {
            ValidationUtils.validateNonNull(users.get(0), "user");
        });
    }
    @Test
    public void validateNonNullFailTest() {
        List<User> users = getUsersWithDifferentRoles();
        ValidationException validationException = assertThrows(ValidationException.class, ()-> {
            ValidationUtils.validateNonNull(null, "user");
        });
        String expectedMessage = "user  NULL" ;
        String actualMessage = validationException.getMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
    @Test
    public void validateMinLengthHappyTest() {
        assertDoesNotThrow(()-> {
            ValidationUtils.validateMinLength("12345678",8);
        });
    }
    @Test
    public void validateMinLengthFailTest() {
        ValidationException validationException = assertThrows(ValidationException.class, ()-> {
            ValidationUtils.validateMinLength("1234567",8);
        });
        String expectedMessage = CommonConstants.PSWD_TOO_SMALL;
        String actualMessage = validationException.getMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }

}
