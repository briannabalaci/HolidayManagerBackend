package com.internship.holiday_manager.service.emailing_service;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Properties;

// JavaMail libraries. Download the JavaMail API
// from https://javaee.github.io/javamail/
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;

// AWS SDK libraries. Download the AWS SDK for Java
// from https://aws.amazon.com/sdk-for-java
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.RawMessage;
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailingServiceImpl implements EmailingService{
    private final AmazonSimpleEmailService amazonSimpleEmailService;
    @Override
    public void sendEmail(byte[] pdf, String name, String senderEmail) throws MessagingException {
//        String htmlBody ="<h1> A new request needs approval </h1>"
//                + "⭐";
//        System.out.println(htmlBody);
//        SendEmailRequest request = new SendEmailRequest()
//                .withDestination(new Destination().withToAddresses(invitedUserEmail))
//                .withMessage(new Message()
//                        .withSubject(new Content().withData("MHP Event Bites | Invitation to " + "WS"))
//                        .withBody(new Body().withHtml(new Content(htmlBody))))
//                .withSource("tara-adela.hudrea@mhp.com");
//        return amazonSimpleEmailService.sendEmail(request);

        Session session = Session.getDefaultInstance(new Properties());

        // Create a new MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Add subject, from and to lines.
        message.setSubject("Subject of the email", "UTF-8");
        message.setFrom(new InternetAddress(senderEmail));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse("tara-adela.hudrea@mhp.com"));

        MimeMultipart msg_body = new MimeMultipart("alternative");

        MimeBodyPart wrap = new MimeBodyPart();

        MimeBodyPart textPart = new MimeBodyPart();
        textPart.setContent("The body of the email", "text/plain; charset=UTF-8");

        MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent("<h3> A new request needs to be approved </h3>" +
                "<p> The user "+name+" made a new request. Take a look and decide if you want to approve the " +
                "holiday request or not. </p>","text/html; charset=UTF-8");

        // Add the text and HTML parts to the child container.
        msg_body.addBodyPart(textPart);
        msg_body.addBodyPart(htmlPart);

        // Add the child container to the wrapper object.
        wrap.setContent(msg_body);

        MimeMultipart msg = new MimeMultipart("mixed");

        // Add the parent container to the message.
        message.setContent(msg);

        // Add the multipart/alternative part to the message.
        msg.addBodyPart(wrap);

        // Define the attachment
        MimeBodyPart att = new MimeBodyPart();
        DataSource fds = new ByteArrayDataSource(pdf,"application/pdf");
        att.setDataHandler(new DataHandler(fds));
        att.setFileName("CerereConcediu"+name+".pdf");

        // Add the attachment to the message.
        msg.addBodyPart(att);

        try {
            // Send the email.
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.writeTo(outputStream);
            RawMessage rawMessage =
                    new RawMessage(ByteBuffer.wrap(outputStream.toByteArray()));

            SendRawEmailRequest rawEmailRequest =
                    new SendRawEmailRequest(rawMessage);

            amazonSimpleEmailService.sendRawEmail(rawEmailRequest);
            System.out.println("Email sent!");
            // Display an error if something goes wrong.
        } catch (Exception ex) {
            System.out.println("Email Failed");
            System.err.println("Error message: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
