package com.mhp.planner.Services;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {
    public static final String SENDER_EMAIL_ADDR = "no-reply@feedback-internship.de";
    private final AmazonSimpleEmailService amazonSimpleEmailService;

    @Override
    public void sendEmails(String title, String body, List<String> emailAddresses) {
        SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withBccAddresses(emailAddresses))
                .withMessage(new Message()
                        .withSubject(new Content().withData(title))
                        .withBody(new Body().withHtml(new Content(body))))
                .withSource(SENDER_EMAIL_ADDR);
        amazonSimpleEmailService.sendEmail(request);
    }

    @Override
    public void sendEmail(String title, String body, String email) {
        sendEmails(title, body, List.of(email));
    }

    @Override
    public void sendTemplatedEmails(String title, String templateName, Map<String, String> variables, List<String> emailAddresses) {

        String templatePath = "emailTemplates/" + templateName;
        String body = readResourceFileAsString(templatePath);

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            body = body.replaceAll("\\{\\{[ ]*" + entry.getKey() + "[ ]*}}", entry.getValue());
        }

        sendEmails(title, body, emailAddresses);
    }

    @Override
    public void sendTemplatedEmail(String title, String templateName, Map<String, String> variables, String email) {
        sendTemplatedEmails(title, templateName, variables, List.of(email));
    }

    private String readResourceFileAsString(String resourcePath) {
            try {
                final ClassPathResource classPathResource = new ClassPathResource(resourcePath);
                return StreamUtils.copyToString(classPathResource.getInputStream(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
    }
}

