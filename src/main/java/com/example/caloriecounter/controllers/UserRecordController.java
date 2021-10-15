package com.example.caloriecounter.controllers;

import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.dtos.DTO;
import com.example.caloriecounter.models.User;
import com.example.caloriecounter.models.UserRecord;
import com.example.caloriecounter.repos.UserRepository;
import com.example.caloriecounter.services.AuthService;
import com.example.caloriecounter.services.UserRecordService;
import com.example.caloriecounter.services.UserService;
import com.example.caloriecounter.utils.ValidationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static com.example.caloriecounter.constants.CommonConstants.*;
import static java.lang.Math.min;

@RestController
public class UserRecordController {
    final UserRecordService userRecordService;
    final UserService userService;
    final AuthService authService;
    final UserRepository userRepository;

    public UserRecordController(UserRecordService userRecordService,
                                UserService userService,
                                AuthService authService, UserRepository userRepository) {
        this.userRecordService = userRecordService;
        this.userService = userService;
        this.authService = authService;
        this.userRepository = userRepository;
    }

    @PostMapping("/record/{userId}")
    public ResponseEntity<DTO.StatusResponseDTO> addUserRecord(@RequestHeader(AUTH_KEY) String sessionId,
                                                               @RequestBody DTO.UserRecordRequestDTO userRecordRequestDTO,
                                                               @PathVariable Long userId) {
        try{
            User currentUser = authService.authenticateUser(sessionId);
            User recordUser = ValidationUtils.validateUserId(userRepository.findById(userId));
            ValidationUtils.validate(userRecordRequestDTO);
            userRecordRequestDTO.setRecordId(null);
            DTO.StatusResponseDTO statusResponseDTO = userRecordService.addUserRecord(userRecordRequestDTO, currentUser, recordUser);
            return ResponseEntity.status(statusResponseDTO.getCode()).body(statusResponseDTO);
        }
        catch(ValidationException validationException){
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        }
        catch (Exception exception){
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/record")
    public ResponseEntity<DTO.StatusResponseDTO> addSelfRecord(@RequestHeader(AUTH_KEY) String sessionId,
                                                               @RequestBody DTO.UserRecordRequestDTO userRecordRequestDTO) {
        try{
            User currentUser = authService.authenticateUser(sessionId);
            ValidationUtils.validate(userRecordRequestDTO);
            userRecordRequestDTO.setRecordId(null);
            DTO.StatusResponseDTO statusResponseDTO = userRecordService.addUserRecord(userRecordRequestDTO, currentUser, currentUser);
            return ResponseEntity.status(statusResponseDTO.getCode()).body(statusResponseDTO);
        }
        catch(ValidationException validationException){
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        }
        catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



    @GetMapping("/records")
    public ResponseEntity<DTO.UserRecordResponseListDTO> getSelfRecords(@RequestHeader(AUTH_KEY) String sessionId,
                                                                        @RequestParam(required = false, defaultValue = DEFAULT_QUERY) String query,
                                                                        @RequestParam(required = false, defaultValue = DEFAUlT_QUERY_START) Integer start,
                                                                        @RequestParam(required = false, defaultValue =MAX_QUERY_SIZE) Integer size
                                                                        ) {
        size = min(10,size);
        try {
            User currentUser = authService.authenticateUser(sessionId);
            DTO.UserRecordResponseListDTO recordResponseDTOList = userRecordService.getUserRecords(query, start, size, currentUser);
            return new ResponseEntity<>(recordResponseDTOList, recordResponseDTOList.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.UserRecordResponseListDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.UserRecordResponseListDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/report")
    public ResponseEntity<DTO.UserReportDTO> getReport(@RequestHeader(AUTH_KEY) String sessionId,
                                                                        @RequestParam(required = false, defaultValue = DEFAULT_QUERY) String query,
                                                                        @RequestParam(required = false, defaultValue = DEFAUlT_QUERY_START) Integer start,
                                                                        @RequestParam(required = false, defaultValue = "50") Integer size
    ) {
        size = min(50,size);
        try {
            User currentUser = authService.authenticateUser(sessionId);
            DTO.UserReportDTO recordResponseDTOList = userRecordService.getUserRecordsReport(query, start, size, currentUser);
            return new ResponseEntity<>(recordResponseDTOList, recordResponseDTOList.getCode());
        } catch (ValidationException validationException) {
            return new ResponseEntity<>(DTO.UserReportDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        } catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.UserReportDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/record/{id}")
    public ResponseEntity<UserRecord> getUserRecord(@PathVariable Long id) {
        UserRecord userRecord = null;
        try {
            userRecord = userRecordService.getUserRecord(id);
        } catch (ValidationException validationException) {
            validationException.printStackTrace();
        }
        if(Objects.nonNull(userRecord)) {
            return ResponseEntity.ok().body(userRecord);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }

    @DeleteMapping("/record/{recordId}")
    public ResponseEntity<DTO.StatusResponseDTO> deleteSelfRecord(@RequestHeader(AUTH_KEY) String sessionId,
                                                                  @PathVariable Long recordId) {
        try{
            User currentUser = authService.authenticateUser(sessionId);
            ValidationUtils.validateNonNull(recordId, "record id");
            DTO.StatusResponseDTO statusResponseDTO = userRecordService.deleteUserRecord(recordId, currentUser);
            return ResponseEntity.status(statusResponseDTO.getCode()).body(statusResponseDTO);
        }
        catch(ValidationException validationException){
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        }
        catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/record")
    public ResponseEntity<DTO.StatusResponseDTO> updateUserRecord(@RequestHeader(AUTH_KEY) String sessionId,
                                                   @RequestBody DTO.UserRecordRequestDTO userRecordRequestDTO) {
        try {
            User currentUser = authService.authenticateUser(sessionId);
            ValidationUtils.validate(userRecordRequestDTO);
            DTO.StatusResponseDTO statusResponseDTO = userRecordService.updateUserRecord(userRecordRequestDTO, currentUser, currentUser);
            return ResponseEntity.status(statusResponseDTO.getCode()).body(statusResponseDTO);
        } catch(ValidationException validationException){
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        }
        catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    @PutMapping("/record/{id}")
    public ResponseEntity<DTO.StatusResponseDTO> updateUserRecord(@RequestHeader(AUTH_KEY) String sessionId,
                                                   @RequestBody DTO.UserRecordRequestDTO userRecordRequestDTO, @PathVariable Long id) {
        try {
            User currentUser = authService.authenticateUser(sessionId);
            User recordUser = ValidationUtils.validateUserId(userRepository.findById(id));
            ValidationUtils.validate(userRecordRequestDTO);
            DTO.StatusResponseDTO statusResponseDTO = userRecordService.updateUserRecord(userRecordRequestDTO, currentUser, recordUser);
            return ResponseEntity.status(statusResponseDTO.getCode()).body(statusResponseDTO);
        } catch(ValidationException validationException){
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(validationException.getMsg())
                    .code(validationException.getCode()).build(), validationException.getCode());
        }
        catch (Exception exception){
            exception.printStackTrace();
            return new ResponseEntity<>(DTO.StatusResponseDTO.builder().msg(SOMETHING_WENT_WRONG + exception.getMessage())
                    .code(HttpStatus.INTERNAL_SERVER_ERROR).build(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
