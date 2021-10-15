package com.example.caloriecounter.controllers;

import com.example.caloriecounter.AbstractTest;
import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.Rollback;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@Rollback(false)
@SpringBootTest
public class UserRecordControllerTest extends AbstractTest {
    @Test
    public void processUserRecordsHappyCase()  {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.getUserRecord(users.get(0).getDailyExpectedCaloriesDataList()
                .get(0).getUserRecords().get(0).getId()).getStatusCode())
                .isEqualTo(HttpStatus.OK);
    }

    @Test
    public void processUserRecordsHappyCase2() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.deleteSelfRecord(users.get(0).getSessionId(),users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId()).getStatusCode())
                .isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserRecordsUserDeletingOtherUserRecord() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.deleteSelfRecord(users.get(1).getSessionId(), users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void processUserRecordsManagerDeletingOtherUserRecord()  {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.deleteSelfRecord(users.get(2).getSessionId(), users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId()).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    public void processUserRecordsAdminDeletingOtherUserRecord()  {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.deleteSelfRecord(users.get(4).getSessionId(),users.get(0).getDailyExpectedCaloriesDataList().get(0).getUserRecords().get(0).getId()).getStatusCode())
                .isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserRecordDeletingRecordWhichDoesNotExist() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.deleteSelfRecord(users.get(1).getSessionId(),9999L).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void processUserRecordsFetchWithoutQuery() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.getSelfRecords(users.get(0).getSessionId(),"true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getSelfRecords(users.get(2).getSessionId(),"true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getSelfRecords(users.get(4).getSessionId(),"true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserRecordsFetchWithQuery() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.getSelfRecords(users.get(0).getSessionId(),"calories gt 400", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getSelfRecords(users.get(2).getSessionId(),"calories gt 400", 0, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getSelfRecords(users.get(4).getSessionId(),"calories gt 400", 0, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserRecordsFetchWithQueryWithPagination() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.getSelfRecords(users.get(0).getSessionId(),"expected_cals lt 2300", 2, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getSelfRecords(users.get(2).getSessionId(),"expected_cals lt 2300", 2, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getSelfRecords(users.get(4).getSessionId(),"expected_cals lt 2300", 2, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
    @Test
    public void processUserReportFetchWithoutQuery() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.getReport(users.get(0).getSessionId(), "true", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getReport(users.get(2).getSessionId(), "true", 0, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getReport(users.get(4).getSessionId(), "true", 0, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserReportFetchWithQuery()  {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.getReport(users.get(0).getSessionId(), "calories gt 400", 0, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getReport(users.get(2).getSessionId(), "calories gt 400", 0, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getReport(users.get(4).getSessionId(), "calories gt 400", 0, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    public void processUserReportFetchWithQuerWithPagination() {
        List<User> users = getUserRecordsDataForDifferentRoles();
        assertThat(userRecordController.getReport(users.get(0).getSessionId(), "expected_cals lt 2300", 1, 10).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getReport(users.get(2).getSessionId(), "expected_cals lt 2300", 2, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(userRecordController.getReport(users.get(4).getSessionId(), "expected_cals lt 2300", 2, 50).getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    }
}
