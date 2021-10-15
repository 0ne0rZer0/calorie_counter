package com.example.caloriecounter;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@Getter
public class ValidationException extends Exception{
    HttpStatus code;
    String msg;

    public ValidationException(String message, HttpStatus code) {
        super(message);
        this.msg = message;
        this.code = code;
    }
}
