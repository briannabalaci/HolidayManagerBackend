package com.internship.holiday_manager.entity;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name="team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @Column(name="team_leader")
    private String teamLeader;
}
