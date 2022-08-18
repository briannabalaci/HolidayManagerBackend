package com.internship.holiday_manager.entity;

import com.internship.holiday_manager.entity.enums.HolidayType;
import com.internship.holiday_manager.entity.enums.NotificationType;
import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name="notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne()
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne()
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Column(name="send_date")
    private LocalDateTime sendDate;

    @Column(name="seen")
    private Boolean seen;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private NotificationType type;

    @ManyToOne(cascade = CascadeType.MERGE)
    @JoinColumn(name = "request_id", nullable = false)
    private Holiday request;
}
