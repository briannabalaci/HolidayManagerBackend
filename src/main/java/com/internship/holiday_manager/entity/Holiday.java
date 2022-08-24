package com.internship.holiday_manager.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="holiday")
public class Holiday {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_date")
    private LocalDateTime startDate;

    @Column(name="end_date")
    private LocalDateTime endDate;

    @Column(name="substitute")
    private String substitute;

    @Column(name="document")
    private byte[] document;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private HolidayType type;

    @Enumerated(EnumType.STRING)
    @Column(name="status")
    private HolidayStatus status;

    @Column(name="details")
    private String details;

    @ManyToOne()
    @JoinColumn(name = "user_id")
    @JsonBackReference
    private User user;
}
