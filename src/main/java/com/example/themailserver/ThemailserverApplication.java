package com.example.themailserver;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ThemailserverApplication {

	public static void main(String[] args) {
		SpringApplication.run(ThemailserverApplication.class, args);
		  try {
	            SmtpServer.startServer();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}

	
}
