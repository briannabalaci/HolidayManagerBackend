package com.internship.holiday_manager.service.emailing_service;

import com.amazonaws.services.simpleemail.model.SendEmailResult;

import javax.mail.MessagingException;

public interface EmailingService {

    public void sendEmail( byte[] pdf, String name, String senderEmail) throws MessagingException;
}
