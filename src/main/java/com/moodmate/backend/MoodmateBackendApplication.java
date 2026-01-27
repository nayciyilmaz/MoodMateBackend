package com.moodmate.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MoodmateBackendApplication {

    public static void main(String[] args) {

        SpringApplication.run(MoodmateBackendApplication.class, args);
        System.out.println("Backend Başarıyla Çalıştı...");
    }
}