package com.example.caloriecounter.services;

import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.dtos.Converter;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.enums.Roles;
import com.example.caloriecounter.models.DailyExpectedCaloriesData;
import com.example.caloriecounter.models.User;
import com.example.caloriecounter.models.UserRecord;
import com.example.caloriecounter.repos.DailyExpectedCaloriesRepository;
import com.example.caloriecounter.repos.UserRecordRepository;
import com.example.caloriecounter.utils.QueryUtils;
import org.hibernate.tool.schema.internal.exec.ScriptTargetOutputToFile;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

import static com.example.caloriecounter.constants.CommonConstants.*;

@Service
public class UserRecordService {

    final UserRecordRepository userRecordRepository;
//    final UserRepository userRepository;
    final UserService userService;
    final AuthService authService;
    final EntityManager entityManager;
    final DailyExpectedCaloriesRepository dailyExpectedCaloriesRepository;

    public UserRecordService( UserRecordRepository userRecordRepository, UserService userService, AuthService authService,
            EntityManager entityManager, DailyExpectedCaloriesRepository dailyExpectedCaloriesRepository) {
        this.userRecordRepository = userRecordRepository;
        this.userService = userService;
        this.authService = authService;
        this.entityManager = entityManager;
        this.dailyExpectedCaloriesRepository = dailyExpectedCaloriesRepository;
    }

    public DTO.UserRecordResponseListDTO getUserRecords(String queryString, Integer start, Integer size, User currentUser) throws ValidationException {
        System.out.println("queryString = " + queryString);
        queryString = QueryUtils.transformQueryForUserRecords(queryString, currentUser, start, size);
        System.out.println("query = " + queryString);
        Query query = entityManager.createNativeQuery(queryString, UserRecord.class);
        List<DTO.UserRecordResponseDTO> userRecordResponseDTOList = new ArrayList<>();
        ((List<UserRecord>)query.getResultList()).forEach(userRecord->{
            userRecordResponseDTOList.add(Converter.convertUserRecordRecordResponseDTO(userRecord));
        });
        return DTO.UserRecordResponseListDTO.builder()
                .userRecordResponseDTOList(userRecordResponseDTOList).code(HttpStatus.ACCEPTED).msg(READ_REQUEST_PROCESSED).build();
    }

    public UserRecord getUserRecord(Long id) throws ValidationException {
        UserRecord userRecord = userRecordRepository.findById(id).orElse(null);
        if(Objects.nonNull(userRecord) && authService.authenticateUser(userRecord.getDailyExpectedCaloriesData().getUser().getSessionId())!=null) {
            return userRecord;
        }
        return null;
    }

    public DTO.StatusResponseDTO addUserRecord(DTO.UserRecordRequestDTO userRecordRequestDTO,
                                               User currentUser,
                                               User recordUser) {
        if((recordUser.getId().equals(currentUser.getId()) || currentUser.getRole() == Roles.ADMIN.ordinal())){
            if(Objects.isNull(userRecordRequestDTO.getCalories())){
                System.out.println("no cals. calling nutritionix " + Thread.currentThread().getName());
                new Thread(() -> {
                    callNutrionix(userRecordRequestDTO,currentUser,recordUser);
                }).start();
                return DTO.StatusResponseDTO.builder().code(HttpStatus.ACCEPTED).msg("cals finding").build();
            }
            if(userRecordRequestDTO.getCalories() < 0){
                return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(CALS_NON_POSITIVE).build();
            }
            UserRecord userRecord = Converter.convertRecordRequestDTOToUserRecord(userRecordRequestDTO);
            DailyExpectedCaloriesData dailyExpectedCaloriesData = dailyExpectedCaloriesRepository
                    .findByDateAndUser_Id(userRecord.getMealDate(), recordUser.getId());
            if(Objects.isNull(dailyExpectedCaloriesData)) {
                System.out.println("creating daily");
                dailyExpectedCaloriesData = DailyExpectedCaloriesData.builder()
                        .user(recordUser)
                        .date(userRecord.getMealDate())
                        .expected_calories(recordUser.getExpected_calories())
                        .total_calories(0)
                        .build();
            }
            dailyExpectedCaloriesData.setTotal_calories(dailyExpectedCaloriesData.getTotal_calories() + userRecord.getCalories());
            dailyExpectedCaloriesData.setDay_within_expected_range(checkCalorieWithinLimit(dailyExpectedCaloriesData));
            System.out.println("dto record id " + userRecordRequestDTO.getRecordId());
            System.out.println("record id " + userRecord.getId());
            if(Objects.nonNull(userRecord.getId()) && Objects.nonNull(dailyExpectedCaloriesData.getUserRecords())){
                UserRecord ur = dailyExpectedCaloriesData.getUserRecords().stream().filter(userRecord1 -> userRecord.getId().equals(userRecord1.getId())).findAny().orElse(null);
                if(Objects.nonNull(ur)){
                    dailyExpectedCaloriesData.getUserRecords().remove(ur);
                }
            }
            dailyExpectedCaloriesRepository.save(dailyExpectedCaloriesData);
            userRecord.setDailyExpectedCaloriesData(dailyExpectedCaloriesData);
            userRecordRepository.save(userRecord);
            System.out.println("record added uid " + userRecord.getDailyExpectedCaloriesData().getUser().getId() + " recid " + userRecord.getId());
            return DTO.StatusResponseDTO.builder().code(HttpStatus.ACCEPTED).msg(RECORD_ADDED).build();
        }
        return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(UNAUTHORISED).build();
    }

    private void callNutrionix(DTO.UserRecordRequestDTO userRecordRequestDTO, User currentUser, User recordUser) {
        System.out.println(Thread.currentThread().getName());
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request =
                new HttpEntity<String>(NUTRITIONIX_REQ_PREFIX + NUTRITIONIX_APP_KEY + NUTRITIONIX_MIDDLE +
                        userRecordRequestDTO.getMeal()+ NUTRITIONIX_SUFFIX, httpHeaders);
        JSONObject response = new JSONObject(restTemplate.postForObject(NUTRITIONIX_URL, request, String.class));
        if (response.getInt(TOTAL) == 0){
            System.out.println("cals not found from nutritionix");
            userRecordRequestDTO.setCalories(250);
        }
        else{
            userRecordRequestDTO.setCalories(response.getJSONArray(HITS).getJSONObject(0)
                    .getJSONObject(FIELDS).getInt(NF_CALORIES));
        }
        addUserRecord(userRecordRequestDTO, currentUser, recordUser);
    }

    private Boolean checkCalorieWithinLimit(DailyExpectedCaloriesData dailyExpectedCaloriesData) {
        return dailyExpectedCaloriesData.getExpected_calories() >= dailyExpectedCaloriesData.getTotal_calories();
    }

    public DTO.StatusResponseDTO deleteUserRecord(Long recordId,
                                                  User currentUser) throws ValidationException {
        Optional<UserRecord> userRecordOptional = userRecordRepository.findById(recordId);
        if(userRecordOptional.isEmpty()){
            throw new ValidationException(USER_RECORD_DOES_NOT_EXISTS, HttpStatus.BAD_REQUEST);
        }
        User recordUser = userRecordOptional.get().getDailyExpectedCaloriesData().getUser();
        if((recordUser.getId().equals(currentUser.getId()) || currentUser.getRole() == Roles.ADMIN.ordinal())){
            DailyExpectedCaloriesData dailyExpectedCaloriesData =
                    dailyExpectedCaloriesRepository.findByDateAndUser_Id(userRecordOptional.get().getMealDate(), recordUser.getId());
            dailyExpectedCaloriesData.setTotal_calories(dailyExpectedCaloriesData.getTotal_calories() - userRecordOptional.get().getCalories());
            dailyExpectedCaloriesData.setDay_within_expected_range(checkCalorieWithinLimit(dailyExpectedCaloriesData));
            dailyExpectedCaloriesRepository.save(dailyExpectedCaloriesData);
            userRecordRepository.delete(userRecordOptional.get());
            return DTO.StatusResponseDTO.builder().code(HttpStatus.ACCEPTED).msg(RECORD_DELETED).build();
        }
        return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(UNAUTHORISED).build();
    }

    public DTO.StatusResponseDTO updateUserRecord(DTO.UserRecordRequestDTO userRecordRequestDTO,
                                    User currentUser,
                                    User recordUser) throws ValidationException {
        if(Objects.nonNull(userRecordRequestDTO.getCalories()) && userRecordRequestDTO.getCalories() < 0){
            return DTO.StatusResponseDTO.builder().code(HttpStatus.FORBIDDEN).msg(CALS_NON_POSITIVE).build();
        }
        UserRecord userRecord = Converter.convertRecordRequestDTOToUserRecord(userRecordRequestDTO);
        UserRecord oldUserRecord = userRecordRepository.findById(userRecord.getId()).orElse(null);
        if (Objects.isNull(oldUserRecord))
            return DTO.StatusResponseDTO.builder().code(HttpStatus.BAD_REQUEST).msg(USER_RECORD_DOES_NOT_EXISTS).build();

        DTO.UserRecordRequestDTO oldDTO = DTO.UserRecordRequestDTO.builder()
                .recordId(oldUserRecord.getId()).meal(oldUserRecord.getMeal()).calories(oldUserRecord.getCalories())
                .dateTime(oldUserRecord.getDailyExpectedCaloriesData().getDate()).build();
        System.out.println("deleting " + userRecordRequestDTO.getRecordId());
        DTO.StatusResponseDTO deleteDTO = deleteUserRecord(userRecordRequestDTO.getRecordId(), currentUser);
        if(deleteDTO.getCode().is4xxClientError()) return deleteDTO;
        System.out.println("adding recordid" + userRecordRequestDTO.getRecordId());
        System.out.println("adding rec for uid " + recordUser.getId() + " recid " + userRecordRequestDTO.getRecordId());
        DTO.StatusResponseDTO addDTO = addUserRecord(userRecordRequestDTO, currentUser, recordUser);
        if(addDTO.getCode().is4xxClientError()) return addDTO;

        return DTO.StatusResponseDTO.builder().code(HttpStatus.ACCEPTED).msg(RECORD_UPDATED).build();
    }

    public DTO.UserReportDTO getUserRecordsReport(String queryString, Integer start, Integer size, User recordUserId) throws ValidationException {
        System.out.println("queryString = " + queryString);
        queryString = QueryUtils.transformQueryForUserRecords(queryString, recordUserId, start, size);
        System.out.println("query = " + queryString);
        Query query = entityManager.createNativeQuery(queryString, UserRecord.class);
        List<UserRecord> userRecords = ((List<UserRecord>)query.getResultList());
        Integer totalCalories = 0, totalExpectedCalories = 0, totalNumberOfMeals = 0;
        Map<String, DTO.UserRecordDayStats> userRecordDayStatsMap = new HashMap<>();
        for(UserRecord userRecord : userRecords){
            String dateKey = userRecord.getMealDate().toString().split("T")[0];
            if(!userRecordDayStatsMap.containsKey(dateKey)){
                userRecordDayStatsMap.put(dateKey, DTO.UserRecordDayStats.builder()
                        .date(userRecord.getMealDate())
                        .avgCaloriesConsumedPerMeal(0)
                        .numberOfMeals(0)
                        .totalCalories(userRecord.getDailyExpectedCaloriesData().getTotal_calories())
                        .expectedCalories(userRecord.getDailyExpectedCaloriesData().getExpected_calories())
                        .isWithinExpectedCalorieLimit(userRecord.getDailyExpectedCaloriesData().getDay_within_expected_range()).build());
                totalExpectedCalories += userRecord.getDailyExpectedCaloriesData().getExpected_calories();
            }
            totalNumberOfMeals +=1;
            totalCalories += userRecord.getCalories();
            DTO.UserRecordDayStats userRecordDayStats = userRecordDayStatsMap.get(dateKey);
            userRecordDayStats.setNumberOfMeals(userRecordDayStats.getNumberOfMeals()+1);
            userRecordDayStats.setAvgCaloriesConsumedPerMeal(userRecordDayStats.getTotalCalories()/userRecordDayStats.getNumberOfMeals());
            userRecordDayStatsMap.put(dateKey,userRecordDayStats);
        };
        return DTO.UserReportDTO.builder()
                .totalNumberOfMeals(userRecords.size())
                .code(HttpStatus.ACCEPTED)
                .msg(REPOST_GENERATED)
                .numberOfDays(userRecordDayStatsMap.size())
                .totalCalories(totalCalories)
                .avgCaloriesConsumedPerDay((float) totalCalories/userRecordDayStatsMap.size())
                .dayWiseStats(userRecordDayStatsMap)
                .avgNumberOfMealsPerDay((float)totalNumberOfMeals/userRecordDayStatsMap.size())
                .cumilativeCalorieIntakeToExpectedPercentage((float)(100*totalCalories)/totalExpectedCalories)
                .build();
    }

}
