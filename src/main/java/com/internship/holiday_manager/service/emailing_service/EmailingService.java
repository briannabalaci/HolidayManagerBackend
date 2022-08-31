package com.internship.holiday_manager.service.emailing_service;

import com.amazonaws.services.simpleemail.model.SendEmailResult;

import javax.mail.MessagingException;

public interface EmailingService {

    public void sendEmail(String invitedUserEmail, byte[] pdf) throws MessagingException;
}
