package com.example.caloriecounter;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

@SpringBootApplication
@EnableJpaAuditing
public class CalorieCounterApplication {

    public static void main(String[] args) throws IOException {
        SpringApplication.run(CalorieCounterApplication.class, args);
    }

}
