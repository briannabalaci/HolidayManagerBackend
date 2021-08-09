package com.mhp.planner.Entities;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "question")
@Data
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "text")
    private String text;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "answers_fk", referencedColumnName = "id", nullable = false)
    private List<Answers> answers;

}
