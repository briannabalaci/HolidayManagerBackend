package com.mhp.planner.Services;

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URISyntaxException;
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
    public void sendEmail(String title, String body, List<String> emailAddresses) {
        SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withBccAddresses(emailAddresses))
                .withMessage(new Message()
                        .withSubject(new Content().withData(title))
                        .withBody(new Body().withHtml(new Content(body))))
                .withSource(SENDER_EMAIL_ADDR);
        amazonSimpleEmailService.sendEmail(request);
    }

    @Override
    public void sendTemplatedEmail(String title, String templateName, Map<String, String> variables, List<String> emailAddresses) {

        String templatePath = "emailTemplates/" + templateName;
        String body = readResourceFileAsString(templatePath);

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            body = body.replaceAll("\\{\\{[ ]*" + entry.getKey() + "[ ]*}}", entry.getValue());
        }

        sendEmail(title, body, emailAddresses);
    }

    private String readResourceFileAsString(String resourcePath) {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        try {
            URI uri = Objects.requireNonNull(classloader.getResource(resourcePath)).toURI();
            return new String(Files.readAllBytes(Paths.get(uri)));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
