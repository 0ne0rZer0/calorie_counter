package com.example.caloriecounter.dtos;

import com.example.caloriecounter.models.User;
import com.example.caloriecounter.models.UserRecord;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Converter {
    public User convertUserSignUpRequestDTOToUser(DTO.UserSignUpRequestDTO userSignUpRequestDTO){
        return User.builder().email(userSignUpRequestDTO.getEmail())
                .expected_calories(userSignUpRequestDTO.getExpectedCalories())
                .firstName(userSignUpRequestDTO.getFirstName())
                .lastName(userSignUpRequestDTO.getLastName())
                .build();
    }
    public User convertUserUpdateRequestDTOToUser(DTO.UserUpdateRequestDTO userUpdateRequestDTO){
        return User.builder().email(userUpdateRequestDTO.getEmail())
                .expected_calories(userUpdateRequestDTO.getExpectedCalories())
                .firstName(userUpdateRequestDTO.getFirstName())
                .lastName(userUpdateRequestDTO.getLastName())
                .role(userUpdateRequestDTO.getRole())
                .build();
    }
    public User convertUserInRequestDTOToUser(DTO.UserSignInRequestDTO userSignInRequestDTO){
        return User.builder().email(userSignInRequestDTO.getEmail())
                .passwordHash(userSignInRequestDTO.getPassword())
                .build();
    }

    public UserRecord convertRecordRequestDTOToUserRecord(DTO.UserRecordRequestDTO userRecordRequestDTO){
        return UserRecord.builder()
                .id(userRecordRequestDTO.getRecordId())
                .calories(userRecordRequestDTO.getCalories())
                .meal(userRecordRequestDTO.getMeal())
                .mealDate(userRecordRequestDTO.getDateTime())
                .mealTime(userRecordRequestDTO.getDateTime())
                .build();
    }


    public DTO.UserRecordResponseDTO convertUserRecordRecordResponseDTO(UserRecord userRecord){
        return DTO.UserRecordResponseDTO.builder()
                .datetime(userRecord.getMealDate().toString() +"T"+userRecord.getMealTime().toString())
                .calories(userRecord.getCalories())
                .isWithinExpectedCalorieLimit(userRecord.getDailyExpectedCaloriesData().getDay_within_expected_range())
                .meal(userRecord.getMeal())
                .recordId(userRecord.getId())
                .userid(userRecord.getDailyExpectedCaloriesData().getUser().getId())
                .build();
    }

    public DTO.UserResponseDTO convertUserToUserResponseDTO(User user){
        return DTO.UserResponseDTO.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastNam(user.getLastName())
                .role(user.getRole())
                .expectedCalories(user.getExpected_calories())
                .build();
    }
}
