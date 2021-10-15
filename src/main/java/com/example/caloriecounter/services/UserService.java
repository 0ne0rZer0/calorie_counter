package com.example.caloriecounter.services;

import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.constants.CommonConstants;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.enums.Roles;
import com.example.caloriecounter.models.DailyExpectedCaloriesData;
import com.example.caloriecounter.models.User;
import com.example.caloriecounter.repos.DailyExpectedCaloriesRepository;
import com.example.caloriecounter.repos.UserRepository;
import com.example.caloriecounter.utils.QueryUtils;
import org.springframework.http.HttpStatus;
import com.example.caloriecounter.dtos.Converter;
import lombok.NonNull;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

import static com.example.caloriecounter.constants.CommonConstants.*;

@Service
public class UserService {
    final UserRepository userRepository;
    final AuthService authService;
    final EntityManager entityManager;
    final DailyExpectedCaloriesRepository dailyExpectedCaloriesRepository;

    public UserService(UserRepository userRepository, AuthService authService,
                       EntityManager entityManager, DailyExpectedCaloriesRepository dailyExpectedCaloriesRepository) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.entityManager = entityManager;
        this.dailyExpectedCaloriesRepository = dailyExpectedCaloriesRepository;
    }

    public DTO.StatusResponseDTO processSignUpRequest(@NonNull DTO.UserSignUpRequestDTO userSignUpRequestDTO){
        if(Objects.nonNull(userSignUpRequestDTO.getExpectedCalories()) && userSignUpRequestDTO.getExpectedCalories() < 0){
            return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(CALS_NON_POSITIVE).build();
        }
        User userinDB = userRepository.findByEmail(userSignUpRequestDTO.getEmail());
        if(Objects.nonNull(userinDB)){
            return DTO.StatusResponseDTO.builder().msg(EMAIL_EXISTS)
                    .code(HttpStatus.BAD_REQUEST).build();
        }
        User user = Converter.convertUserSignUpRequestDTOToUser(userSignUpRequestDTO);
        user.setPasswordHash(authService.securePassword(userSignUpRequestDTO.getPassword()));
        user = userRepository.save(user);
        return DTO.StatusResponseDTO.builder().msg(SIGNUP_SUCCESS + " id = " + user.getId())
                .code(HttpStatus.ACCEPTED).build();
    }

    public ResponseEntity<DTO.StatusResponseDTO> processSignInRequest(@NonNull DTO.UserSignInRequestDTO userSignInRequestDTO){
        User userinDB = userRepository.findByEmail(userSignInRequestDTO.getEmail());
        if(!Objects.nonNull(userinDB) || !authService.authenticatePassword(userSignInRequestDTO.getPassword(),
                userinDB.getPasswordHash())){
            System.out.println("fail for " + userSignInRequestDTO.getEmail() );
            return ResponseEntity.badRequest().body(DTO.StatusResponseDTO
                    .builder().msg(EMAIL_PSWD_NO_MATCH).code(HttpStatus.BAD_REQUEST).build());
        }
        userinDB.setSessionExpiryTime(authService.generateSessionExpiryDate());
        userinDB.setSessionId(authService.generateSessionID());
        userinDB = userRepository.save(userinDB);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(CommonConstants.AUTH_KEY, userinDB.getSessionId());
        return  new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(LOGIN_SUCCESS + " id = " + userinDB.getId())
                .code(HttpStatus.ACCEPTED).build(), httpHeaders, HttpStatus.ACCEPTED);
    }

    public DTO.StatusResponseDTO deleteUser(User recordUser, User currentUser) throws ValidationException {
        if((recordUser.getId().equals(currentUser.getId()) || (currentUser.getRole() >= recordUser.getRole() && currentUser.getRole() > 0))) {
            userRepository.deleteById(recordUser.getId());
            return DTO.StatusResponseDTO.builder().code(HttpStatus.ACCEPTED).msg(USER_DELETED).build();
        }
        return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(UNAUTHORISED).build();
    }

    public DTO.UserResponseListDTO getUserData(String queryString, Integer start, Integer size, User currentUser) throws ValidationException {
        System.out.println("queryString= " + queryString);
        queryString = QueryUtils.transformQueryForUsers(queryString, currentUser, start, size);
        System.out.println("query = " + queryString);
        Query query = entityManager.createNativeQuery(queryString, User.class);
        List<DTO.UserResponseDTO> userResponseDTOS = new ArrayList<>();
        List<User> users = new ArrayList<>();
        try{
            users = (List<User>)query.getResultList();
        }
        catch (Exception e){
            throw new ValidationException("invalid query gave error " + e.getMessage() , HttpStatus.BAD_REQUEST);
        }
        users.forEach(user->{
            userResponseDTOS.add(Converter.convertUserToUserResponseDTO(user));
        });
        return DTO.UserResponseListDTO.builder()
                .userResponseDTOList(userResponseDTOS).code(HttpStatus.ACCEPTED).msg(READ_REQUEST_PROCESSED).build();
    }

    public DTO.StatusResponseDTO updateUser(DTO.UserUpdateRequestDTO userSignUpRequestDTO,  User recordUser, User currentUser) {
        if(Objects.nonNull(userSignUpRequestDTO.getExpectedCalories()) && userSignUpRequestDTO.getExpectedCalories() < 0){
            return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(CALS_NON_POSITIVE).build();
        }
        if((recordUser.getId().equals(currentUser.getId()) || (currentUser.getRole() >= recordUser.getRole() && currentUser.getRole()>0))) {

            User user = Converter.convertUserUpdateRequestDTOToUser(userSignUpRequestDTO);
            recordUser.setFirstName(user.getFirstName());
            recordUser.setLastName(user.getLastName());
            recordUser.setEmail(user.getEmail());
            recordUser.setPasswordHash(authService.securePassword(userSignUpRequestDTO.getPassword()));

            if (Objects.nonNull(user.getRole()) && currentUser.getRole() > Roles.REGULAR_USER.ordinal()) {
                switch (currentUser.getRole()) {
                    case 2:
                        recordUser.setRole(user.getRole());
                        break;
                    case 1:
                        if(user.getRole() < 2) {
                            recordUser.setRole(user.getRole());
                        } else {
                            return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg("unauthorized operation").build();
                        }
                        break;
                    default:
                        return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(UNAUTHORISED).build();

                }
            }
            if(Objects.nonNull(user.getExpected_calories()) && user.getExpected_calories()!=recordUser.getExpected_calories()){
                recordUser.setExpected_calories(user.getExpected_calories());
                DailyExpectedCaloriesData dailyExpectedCaloriesData = dailyExpectedCaloriesRepository.findByDateAndUser_Id(new Date(), recordUser.getId());
                if(Objects.nonNull(dailyExpectedCaloriesData)) {
                    System.out.println("creating daily");
                    dailyExpectedCaloriesData.setExpected_calories(user.getExpected_calories());
                    dailyExpectedCaloriesData.setDay_within_expected_range(dailyExpectedCaloriesData.getExpected_calories() >= dailyExpectedCaloriesData.getTotal_calories());
                    dailyExpectedCaloriesRepository.save(dailyExpectedCaloriesData);
                }
            }
            recordUser = userRepository.save(recordUser);
            return DTO.StatusResponseDTO.builder().msg("update successful id = " + recordUser.getId())
                    .code(HttpStatus.ACCEPTED).build();
        }
        return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(UNAUTHORISED).build();
    }
}
