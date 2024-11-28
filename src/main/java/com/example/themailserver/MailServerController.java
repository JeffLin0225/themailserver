package com.example.themailserver;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MailServerController {

    @Autowired
    private JavaMailSender mailSender;

    @GetMapping("/send-email")
    public String sendEmail(@RequestParam String to, @RequestParam String subject, @RequestParam String body) {
        try {
            // Construct the email
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("jefff@jxdns.ddnsking.com");
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            
            // Send the email
            mailSender.send(message);
            
            return "Email sent successfully!";
        } catch (Exception e) {
            return "Error sending email: " + e.getMessage();
        }
    }
}

