package com.internship.holiday_manager.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
@Table(name="notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="message")
    private String message;

    @Column(name="user_id")
    private Long userID;
}
