package com.example.caloriecounter.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.List;
import java.util.Map;


public class DTO {
    @Data
    @Builder
    public static class UserSignInRequestDTO {
        private String email, password;
    }

    @Builder
    @AllArgsConstructor
    @Data
    public static class StatusResponseDTO {
        String msg;
        HttpStatus code;
    }

    @Data
    @Builder
    public static class UserSignUpRequestDTO {
        private String firstName, lastName, password, email;
        private Integer expectedCalories;
    }

    @Data
    @Builder
    public static class UserUpdateRequestDTO {
        private String firstName, lastName, password, email;
        private Integer expectedCalories, role;
    }

    @Data
    @Builder
    public static class UserResponseDTO {
        private String firstName, lastNam, email;
        private Integer expectedCalories, role;
        private Long userId;
    }

    @Data
    @Builder
    public static class UserRecordRequestDTO {
        private Long recordId;
        private String meal;
        private Date dateTime;
        private Integer calories;
    }

    @Data
    @Builder
    public static class UserRecordResponseDTO {
        private Long recordId, userid;
        private String meal;
        private String datetime;
        private Integer calories;
        private Boolean isWithinExpectedCalorieLimit;
    }



    @Data
    @Builder
    public static class UserReportDTO {
        private String msg;
        private HttpStatus code;
        private Integer numberOfDays;
        private Integer totalNumberOfMeals;
        private Integer totalCalories;
        private Float avgCaloriesConsumedPerDay;
        private Float avgNumberOfMealsPerDay;
        private Float cumilativeCalorieIntakeToExpectedPercentage;
        private Map<String ,UserRecordDayStats> dayWiseStats;
    }

    @Data
    @Builder
    public static class UserRecordDayStats {
        private Date date;
        private Integer totalCalories;
        private Integer avgCaloriesConsumedPerMeal;
        private Integer numberOfMeals;
        private Integer expectedCalories;
        private Boolean isWithinExpectedCalorieLimit;
    }

    @Data
    @Builder
    public static class UserRecordResponseListDTO {
        private List<UserRecordResponseDTO> userRecordResponseDTOList;
        String msg;
        HttpStatus code;
    }

    @Data
    @Builder
    public static class UserResponseListDTO {
        private List<UserResponseDTO> userResponseDTOList;
        String msg;
        HttpStatus code;
    }

}
