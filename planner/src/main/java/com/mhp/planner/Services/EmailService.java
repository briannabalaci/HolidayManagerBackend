package com.mhp.planner.Services;

import java.util.List;
import java.util.Map;

public interface EmailService {
    void sendEmail(String title, String body, List<String> emailAddresses);

    void sendTemplatedEmail(String title, String templateName, Map<String, String> variables, List<String> emailAddresses);
}
