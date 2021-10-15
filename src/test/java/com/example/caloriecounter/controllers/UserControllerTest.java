package com.example.caloriecounter.controllers;

import com.example.caloriecounter.AbstractTest;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.Rollback;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Rollback(false)
@SpringBootTest
public class UserControllerTest extends AbstractTest {

    @Test
    public void signUpUsers() {

        ArrayList<DTO.UserSignUpRequestDTO> dummyUsers = getDummyUser();
        DTO.UserSignUpRequestDTO normalUser = dummyUsers.get(0);
        ResponseEntity<DTO.StatusResponseDTO> normalUserSignup = userController.signup(normalUser);
        assertThat(normalUserSignup.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        DTO.UserSignUpRequestDTO noNameUser = dummyUsers.get(1);
        noNameUser.setFirstName("");
        noNameUser.setLastName("");
        ResponseEntity<DTO.StatusResponseDTO> noNameUserSignup = userController.signup(noNameUser);
        assertThat(noNameUserSignup.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        DTO.UserSignUpRequestDTO smallPasswordUser = dummyUsers.get(2);
        smallPasswordUser.setPassword("1234");
        ResponseEntity<DTO.StatusResponseDTO> smallPasswordUserSignUp = userController.signup(smallPasswordUser);
        assertThat(smallPasswordUserSignUp.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);


        DTO.UserSignUpRequestDTO wrongEmailUser = dummyUsers.get(3);
        wrongEmailUser.setEmail("wrong@@wrong.com");
        ResponseEntity<DTO.StatusResponseDTO> wrongEmailDTO = userController.signup(wrongEmailUser);
        assertThat(wrongEmailDTO.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void signInUsers() {
        userRepository.deleteAll();

        fillData();
        ArrayList<String> dummyData = getDummyData();
        ArrayList<DTO.UserSignUpRequestDTO> dummyUser = getDummyUser();

        //Wrong email Right password
        DTO.UserSignInRequestDTO wrongEmailSignIn = DTO.UserSignInRequestDTO.builder()
                .email(dummyUser.get(0).getEmail()).password(dummyUser.get(0).getPassword()).build();
        wrongEmailSignIn.setEmail("wrong@email.com");
        assertThat(userController.signIn(wrongEmailSignIn).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //Wrong password
        DTO.UserSignInRequestDTO wrongPasswordSignIn = DTO.UserSignInRequestDTO.builder()
                .email(dummyUser.get(1).getEmail()).password(dummyUser.get(1).getPassword()).build();
        wrongPasswordSignIn.setPassword("notThis");
        assertThat(userController.signIn(wrongPasswordSignIn).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        //Right both
        DTO.UserSignInRequestDTO normalSignIn = DTO.UserSignInRequestDTO.builder()
                .email(dummyUser.get(2).getEmail()).password(dummyUser.get(2).getPassword()).build();
        assertThat(userController.signIn(normalSignIn).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }

    @Test
    public void updateUsers() {
        userRepository.deleteAll();
        ArrayList<DTO.UserSignUpRequestDTO> dummyUser = getDummyUser();
        List<Long> userId = fillData();
        System.out.println(userRepository.findAll().size());
        ArrayList<String> authenticationCodes = storeSessionIds();
        setRoles(authenticationCodes);
        //------------------------Update Test starts here-------------------------------------------//
        String adminSessionId = authenticationCodes.get(0);
        String managerSessionId = authenticationCodes.get(1);
        String userSessionId = authenticationCodes.get(2);


        // Admin changes Normal to Manager T
        DTO.UserUpdateRequestDTO adminNormalToManager = DTO.UserUpdateRequestDTO.builder().role(1).firstName(dummyUser.get(2).
                getFirstName()).lastName(dummyUser.get(2).getLastName()).email(dummyUser.get(2).getEmail()).password(dummyUser.get(2).getPassword()).expectedCalories(dummyUser.get(2).getExpectedCalories()).
                build();
        ResponseEntity<DTO.StatusResponseDTO> adminNormalToManagerResponse =  userController.updateUser(adminNormalToManager, adminSessionId, userId.get(3-1));
        System.out.println(adminNormalToManagerResponse.getBody().toString());
        assertThat(adminNormalToManagerResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Admin changes Manager to Normal T
        DTO.UserUpdateRequestDTO adminManagerToNormal = DTO.UserUpdateRequestDTO.builder().role(0).firstName(dummyUser.get(2).
                getFirstName()).lastName(dummyUser.get(2).getLastName()).email(dummyUser.get(2).getEmail()).password(dummyUser.get(2).getPassword()).expectedCalories(dummyUser.get(2).getExpectedCalories()).build();
        ResponseEntity<DTO.StatusResponseDTO> adminManagerToNormalResponse =  userController.updateUser(adminManagerToNormal, adminSessionId, userId.get(3-1));
        assertThat(adminManagerToNormalResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Manager changes Normal to Admin F
        DTO.UserUpdateRequestDTO managerNormalToAdmin = DTO.UserUpdateRequestDTO.builder().role(2).firstName(dummyUser.get(2).
                getFirstName()).lastName(dummyUser.get(2).getLastName()).email(dummyUser.get(2).getEmail()).password(dummyUser.get(2).getPassword()).expectedCalories(dummyUser.get(2).getExpectedCalories()).build();
        ResponseEntity<DTO.StatusResponseDTO> managerNormalToAdminResponse =  userController.updateUser(managerNormalToAdmin, managerSessionId,userId.get(3-1));
        assertThat(managerNormalToAdminResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Manager changes Normal to Manager T
        DTO.UserUpdateRequestDTO managerNormalToManager = DTO.UserUpdateRequestDTO.builder().role(1).firstName(dummyUser.get(2).
                getFirstName()).lastName(dummyUser.get(2).getLastName()).email(dummyUser.get(2).getEmail()).password(dummyUser.get(2).getPassword()).expectedCalories(dummyUser.get(2).getExpectedCalories()).build();
        ResponseEntity<DTO.StatusResponseDTO> managerNormalToManagerResponse =  userController.updateUser(managerNormalToManager, managerSessionId,userId.get(3-1) );
        assertThat(managerNormalToManagerResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Admin changes Manager to Normal T
        DTO.UserUpdateRequestDTO adminManagerToNormal2 = DTO.UserUpdateRequestDTO.builder().role(0).firstName(dummyUser.get(2).
                getFirstName()).lastName(dummyUser.get(2).getLastName()).email(dummyUser.get(2).getEmail()).password(dummyUser.get(2).getPassword()).expectedCalories(dummyUser.get(2).getExpectedCalories()).build();
        ResponseEntity<DTO.StatusResponseDTO> adminManagerToNormal2Response =  userController.updateUser(adminManagerToNormal2, adminSessionId,userId.get(3-1));
            assertThat(adminManagerToNormal2Response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Normal changes Normal F
        DTO.UserUpdateRequestDTO normalToNormal = DTO.UserUpdateRequestDTO.builder().firstName("normalChanged").role(2).firstName(dummyUser.get(4).
                getFirstName()).lastName(dummyUser.get(4).getLastName()).email(dummyUser.get(4).getEmail()).password(dummyUser.get(4).getPassword()).expectedCalories(dummyUser.get(4).getExpectedCalories()).build();
        ResponseEntity<DTO.StatusResponseDTO> normalToNormalResponse =  userController.updateUser(normalToNormal, userSessionId,userId.get(5-1));
        assertThat(normalToNormalResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    }

    @Test
    public void deleteUsers() {
        userRepository.deleteAll();
        userRepository.flush();
        List<Long> userId = fillData();
        ArrayList<String> authenticationCodes = storeSessionIds();
        setRoles(authenticationCodes);
        String adminSessionId = authenticationCodes.get(0);
        String managerSessionId = authenticationCodes.get(1);
        String normalSessionId = authenticationCodes.get(2);

        // Manager deletes T
        ResponseEntity<DTO.StatusResponseDTO> managerDeletesSomeone = userController.deleteUser(managerSessionId, userId.get(5-1));
        assertThat(managerDeletesSomeone.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Normal deletes someone else F
        ResponseEntity<DTO.StatusResponseDTO> normalDeletesSomeone = userController.deleteUser(normalSessionId, userId.get(4-1));
        assertThat(normalDeletesSomeone.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        // Admin deletes T
        ResponseEntity<DTO.StatusResponseDTO> adminResponseDTO = userController.deleteUser(adminSessionId, userId.get(4-1));
        assertThat(adminResponseDTO.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        // Normal Deletes itself T
        ResponseEntity<DTO.StatusResponseDTO> normalDeletesItself = userController.deleteUser(normalSessionId, userId.get(3-1));
        assertThat(normalDeletesItself.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void getUsersAll() {
        List<User> users = getUsersWithDifferentRoles();
        List<String> sessionIds = storeSessionIds();
        assertThat(userController.getSelfRecords(
                sessionIds.get(0),"true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userController.getSelfRecords("wrong","true",0,10).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userController.getSelfRecords(
                sessionIds.get(1),"true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userController.getSelfRecords(
                sessionIds.get(2),"true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
    @Test
    public void getUsersNoQuery() {
        List<User> users = getUsersWithDifferentRoles();
        List<String> sessionIds = storeSessionIds();
        assertThat(userController.getSelfRecords(sessionIds.get(0),"true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void getUsersWithQuery() {
        List<User> users = getUsersWithDifferentRoles();
        List<String> sessionIds = storeSessionIds();
        assertThat(userController.getSelfRecords(sessionIds.get(4),"id gt " + users.get(0).getId(), 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userController.getSelfRecords("wrongsession","id gt " + users.get(0).getId(), 0, 10).getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userController.getSelfRecords(sessionIds.get(0),"fname neq '" + users.get(2).getFirstName() +"'",  0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }

    @Test
    public void getUsersWithQueryAndPagination() {
        List<User> users = getUsersWithDifferentRoles();
        List<String> sessionIds = storeSessionIds();
        assertThat(userController.getSelfRecords(sessionIds.get(4), "id gt "+users.get(0).getId(),1,10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userController.getSelfRecords(sessionIds.get(2), "id gt "+users.get(0).getId(),1,10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userController.getSelfRecords(sessionIds.get(0),"fname neq '" + users.get(2).getFirstName() +"'",  2, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }


}

