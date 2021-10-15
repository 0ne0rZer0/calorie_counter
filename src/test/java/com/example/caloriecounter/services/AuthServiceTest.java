package com.example.caloriecounter.services;

import com.example.caloriecounter.AbstractTest;
import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.constants.CommonConstants;
import com.example.caloriecounter.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Rollback;

import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Rollback(false)
@SpringBootTest
public class AuthServiceTest extends AbstractTest {
    @Test
    public void authenticateUserHappyTest() {
        List<User> usersList = getUsersWithDifferentRoles();
        List<String> sessionId = storeSessionIds();
        try {
            User user = authService.authenticateUser(sessionId.get(0));
            assertThat(user).isNotNull();
        }catch (ValidationException validationException) {
            assertThat(false).isEqualTo(true);
        }
    }
    @Test
    public void authenticateUserFailTest() {
        List<User> usersList = getUsersWithDifferentRoles();
        ValidationException validationException = assertThrows(ValidationException.class, ()-> {
            authService.authenticateUser("wrongsessionid");
        });
        String expectedMessage = CommonConstants.INVALID_USER_ID;
        String actualMessage = validationException.getMessage();
        assertThat(actualMessage).isEqualTo(expectedMessage);
    }
    @Test
    public void generateSessionIdHappyTest() {
        assertThat(authService.generateSessionID()).isNotNull();
    }
    @Test
    public void securePasswordHappyTest() {
        assertThat(authService.securePassword("password12312312412412412")).isNotNull();
    }
    @Test
    public void generateSessionExpiryDateHappyTest() {
        assertThat(authService.generateSessionExpiryDate()).isNotEqualTo(new Date());
    }
    @Test
    public void authenticatePasswordHappyTest() {
        List<User> usersList = getUsersWithDifferentRoles();
        assertThat(authService.authenticatePassword("12345678", usersList.get(0).getPasswordHash())).isTrue();
    }
    @Test
    public void authenticatePasswordFailTest() {
        List<User> usersList = getUsersWithDifferentRoles();
        assertThat(authService.authenticatePassword("12345677", usersList.get(0).getPasswordHash())).isFalse();
    }

}
