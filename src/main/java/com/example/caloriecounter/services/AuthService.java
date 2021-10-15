package com.example.caloriecounter.services;

import com.example.caloriecounter.ValidationException;
import com.example.caloriecounter.constants.CommonConstants;
import com.example.caloriecounter.models.User;
import com.example.caloriecounter.repos.UserRepository;
import com.example.caloriecounter.utils.AuthUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {
    final
    UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User authenticateUser(String sessionId) throws ValidationException {
        User user = userRepository.findBySessionId(sessionId);
        if(Objects.nonNull(user) && user.getSessionExpiryTime().after(new Date())){
            return user;
        }
        throw new ValidationException(CommonConstants.INVALID_USER_ID, HttpStatus.FORBIDDEN);
    }

    public Boolean authenticatePassword(String providedPassword, String securePassword) {
        String salt = AuthUtils.getSalt();
        return AuthUtils.verifyUserPassword(providedPassword, securePassword, salt);
    }

    public String securePassword(String plainPwd) {
        String salt =  AuthUtils.getSalt();
        return AuthUtils.generateSecurePassword(plainPwd, salt);
    }

    public String generateSessionID() {
        return UUID.randomUUID().toString();
    }

    public Date generateSessionExpiryDate() {
        Calendar currentTimeNow = Calendar.getInstance();
        currentTimeNow.add(Calendar.SECOND, 500);
        return currentTimeNow.getTime();
    }
}
