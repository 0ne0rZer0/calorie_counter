package com.example.caloriecounter.utils;

import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.enums.Roles;
import com.example.caloriecounter.models.User;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.util.*;

import static com.example.caloriecounter.utils.ValidationUtils.superRolesForRecords;
import static com.example.caloriecounter.utils.ValidationUtils.superRolesForUsers;

@UtilityClass
public class QueryUtils {
    static Map<String, String> operatorMap = new HashMap<>();
    static Map<String, String> userRecordParamsMap = new HashMap<>();
    static Map<String, String> userParamsMap = new HashMap<>();
    static {
        operatorMap.put("gteq", ">=");
        operatorMap.put("lteq", "<=");
        operatorMap.put("gt", ">");
        operatorMap.put("lt","<");
        operatorMap.put("eq", "=");
        operatorMap.put("neq","!=");
        userRecordParamsMap.put("expected_cals", "d.expected_calories");
        userRecordParamsMap.put("date","meal_date");
        userRecordParamsMap.put("time","meal_time");
        userRecordParamsMap.put("id","u.id");
        userParamsMap.put("fname","first_name");
        userParamsMap.put("lname","last_name");
        userParamsMap.put("expected_cals", "expected_calories");
    }

    public String transformQueryForUserRecords(String queryString, User currentUser, Integer start, Integer size) throws ValidationException {
        if(superRolesForRecords.contains(currentUser.getRole())){
            return "select uR.id, calories, ur.created_at, meal,meal_date, meal_time, uR.updated_at, " +
                    "daily_expected_calories_data_id from users  as u inner join daily_expected_calories_data " +
                    "as d on u.id = d.user_id inner join user_records as uR on d.id = uR.daily_expected_calories_data_id where " +
                    String.join(" ", parseQuery(queryString, currentUser, userRecordParamsMap, superRolesForRecords))
                    + " and ( u.id = " + currentUser.getId() + " or role <= " + currentUser.getRole()+ " ) limit " + start + ", " + size;
        }
        return "select uR.id, calories, ur.created_at, meal,meal_date, meal_time, uR.updated_at, " +
                "daily_expected_calories_data_id from users  as u inner join daily_expected_calories_data " +
                "as d on u.id = d.user_id inner join user_records as uR on d.id = uR.daily_expected_calories_data_id where " +
                String.join(" ", parseQuery(queryString, currentUser, userRecordParamsMap, superRolesForRecords))
                + " and ( u.id = " + currentUser.getId() + " ) limit " + start + ", " + size;


    }


    public String transformQueryForUsers(String queryString, User currentUser, Integer start, Integer size) throws ValidationException {

        if(superRolesForUsers.contains(currentUser.getRole())){
            return "select * from users where " + String.join(" ", parseQuery(queryString, currentUser, userParamsMap, superRolesForUsers))
                    + " and ( id = " + currentUser.getId() + " or role <= " + currentUser.getRole()+ " ) limit " + start + ", " + size;
        }
        return "select * from users where " + String.join(" ", parseQuery(queryString, currentUser, userParamsMap, superRolesForUsers))
                + " and ( id = " + currentUser.getId() + " ) limit " + start + ", " + size;

    }

    private String[] parseQuery(String queryString, User currentUser, Map<String, String> paramsMap, Set<Integer> superRoles) throws ValidationException {
        String[] queryParts = queryString.split("\\s+");
        for(int i = 0; i<queryParts.length; i++){
            if(queryParts[i].equals("id") && !superRoles.contains(currentUser.getRole())){
                throw new ValidationException("user not eligible to query over different ids", HttpStatus.FORBIDDEN);
            }
            if(operatorMap.containsKey(queryParts[i])){
                queryParts[i] = operatorMap.get(queryParts[i]);
            }
            else if(paramsMap.containsKey(queryParts[i])){
                queryParts[i] = paramsMap.get(queryParts[i]);
            }
        }
        return queryParts;
    }
}
