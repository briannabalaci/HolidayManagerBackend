package com.mhp.planner.Controller;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController

@AllArgsConstructor
@CrossOrigin
public class TestEmailController {
    @Autowired
    private AmazonSimpleEmailService amazonSimpleEmailService;

    @GetMapping("/sendEmail/{email}")
    public ResponseEntity<?> sendEmail(@PathVariable("email") String email){
        SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(email))
                .withMessage(new Message().withSubject(new Content().withData("hello")).withBody(new Body().withHtml(new Content("hello world"))))
                .withSource("no-reply@feedback-internship.de");
        amazonSimpleEmailService.sendEmail(request);

        return ResponseEntity.ok().build();
    }




}
