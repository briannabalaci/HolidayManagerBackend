package com.internship.holiday_manager.service.user_service;

import com.internship.holiday_manager.entity.User;

public interface UserService {

    User authentication(String email, String password);


}
