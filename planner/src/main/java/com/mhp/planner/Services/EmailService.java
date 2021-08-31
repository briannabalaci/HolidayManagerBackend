package com.mhp.planner.Services;

import java.util.List;
import java.util.Map;

public interface EmailService {
    void sendEmails(String title, String body, List<String> emailAddresses);

    void sendEmail(String title, String body, String email);

    void sendTemplatedEmails(String title, String templateName, Map<String, String> variables, List<String> emailAddresses);

    void sendTemplatedEmail(String title, String templateName, Map<String, String> variables, String email);
}
