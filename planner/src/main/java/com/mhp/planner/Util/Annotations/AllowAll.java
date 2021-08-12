package com.mhp.planner.Util.Annotations;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.mhp.planner.Util.Enums.EAppRoles).ADMIN)" +
        "or hasAuthority(T(com.mhp.planner.Util.Enums.EAppRoles).ATTENDEE)" +
        "or hasAuthority(T(com.mhp.planner.Util.Enums.EAppRoles).ORGANIZER)")
@Target(ElementType.METHOD)
public @interface AllowAll {
}
