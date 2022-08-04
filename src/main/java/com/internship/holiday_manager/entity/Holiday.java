package com.internship.holiday_manager.entity;

import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import com.sun.xml.txw2.Document;
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
    private LocalDateTime start_date;

    @Column(name="end_date")
    private LocalDateTime end_date;

    @Column(name="substitute")
    private String substitute;

    @Column(name="document")
    private byte[] document;

    @Column(name="type")
    private HolidayType type;

    @Column(name="status")
    private HolidayStatus status;

    @Column(name="details")
    private String details;


    @ManyToOne()
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
