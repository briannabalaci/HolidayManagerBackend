package com.mhp.planner.Emailing;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Bean
    public AmazonSimpleEmailService emailService() {
        return AmazonSimpleEmailServiceClientBuilder.standard()
                .withCredentials(credentialsProvider())
                .withRegion(Regions.EU_CENTRAL_1)
                .build();
    }

    public AWSCredentialsProvider credentialsProvider() {
        return new AWSStaticCredentialsProvider(credentials());
    }

    public AWSCredentials credentials() {
        return new BasicAWSCredentials("AKIA4ZOVBMAY7USXDB4L","xwjlxNHprSqvPi+L5KTUSviRNcfboOAQw/y1TsN7");
    }
}
