package com.internship.holiday_manager.utils.annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.internship.holiday_manager.entity.enums.UserType).ADMIN)")
@Target(ElementType.METHOD)
public @interface AllowAdmin {
}
