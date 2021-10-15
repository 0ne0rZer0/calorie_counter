package com.example.caloriecounter.utils;

import com.example.caloriecounter.AbstractTest;
import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Rollback(false)
@SpringBootTest
public class QueryUtilsTest extends AbstractTest {
    @Test
    public void transformQueryForUsersHappyTest( ) {
        List<User> users = getUsersWithDifferentRoles();
        List<String> sessionIds = storeSessionIds();
        String query = "id gt "+users.get(1).getId();
        try {
            QueryUtils.transformQueryForUsers(query,users.get(0),0, 10);
            assertThat(false).isTrue();
        } catch (ValidationException validationException) {
            assertThat(false).isFalse();
        }
    }
    @Test
    public void transformQueryForUsersFailTest( ) {
        List<User> users = getUsersWithDifferentRoles();
        List<String> sessionIds = storeSessionIds();
        String query = "id gt "+users.get(0).getId();
        try {
            QueryUtils.transformQueryForUsers(query,users.get(1),0, 10);
            assertThat(false).isTrue();
        } catch (ValidationException validationException) {
            assertThat(false).isFalse();
        }
    }
}
