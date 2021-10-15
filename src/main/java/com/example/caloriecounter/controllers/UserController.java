package com.example.caloriecounter.controllers;

import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.models.User;
import com.example.caloriecounter.repos.UserRepository;
import com.example.caloriecounter.services.AuthService;
import com.example.caloriecounter.services.UserService;
import com.example.caloriecounter.utils.ValidationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static com.example.caloriecounter.constants.CommonConstants.*;
import static java.lang.Math.min;


@RestController
public class UserController {

    final UserService userService;
    final AuthService authService;
    final UserRepository userRepository;

    public UserController(UserService userService, AuthService authService, UserRepository userRepository) {
        this.userService = userService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/signup")
    public ResponseEntity<DTO.StatusResponseDTO> signup(@RequestBody DTO.UserSignUpRequestDTO userSignUpRequestDTO){
        try {
            ValidationUtils.validateNonNull(userSignUpRequestDTO.getFirstName(), "first name");
            ValidationUtils.validateMinLength(userSignUpRequestDTO.getFirstName(), 1);
            ValidationUtils.validateNonNull(userSignUpRequestDTO.getLastName(), "last name");
            ValidationUtils.validateMinLength(userSignUpRequestDTO.getLastName(), 1);
            ValidationUtils.validateNonNull(userSignUpRequestDTO.getPassword(), "password");
            ValidationUtils.validateMinLength(userSignUpRequestDTO.getPassword(),8);
            ValidationUtils.validateMaxLength(userSignUpRequestDTO.getPassword(),64);
            ValidationUtils.validateNonNull(userSignUpRequestDTO.getEmail(), "email");;
            ValidationUtils.validateEmail(userSignUpRequestDTO.getEmail());

            DTO.StatusResponseDTO statusResponseDTO = userService.processSignUpRequest(userSignUpRequestDTO);
            return new ResponseEntity<>(statusResponseDTO,statusResponseDTO.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/signin")
    public ResponseEntity<DTO.StatusResponseDTO> signIn(@RequestBody DTO.UserSignInRequestDTO userSignInRequestDTO) {
        try{
            ValidationUtils.validateEmail(userSignInRequestDTO.getEmail());
            return userService.processSignInRequest(userSignInRequestDTO);
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/users")
    public ResponseEntity<DTO.UserResponseListDTO> getSelfRecords(@RequestHeader(AUTH_KEY) String sessionId,
                                                                        @RequestParam(required = false, defaultValue = DEFAULT_QUERY) String query,
                                                                        @RequestParam(required = false, defaultValue = DEFAUlT_QUERY_START) Integer start,
                                                                        @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        size = min(10,size);
        try {
            User currentUser = authService.authenticateUser(sessionId);
            DTO.UserResponseListDTO userResponseListDTO = userService.getUserData(query, start, size, currentUser);
            return new ResponseEntity(userResponseListDTO, userResponseListDTO.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.UserResponseListDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.UserResponseListDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/auth")
    public Boolean authenticate(@RequestHeader(AUTH_KEY) String sessionId){
        try {
            return authService.authenticateUser(sessionId)!=null;
        } catch (ValidationException validationException) {
            validationException.printStackTrace();
        }
        return null;
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<DTO.StatusResponseDTO> deleteUser(@RequestHeader(AUTH_KEY) String sessionId, @PathVariable Long id) {
        DTO.StatusResponseDTO statusResponseDTO = null;
        try {
            User currentUser = authService.authenticateUser(sessionId);
            User recordUser = ValidationUtils.validateUserId(userRepository.findById(id));
            statusResponseDTO = userService.deleteUser(recordUser, currentUser);
            return new ResponseEntity<>(statusResponseDTO,statusResponseDTO.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @DeleteMapping("/user")
    public ResponseEntity<DTO.StatusResponseDTO> deleteUser(@RequestHeader(AUTH_KEY) String sessionId) {
        DTO.StatusResponseDTO statusResponseDTO = null;
        try {
            User currentUser = authService.authenticateUser(sessionId);
            statusResponseDTO = userService.deleteUser(currentUser, currentUser);
            return new ResponseEntity<>(statusResponseDTO,statusResponseDTO.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception) {
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/user/{id}")
    public ResponseEntity<DTO.StatusResponseDTO> updateUser(@RequestBody DTO.UserUpdateRequestDTO userUpdateRequestDTO,
                                                            @RequestHeader(AUTH_KEY) String sessionId, @PathVariable Long id) {
        DTO.StatusResponseDTO statusResponseDTO = null;
        try {
            ValidationUtils.validateNonNull(userUpdateRequestDTO.getFirstName(), "first name");
            ValidationUtils.validateMinLength(userUpdateRequestDTO.getFirstName(), 1);
            ValidationUtils.validateNonNull(userUpdateRequestDTO.getLastName(), "last name");
            ValidationUtils.validateMinLength(userUpdateRequestDTO.getLastName(), 1);
            ValidationUtils.validateNonNull(userUpdateRequestDTO.getPassword(), "password");
            ValidationUtils.validateMinLength(userUpdateRequestDTO.getPassword(),8);
            ValidationUtils.validateMaxLength(userUpdateRequestDTO.getPassword(),64);
            ValidationUtils.validateNonNull(userUpdateRequestDTO.getEmail(), "email");;
            ValidationUtils.validateEmail(userUpdateRequestDTO.getEmail());
            ValidationUtils.validateNonNull(userUpdateRequestDTO, "request body");


            User currentUser = authService.authenticateUser(sessionId);
            User recordUser = ValidationUtils.validateUserId(userRepository.findById(id));
            statusResponseDTO = userService.updateUser(userUpdateRequestDTO, recordUser, currentUser);
            return new ResponseEntity<>(statusResponseDTO,statusResponseDTO.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/user")
    public ResponseEntity<DTO.StatusResponseDTO> updateUser(@RequestBody DTO.UserUpdateRequestDTO userUpdateRequestDTO,
                                                            @RequestHeader(AUTH_KEY) String sessionId) {
        DTO.StatusResponseDTO statusResponseDTO = null;
        try {
            ValidationUtils.validateNonNull(userUpdateRequestDTO.getEmail(), "email");
            ValidationUtils.validateEmail(userUpdateRequestDTO.getEmail());
            User currentUser = authService.authenticateUser(sessionId);
            statusResponseDTO = userService.updateUser(userUpdateRequestDTO, currentUser, currentUser);
            return new ResponseEntity<>(statusResponseDTO,statusResponseDTO.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception) {
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
