package com.internship.holiday_manager.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name="detailed_holiday")
public class DetailedHoliday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="creation_date")
    private LocalDateTime creationDate;

    @OneToOne
    @JoinColumn(name = "holiday_id", nullable = false)
    @JsonBackReference
    private Holiday holiday;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;
}
