package com.mhp.planner.Entities;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "answer")
@Data
public class Answers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "text")
    private String text;

}
