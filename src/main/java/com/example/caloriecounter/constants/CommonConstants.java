package com.example.caloriecounter.constants;

import lombok.experimental.UtilityClass;

import static com.example.caloriecounter.utils.ValidationUtils.getStringProp;


@UtilityClass
public class CommonConstants {

    public final String AUTH_KEY = "auth-access-token";
    public final String SOMETHING_WENT_WRONG = "something went wrong. ";
    public final String DEFAULT_QUERY = "true";
    public final String DEFAUlT_QUERY_START = "0";
    public final String MAX_QUERY_SIZE = "10";
    public final String INVALID_USER_ID = "unauthorised/invalid user id ";
    public final String USER_RECORD_DOES_NOT_EXISTS = "User Record does not exist";
    public final String RECORD_UPDATED = "Record updated";
    public final String RECORD_ADDED = "Record added";
    public final String REPOST_GENERATED = "successfully generated user query report";
    public final String READ_REQUEST_PROCESSED = "read request processed";
    public final String NUTRITIONIX_REQ_PREFIX = "{\"appId\": \"2a8c62f0\",\"appKey\": \"";
    public final String NUTRITIONIX_MIDDLE =  "\",\"fields\": [\"nf_calories\"],\"offset\": 0,\"limit\": 1,\"query\": \"";
    public final String NUTRITIONIX_SUFFIX = "\"}";
    public final String FIELDS = "fields";
    public final String NF_CALORIES = "nf_calories";
    public final String UNAUTHORISED = "unauthorized operation";
    public final String CALS_NON_POSITIVE = "calories have to be non negative";
    public final String RECORD_DELETED = "Record deleted";
    public final String TOTAL = "total";
    public final String HITS = "hits";
    public final String EMAIL_EXISTS = "email already registered";
    public final String  SIGNUP_SUCCESS = "sign up successful";
    public final String EMAIL_PSWD_NO_MATCH = "email password combination does not match";
    public final String LOGIN_SUCCESS = "login success";
    public final String USER_DELETED = "user deleted";
    public final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}";
    public final String INVALID_EMAIL = "Email format invalid";
    public final String PSWD_TOO_SMALL = "Password too small";

    public final String NUTRITIONIX_APP_KEY= getStringProp("nutrionix.app.key");
    public final String NUTRITIONIX_URL = getStringProp("nutritionix.url");
    public final static String SALT = getStringProp("salt.key");
    public final static String SK_INSTANCE = getStringProp("sk.instance.key");


}
