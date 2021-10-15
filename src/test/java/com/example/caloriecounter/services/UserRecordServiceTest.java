package com.example.caloriecounter.services;


import com.example.caloriecounter.AbstractTest;
import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Rollback(false)
@SpringBootTest
public class UserRecordServiceTest extends AbstractTest {

    @Test
    public void createDummyRecords(){
        flushDB();
        getUserRecordsDataForDifferentRoles();
    }

    @Test
    public void processUserRecordsHappyCase() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecord(users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId()).getMeal())
                .isEqualTo(users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getMeal());
    }

    @Test
    public void processUserRecordsHappyCase2() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.deleteUserRecord(users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId(),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserRecordsUserDeletingOtherUserRecord() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.deleteUserRecord(users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId(),users.get(1)).getCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void processUserRecordsManagerDeletingOtherUserRecord() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.deleteUserRecord(users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId(),users.get(2)).getCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void processUserRecordsAdminDeletingOtherUserRecord() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.deleteUserRecord(users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId(),users.get(4)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserRecordDeleteingRecordWhichDoesNotExist() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThatThrownBy(()->userRecordService.deleteUserRecord(9999L,users.get(1)).getCode()).isInstanceOf(ValidationException.class);
    }

    @Test
    public void processUserRecordsFetchWithoutQuery() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.getUserRecords("true",0,50,users.get(2)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.getUserRecords("true",0,50,users.get(4)).getUserRecordResponseDTOList().size()).isEqualTo(24);
    }

    @Test
    public void processUserRecordsFetchWithQuery() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("calories gt 400",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(1);
        assertThat(userRecordService.getUserRecords("calories gt 400",0,50,users.get(2)).getUserRecordResponseDTOList().size()).isEqualTo(1);
        assertThat(userRecordService.getUserRecords("calories gt 400",0,50,users.get(4)).getUserRecordResponseDTOList().size()).isEqualTo(6);
    }

    @Test
    public void processUserRecordsFetchWithQuerWithPagination() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("expected_cals lt 2300",2,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(0);
        assertThat(userRecordService.getUserRecords("expected_cals lt 2300",2,50,users.get(2)).getUserRecordResponseDTOList().size()).isEqualTo(0);
        assertThat(userRecordService.getUserRecords("expected_cals lt 2300",2,50,users.get(4)).getUserRecordResponseDTOList().size()).isEqualTo(10);
    }

    @Test
    public void processUserReportFetchWithoutQuery() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecordsReport("true",0,10,users.get(0)).getDayWiseStats().size()).isEqualTo(2);
        assertThat(userRecordService.getUserRecordsReport("true",0,50,users.get(2)).getDayWiseStats().size()).isEqualTo(2);
        assertThat(userRecordService.getUserRecordsReport("true",0,50,users.get(4)).getDayWiseStats().size()).isEqualTo(2);
    }

    @Test
    public void processUserReportFetchWithQuery() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecordsReport("calories gt 400",0,10,users.get(0)).getDayWiseStats().size()).isEqualTo(1);
        assertThat(userRecordService.getUserRecordsReport("calories gt 400",0,50,users.get(2)).getDayWiseStats().size()).isEqualTo(1);
        assertThat(userRecordService.getUserRecordsReport("calories gt 400",0,50,users.get(4)).getDayWiseStats().size()).isEqualTo(1);
    }

    @Test
    public void processUserReportFetchWithQuerWithPagination() throws ValidationException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecordsReport("expected_cals lt 2300",2,10,users.get(0)).getDayWiseStats().size()).isEqualTo(0);
        assertThat(userRecordService.getUserRecordsReport("expected_cals lt 2300",2,50,users.get(2)).getDayWiseStats().size()).isEqualTo(0);
        assertThat(userRecordService.getUserRecordsReport("expected_cals lt 2300",2,50,users.get(4)).getDayWiseStats().size()).isEqualTo(1);
    }

    @Test
    public void processUserRecordsHappyCaseSaveRecordWithCalories() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.addUserRecord(DTO.UserRecordRequestDTO.builder().dateTime(new Date()).calories(200).meal("beans").build(),users.get(0),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(5);
    }

    @Test
    public void processUserRecordsHappyCaseSaveRecordWithoutCalories() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.addUserRecord(DTO.UserRecordRequestDTO.builder().dateTime(new Date()).meal("beans").build(),users.get(0),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        Thread.sleep(2000);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(5);
    }

    @Test
    public void processUserRecordsUserSavingRecordOfAnotherUser() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.addUserRecord(DTO.UserRecordRequestDTO.builder().dateTime(new Date()).meal("beans").build(),users.get(1),users.get(0)).getCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
    }

    @Test
    public void processUserRecordsManagerSavingRecordOfAnotherUser() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.addUserRecord(DTO.UserRecordRequestDTO.builder().dateTime(new Date()).meal("beans").build(),users.get(1),users.get(0)).getCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
    }

    @Test
    public void processUserRecordsHappyCaseAdminSavingRecordOfAnotherUser() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.addUserRecord(DTO.UserRecordRequestDTO.builder().dateTime(new Date()).meal("beans").build(),users.get(4),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        Thread.sleep(2000);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(5);
    }

    @Test
    public void processUserRecordsHappyCaseAdminSavingRecordOfAnotherNewUser() throws ValidationException, InterruptedException {
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(0);
        assertThat(userRecordService.addUserRecord(DTO.UserRecordRequestDTO.builder().dateTime(new Date()).meal("beans").build(),users.get(4),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        Thread.sleep(2000);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(1);
    }

    @Test
    public void processUserRecordsHappyCaseSaveRecordWithoutCaloriesNutritionixCalsNotFound() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        Calendar currentTimeNow = Calendar.getInstance();
        currentTimeNow.add(Calendar.DATE, 3);
        Date day = currentTimeNow.getTime();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.addUserRecord(DTO.UserRecordRequestDTO.builder().dateTime(day).meal("aacxasdvzserzgzd").build(),users.get(0),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        Thread.sleep(2000);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().get(4).getCalories()).isEqualTo(250);
    }


    @Test
    public void processUserRecordsHappyCaseUpdateRecordWithCalories() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.updateUserRecord(DTO.UserRecordRequestDTO.builder().recordId(users.get(0).getDailyExpectedCaloriesDataList().get(0)
        .getUserRecords().get(0).getId()).dateTime(new Date()).calories(200).meal("newmeal").build(),users.get(0),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().stream()
                .anyMatch(userRecordResponseDTO -> userRecordResponseDTO.getMeal().equals("newmeal"))).isTrue();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
    }

    @Test
    public void processUserRecordsHappyCaseUpdateRecordWithoutCalories() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.updateUserRecord(DTO.UserRecordRequestDTO.builder().recordId(users.get(0).getDailyExpectedCaloriesDataList().get(0)
                .getUserRecords().get(0).getId()).dateTime(new Date()).meal("pasta").build(),users.get(0),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        Thread.sleep(2000);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().stream()
                .anyMatch(userRecordResponseDTO -> userRecordResponseDTO.getMeal().equals("pasta"))).isTrue();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
    }

    @Test
    public void processUserRecordsUserUpdatingRecordOfAnotherUser() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.updateUserRecord(DTO.UserRecordRequestDTO.builder().recordId(users.get(0).getDailyExpectedCaloriesDataList().get(0)
                .getUserRecords().get(0).getId()).dateTime(new Date()).meal("pasta").build(),users.get(1),users.get(0)).getCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
    }

    @Test
    public void processUserRecordsManagerUpdatingRecordOfAnotherUser() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.updateUserRecord(DTO.UserRecordRequestDTO.builder().recordId(users.get(0).getDailyExpectedCaloriesDataList().get(0)
                .getUserRecords().get(0).getId()).dateTime(new Date()).meal("pasta").build(),users.get(2),users.get(0)).getCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
    }

    @Test
    public void processUserRecordsHappyCaseAdminUpdatingRecordOfAnotherUser() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.updateUserRecord(DTO.UserRecordRequestDTO.builder().recordId(users.get(0).getDailyExpectedCaloriesDataList().get(0)
                .getUserRecords().get(0).getId()).dateTime(new Date()).meal("pasta").build(),users.get(4),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        Thread.sleep(2000);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().stream()
                .anyMatch(userRecordResponseDTO -> userRecordResponseDTO.getMeal().equals("pasta"))).isTrue();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
    }

    @Test
    public void processUserRecordsHappyCaseAdminSavingRecordwhichDoesNotExist() throws ValidationException, InterruptedException {
        List<User> users = getUsersWithDifferentRoles();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(0);
        assertThat(userRecordService.updateUserRecord(DTO.UserRecordRequestDTO.builder().recordId(999L)
                .dateTime(new Date()).meal("pasta").build(),users.get(4),users.get(0)).getCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void processUserRecordsHappyCaseUpdateRecordWithoutCaloriesNutritionixCalsNotFound() throws ValidationException, InterruptedException {
        List<User> users = getUserRecordsDataForDifferentRoles();
        Calendar currentTimeNow = Calendar.getInstance();
        currentTimeNow.add(Calendar.DATE, 3);
        Date day = currentTimeNow.getTime();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().size()).isEqualTo(4);
        assertThat(userRecordService.updateUserRecord(DTO.UserRecordRequestDTO.builder().recordId(users.get(0).getDailyExpectedCaloriesDataList().get(0)
                .getUserRecords().get(0).getId()).dateTime(new Date()).meal("asddzfsfdfx").build(),users.get(4),users.get(0)).getCode())
                .isEqualTo(HttpStatus.ACCEPTED);
        Thread.sleep(2000);
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().stream()
                .anyMatch(userRecordResponseDTO -> userRecordResponseDTO.getMeal().equals("asddzfsfdfx"))).isTrue();
        assertThat(userRecordService.getUserRecords("true",0,10,users.get(0)).getUserRecordResponseDTOList().stream()
                .anyMatch(userRecordResponseDTO -> userRecordResponseDTO.getCalories().equals(250))).isTrue();
    }

}
