package com.internship.holiday_manager.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(name="team")
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name")
    private String name;

    @OneToOne
    @JoinColumn(name = "team_leader")
    private User teamLeader;

    @OneToMany(cascade = {CascadeType.ALL},orphanRemoval = true)
    @JoinColumn(name="team_id")
    @JsonManagedReference
    private List<User> members;
}
