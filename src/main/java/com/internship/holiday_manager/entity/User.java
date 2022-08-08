package com.internship.holiday_manager.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.internship.holiday_manager.entity.enums.Department;
import com.internship.holiday_manager.entity.enums.Role;
import com.internship.holiday_manager.entity.enums.UserType;
import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name="users")
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

    @Enumerated(EnumType.STRING)
    @Column(name="department")
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name="role")
    private Role role;

    @Column(name="nr_holidays")
    private Integer nrHolidays;

    @Enumerated(EnumType.STRING)
    @Column(name="type")
    private UserType type;

    @ManyToOne()
    @JoinColumn(name = "team_id" )
//    @ToString.Exclude
    @JsonBackReference
    private Team team;


}
