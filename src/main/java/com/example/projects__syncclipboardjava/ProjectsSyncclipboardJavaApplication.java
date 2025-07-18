package com.example.projects__syncclipboardjava;

import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.*;
import org.springframework.context.*;
import org.springframework.stereotype.*;

@SpringBootApplication
public class ProjectsSyncclipboardJavaApplication {

    public static void main(String[] args) {
        // SpringApplication.run(ProjectsSyncclipboardJavaApplication.class, args);

        SpringApplicationBuilder builder = new SpringApplicationBuilder(ProjectsSyncclipboardJavaApplication.class);
        // https://stackoverflow.com/questions/51004447/spring-boot-java-awt-headlessexception
        builder.headless(false);

        ConfigurableApplicationContext context = builder.run(args);
    }

    @Component
    public static class Runner implements CommandLineRunner {
        @Override
        public void run(String... args) throws Exception {
            SynCopy.main(args);
        }
    }
}
