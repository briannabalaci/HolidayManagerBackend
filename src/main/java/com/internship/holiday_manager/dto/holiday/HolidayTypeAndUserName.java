package com.internship.holiday_manager.dto.holiday;

import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.HolidayType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HolidayTypeAndUserName {
    private HolidayType type;
    private String forname;
    private String surname;
    private Long teamLeaderId;
}
