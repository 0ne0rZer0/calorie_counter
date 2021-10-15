package com.example.caloriecounter.utils;

import com.example.caloriecounter.AbstractTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;

import static com.example.caloriecounter.utils.AuthUtils.getSalt;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Rollback(false)
@SpringBootTest
public class AuthUtilsTest extends AbstractTest {

    @Test
    public void testSecurePassword(){
        String expectedSecurePassword = "wVz2T+3RlmM1F78VcdpSQdf1kT51ZQjjf9ywaKGRxGU=";
        assertThat(AuthUtils.generateSecurePassword("password", getSalt())).isEqualTo(expectedSecurePassword);
        assertThat(AuthUtils.generateSecurePassword("password0", getSalt()).equals(expectedSecurePassword)).isFalse();
    }

    @Test
    public void testveriyPassword(){
        String expectedSecurePassword = "wVz2T+3RlmM1F78VcdpSQdf1kT51ZQjjf9ywaKGRxGU=";
        assertThat(AuthUtils.verifyUserPassword("password", expectedSecurePassword, getSalt())).isTrue();
        assertThat(AuthUtils.verifyUserPassword("passworD", expectedSecurePassword, getSalt())).isFalse();
    }
}
