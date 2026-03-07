package com.studyhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.studyhub.model")
@EnableJpaRepositories("com.studyhub.repository")
public class StudyHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(StudyHubApplication.class, args);
    }
}
