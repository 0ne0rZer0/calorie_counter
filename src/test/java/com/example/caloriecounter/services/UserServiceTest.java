package com.example.caloriecounter.services;

import com.example.caloriecounter.AbstractTest;
import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Rollback(false)
@SpringBootTest
public class UserServiceTest extends AbstractTest {

    @Test
    public void processSignUpTestHappyCase(){
        DTO.UserSignUpRequestDTO userSignUpRequestDTO = DTO.UserSignUpRequestDTO.builder().email("sample@user.com")
                .password("password!!")
                .firstName("mask")
                .lastName("man")
                .build();
        assertThat(userService.processSignUpRequest(userSignUpRequestDTO).getCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processSignUpTestEmailAlreadyExists(){
        DTO.UserSignUpRequestDTO userSignUpRequestDTO = DTO.UserSignUpRequestDTO.builder().email("sample@user.com")
                .password("password!!")
                .firstName("mask")
                .lastName("man")
                .build();
        userService.processSignUpRequest(userSignUpRequestDTO);
        assertThat(userService.processSignUpRequest(userSignUpRequestDTO).getCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void processSignInHappyTestCase(){
        List<Long> ids = fillData();
        User user = userRepository.findById(ids.get(0)).get();

        DTO.UserSignInRequestDTO userSignInRequestDTO = DTO.UserSignInRequestDTO.builder()
                .email(user.getEmail())
                .password("12345678")
                .build();
        assertThat(userService.processSignInRequest(userSignInRequestDTO).getBody().getCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processSignInUserDoesNotExist(){
        DTO.UserSignInRequestDTO userSignInRequestDTO = DTO.UserSignInRequestDTO.builder()
                .email("randomuser123@gmail.com")
                .password("12345678")
                .build();
        assertThat(Objects.requireNonNull(userService.processSignInRequest(userSignInRequestDTO).getBody()).getCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void processSignInUserPasswordIncorrect(){
        List<Long> ids = fillData();
        User user = userRepository.findById(ids.get(0)).get();
        DTO.UserSignInRequestDTO userSignInRequestDTO = DTO.UserSignInRequestDTO.builder()
                .email(user.getEmail())
                .password("abcdefghij")
                .build();
        assertThat(userService.processSignInRequest(userSignInRequestDTO).getBody().getCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void deleteUserRegularUserDeletingAnotherUser() throws ValidationException {
        List<Long> ids = fillData();
        assertThat(userService.deleteUser(userRepository.findById(ids.get(0)).get(),
                userRepository.findById(ids.get(1)).get()).getCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void deleteUserAdminDeletingAnotherRegularUser() throws ValidationException {
        List<Long> ids = fillData();
        User admin = userRepository.findById(ids.get(0)).get();
        admin.setRole(2);
        admin = userRepository.save(admin);
        assertThat(userService.deleteUser(userRepository.findById(ids.get(1)).get(),
                admin).getCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void deleteUserRegularUserDeletingSelf() throws ValidationException {
        List<Long> ids = fillData();
        assertThat(userService.deleteUser(userRepository.findById(ids.get(0)).get(),
                userRepository.findById(ids.get(0)).get()).getCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void getusersTestNoQuery() throws ValidationException {
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userService.getUserData("true",0,10, users.get(0)).getUserResponseDTOList().size()).isEqualTo(1);
    }

    @Test
    public void getusersFetchingMultipleUsers() throws ValidationException {
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userService.getUserData("true",0,10, users.get(0)).getUserResponseDTOList().size()).isEqualTo(1);
        assertThat(userService.getUserData("true",0,10, users.get(2)).getUserResponseDTOList().size()).isEqualTo(4);
        assertThat(userService.getUserData("true",0,10, users.get(4)).getUserResponseDTOList().size()).isEqualTo(6);
    }

    @Test
    public void getusersWithQuery() throws ValidationException {
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userService.getUserData("id gt "+users.get(0).getId(),0,10, users.get(4)).getUserResponseDTOList().size()).isEqualTo(5);
        assertThat(userService.getUserData("fname neq '"+users.get(2).getFirstName()+"'",0,10, users.get(4)).getUserResponseDTOList().size()).isEqualTo(5);
    }

    @Test
    public void getusersWithQueryAndPagination() throws ValidationException {
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userService.getUserData("id gt "+users.get(0).getId(),1,10, users.get(4)).getUserResponseDTOList().size()).isEqualTo(4);
        assertThat(userService.getUserData("fname neq '"+users.get(2).getFirstName()+"'",2,10, users.get(4)).getUserResponseDTOList().size()).isEqualTo(3);
    }

    @Test
    public void processUpdateTestMangerTryingToUpdateAdmin(){
        DTO.UserUpdateRequestDTO userUpdateRequestDTO = DTO.UserUpdateRequestDTO.builder().email("sample@user.com")
                .password("password!!")
                .firstName("mask")
                .lastName("man")
                .expectedCalories(2200)
                .role(1)
                .build();
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userService.updateUser(userUpdateRequestDTO,users.get(5),users.get(3)).getCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void processUpdateTestMangerTryingToUpdateManager(){
        DTO.UserUpdateRequestDTO userUpdateRequestDTO = DTO.UserUpdateRequestDTO.builder().email("sample@user.com")
                .password("password!!")
                .firstName("mask")
                .lastName("man")
                .expectedCalories(2200)
                .role(1)
                .build();
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userService.updateUser(userUpdateRequestDTO,users.get(2),users.get(3)).getCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUpdateTestAdminTryingToUpdateManager(){
        DTO.UserUpdateRequestDTO userUpdateRequestDTO = DTO.UserUpdateRequestDTO.builder().email("sample@user.com")
                .password("password!!")
                .firstName("mask")
                .lastName("man")
                .expectedCalories(2200)
                .role(1)
                .build();
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userService.updateUser(userUpdateRequestDTO,users.get(2),users.get(4)).getCode()).isEqualTo(HttpStatus.ACCEPTED);
    }


}
