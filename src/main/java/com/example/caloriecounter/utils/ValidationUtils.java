package com.example.caloriecounter.utils;

import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.enums.Roles;
import com.example.caloriecounter.models.User;
import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.example.caloriecounter.constants.CommonConstants.*;

@UtilityClass
public class ValidationUtils {

    static {
        Properties p = new Properties();
        try {
            p.load(new FileReader("caloriecounter.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.setProperties(p);
    }

    public static Set<Integer> superRolesForRecords = new HashSet<>(Collections.singletonList(Roles.ADMIN.ordinal()));
    public static Set<Integer> superRolesForUsers = new HashSet<>(Arrays.asList(Roles.ADMIN.ordinal(), Roles.USER_MANAGER.ordinal()));

    public void validate(DTO.UserRecordRequestDTO userRecordRequestDTO) throws ValidationException {
        String message = null;

        if(Objects.isNull(userRecordRequestDTO)){
            message = "failed. user record can not be null.";
        }
        else if(Objects.isNull(userRecordRequestDTO.getDateTime())){
            message = "failed. userRecord.date can not be null";
        }
        else if(Objects.isNull(userRecordRequestDTO.getMeal())){
            message = "failed. userRecord.meal can not be null";
        }
        if(Objects.nonNull(message)){
            throw new ValidationException(message, HttpStatus.BAD_REQUEST);
        }
    }
    public void validateEmail(String email) throws ValidationException {
        String regex = EMAIL_REGEX;
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);
        if(!matcher.matches())
            throw new ValidationException(INVALID_EMAIL, HttpStatus.BAD_REQUEST);
    }
    public User validateUserId(Optional<User> userOptional) throws ValidationException {
        String message = null;
        if(userOptional.isEmpty()){
            throw  new ValidationException(INVALID_USER_ID, HttpStatus.BAD_REQUEST);
        }
        return userOptional.get();
    }

    public void validateNonNull(Object object, String name) throws ValidationException{
        if(Objects.isNull(object))
            throw new ValidationException(name + "  NULL", HttpStatus.BAD_REQUEST);
    }

    public void validateMinLength(String string, int length) throws ValidationException {
        if(string.length() < length)
            throw new ValidationException(PSWD_TOO_SMALL, HttpStatus.BAD_REQUEST);
    }

    public void validateMaxLength(String string, int length) throws ValidationException {
        if(string.length() > length)
            throw new ValidationException("Password too long", HttpStatus.BAD_REQUEST);
    }

    public static String getStringProp(String key){
        String prop =  System.getProperty(key);
        if(Objects.isNull(prop)){
            throw new RuntimeException("prop is null for key : " + key);
        }
        return prop;
    }
}
