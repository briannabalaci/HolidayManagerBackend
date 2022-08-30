package com.internship.holiday_manager.service.emailing_service;

import com.amazonaws.services.simpleemail.model.SendEmailResult;

public interface EmailingService {

    public SendEmailResult sendEmail(String invitedUserEmail);
}
