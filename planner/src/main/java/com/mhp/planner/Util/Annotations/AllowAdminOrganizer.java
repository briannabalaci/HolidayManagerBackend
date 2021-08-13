package com.mhp.planner.Util.Annotations;


import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasAuthority(T(com.mhp.planner.Util.Enums.EAppRoles).ADMIN)" +
        "or hasAuthority(T(com.mhp.planner.Util.Enums.EAppRoles).ORGANIZER)")
@Target(ElementType.METHOD)
public @interface AllowAdminOrganizer {
}
