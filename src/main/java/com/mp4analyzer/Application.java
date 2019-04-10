package com.mp4analyzer;

import com.mp4analyzer.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class Application /* implements CommandLineRunner */ {

    @Autowired
    private VideoService videoService;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

//    @Override
//    public void run(String... args) throws Exception {
//        this.screenGrabService.testSharedFolder();
//    }
}
