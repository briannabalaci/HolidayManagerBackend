package com.internship.holiday_manager.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name="team",uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "team_leader"})})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="name",unique=true)
    private String name;

    @OneToOne
    @JoinColumn(name = "team_leader", unique = true)
    private User teamLeader;

    @OneToMany()
    @JoinColumn(name="team_id")
    @JsonManagedReference
    @ToString.Exclude
    private List<User> members;
}
