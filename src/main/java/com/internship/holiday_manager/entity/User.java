package com.internship.holiday_manager.entity;

import lombok.Data;

import javax.persistence.*;

enum Department{
    JAVA, ABAP, BUSINESS_INTELLIGENCE
}

enum Role{
    TESTER, DEVELOPER
}

enum UserType{
    ADMIN, TEAMLEAD, EMPLOYEE
}
@Entity
@Data
@Table(name="user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="email")
    private String email;

    @Column(name="password")
    private String password;

    @Column(name="forname")
    private String forname;

    @Column(name="surname")
    private String surname;

    @Column(name="department")
    private Department department;

    @Column(name="role")
    private Role role;

    @Column(name="nr_of_holidays")
    private Integer nr_of_holidays;

    @Column(name="type")
    private UserType type;

    @Column(name="team_id")
    private Long teamID;
}
