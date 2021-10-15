package com.example.caloriecounter;

import com.example.caloriecounter.controllers.UserController;
import com.example.caloriecounter.controllers.UserRecordController;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.models.DailyExpectedCaloriesData;
import com.example.caloriecounter.models.User;
import com.example.caloriecounter.models.UserRecord;
import com.example.caloriecounter.repos.DailyExpectedCaloriesRepository;
import com.example.caloriecounter.repos.UserRecordRepository;
import com.example.caloriecounter.repos.UserRepository;
import com.example.caloriecounter.services.AuthService;
import com.example.caloriecounter.services.UserRecordService;
import com.example.caloriecounter.services.UserService;
import com.example.caloriecounter.utils.AuthUtils;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import javax.xml.crypto.Data;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.*;

import static com.example.caloriecounter.constants.CommonConstants.AUTH_KEY;


public class AbstractTest {

    @Autowired
    public UserController userController;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public UserService userService;
    @Autowired
    public UserRecordRepository userRecordRepository;
    @Autowired
    public UserRecordService userRecordService;
    @Autowired
    public UserRecordController userRecordController;
    @Autowired
    public AuthService authService;

    @Autowired
    public DailyExpectedCaloriesRepository dailyExpectedCaloriesRepository;

    @AfterEach
    public void flushDB(){
        userRepository.deleteAll();
    }

    public ArrayList<String> getDummyData() {
        return new ArrayList<String>(List.of("User", "Test", "12345678", "user"));
    }

    public ArrayList<DTO.UserSignUpRequestDTO> getDummyUser() {
        ArrayList<DTO.UserSignUpRequestDTO> dummyUsers = new ArrayList<>();
        ArrayList<String> dummyData = getDummyData();
        for(int i = 1; i <= 6; i++) {
            dummyUsers.add(DTO.UserSignUpRequestDTO.builder().firstName(dummyData.get(0) + i)
                    .lastName(dummyData.get(1) + i).password(dummyData.get(2)).email(dummyData.get(3) + i + "@email.com").expectedCalories(2500).build());
        }
        return dummyUsers;
    }

    //Signup
    public List<Long> fillData() {
        ArrayList<DTO.UserSignUpRequestDTO> dummyUsers = getDummyUser();
        ArrayList<Long> userId = new ArrayList<>();
        for(int i = 1; i <= 6; i++) {
            ResponseEntity<DTO.StatusResponseDTO> statusResponseDTO = userController.signup(dummyUsers.get(i-1));
            userId.add(userRepository.findByEmail(dummyUsers.get(i-1).getEmail()).getId());
        }
        System.out.println("Signed up 6 users");
        return userId;
    }

    //Signin
    public ArrayList<String> storeSessionIds() {
        ArrayList<DTO.UserSignUpRequestDTO> dummyUsers = getDummyUser();
        ArrayList<String> authAccessToken = new ArrayList<>();
        for(int i = 1; i <= 6; i++) {
            ResponseEntity<DTO.StatusResponseDTO> statusResponseDTO = userController.signIn(DTO.UserSignInRequestDTO.builder().email(dummyUsers.get(i-1).getEmail()).password(dummyUsers.get(i-1).getPassword()).build());
            authAccessToken.add(statusResponseDTO.getHeaders().get(AUTH_KEY).get(0));
        }
        System.out.println("Signed in 6 users");
        return authAccessToken;
    }

    public void setRoles(ArrayList<String> authenticationCodes ) {
        // Creating dummy admins manager and user
        User userChangeToAdmin = userRepository.findBySessionId(authenticationCodes.get(0));
        userChangeToAdmin.setRole(2);
        userRepository.save(userChangeToAdmin);
        User userChangeToManager = userRepository.findBySessionId(authenticationCodes.get(1));
        userChangeToManager.setRole(1);
        userRepository.save(userChangeToManager);

    }
    public void makeAdmin(int index, ArrayList<String> authenticationCodes) {
        User userChangeToAdmin = userRepository.findBySessionId(authenticationCodes.get(index));
        userChangeToAdmin.setRole(2);
        userRepository.save(userChangeToAdmin);
    }
    public void makeManager(int index, ArrayList<String> authenticationCodes) {
        User userChangeToAdmin = userRepository.findBySessionId(authenticationCodes.get(index));
        userChangeToAdmin.setRole(1);
        userRepository.save(userChangeToAdmin);
    }
    public void makeNormal(int index, ArrayList<String> authenticationCodes) {
        User userChangeToAdmin = userRepository.findBySessionId(authenticationCodes.get(index));
        userChangeToAdmin.setRole(0);
        userRepository.save(userChangeToAdmin);
    }
    public void createRoles(ArrayList<String> authenticationCodes) {
        makeAdmin(0, authenticationCodes);
        makeManager(1, authenticationCodes);
        makeNormal(2, authenticationCodes);
        makeAdmin(3, authenticationCodes);
        makeManager(4, authenticationCodes);
        makeNormal(5, authenticationCodes);

    }

    public List<User> getUsersWithDifferentRoles(){
        List<User> users = new ArrayList<>();
        ArrayList<String> dummyData = getDummyData();
        for(int i = 1; i <= 6; i++) {
            DTO.UserSignUpRequestDTO userSignUpRequestDTO = DTO.UserSignUpRequestDTO.builder().firstName(dummyData.get(0) + i)
                    .lastName(dummyData.get(1) + i).password(dummyData.get(2)).email(dummyData.get(3) + i + "@email.com").expectedCalories(1200 + (i*300)).build();
            userService.processSignUpRequest(userSignUpRequestDTO);
            User user = userRepository.findByEmail(userSignUpRequestDTO.getEmail());
            user.setRole((i-1)/2); //1 2 - regular // 3 4 - manager // 5 6 - admin
            user = userRepository.save(user);
            users.add(user);
        }
        return users;
    }

    public List<User> getUserRecordsDataForDifferentRoles(){
        List<User> users = getUsersWithDifferentRoles();
        List<User> newUserList = new ArrayList<>();
        List<UserRecord> userRecords = new ArrayList<>();
        for(int i = 0; i < 6; i++){
            Date day_1 = new Date();
            UserRecord ur11 = UserRecord.builder()
                    .calories(200)
                    .meal("bread")
                    .mealDate(day_1)
                    .mealTime(day_1)
                    .build();
            UserRecord ur12 = UserRecord.builder()
                    .calories(500)
                    .meal("cheese")
                    .mealDate(day_1)
                    .mealTime(day_1)
                    .build();
            Calendar currentTimeNow = Calendar.getInstance();
            currentTimeNow.add(Calendar.DATE, 1);
            Date day_2 = currentTimeNow.getTime();
            UserRecord ur21 = UserRecord.builder()
                    .calories(300)
                    .meal("chips")
                    .mealDate(day_2)
                    .mealTime(day_2)
                    .build();
            UserRecord ur22 = UserRecord.builder()
                    .calories(100)
                    .meal("rice")
                    .mealDate(day_2)
                    .mealTime(day_2)
                    .build();
            DailyExpectedCaloriesData d1 = DailyExpectedCaloriesData.builder()
                    .date(ur11.getMealDate())
                    .userRecords(Arrays.asList(ur11, ur12))
                    .expected_calories(2200)
                    .day_within_expected_range(true)
                    .total_calories(700)
                    .user(users.get(i))
                    .build();
            DailyExpectedCaloriesData d2 = DailyExpectedCaloriesData.builder()
                    .date(ur22.getMealDate())
                    .userRecords(Arrays.asList(ur21, ur22))
                    .expected_calories(2400)
                    .day_within_expected_range(true)
                    .total_calories(400)
                    .user(users.get(i))
                    .build();
            ur11.setDailyExpectedCaloriesData(d1);
            ur12.setDailyExpectedCaloriesData(d1);
            ur21.setDailyExpectedCaloriesData(d2);
            ur22.setDailyExpectedCaloriesData(d2);
            users.get(i).setDailyExpectedCaloriesDataList(Arrays.asList(d1,d2));
            users.get(i).setSessionId("uniq"+i);
            users.get(i).setSessionExpiryTime(day_2);
            dailyExpectedCaloriesRepository.save(d1);
            dailyExpectedCaloriesRepository.save(d2);
            userRecordRepository.save(ur11);
            userRecordRepository.save(ur12);
            userRecordRepository.save(ur21);
            userRecordRepository.save(ur22);
            newUserList.add(userRepository.save(users.get(i)));
        }
        return newUserList;
    }
}
