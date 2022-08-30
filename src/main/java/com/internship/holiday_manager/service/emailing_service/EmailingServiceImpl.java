package com.internship.holiday_manager.service.emailing_service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailingServiceImpl implements EmailingService{
    private AmazonSimpleEmailService amazonSimpleEmailService;
    @Override
    public SendEmailResult sendEmail(String invitedUserEmail) {
        String htmlBody ="<h1>You have been invited to a new event: " + "yes" + ".</h1>"
                + "Link : ⭐mhp-event-bites.web.app/event/redirect/"+ "1" +"⭐";
        log.info("HTML BODYYYYYY : {}",htmlBody);
        System.out.println(htmlBody);
        SendEmailRequest request = new SendEmailRequest()
                .withDestination(new Destination().withToAddresses(invitedUserEmail))
                .withMessage(new Message()
                        .withSubject(new Content().withData("MHP Event Bites | Invitation to " + "WS"))
                        .withBody(new Body().withHtml(new Content(htmlBody))))
                .withSource("tara-adela.hudrea@mhp.com");
        return amazonSimpleEmailService.sendEmail(request);
    }
}
