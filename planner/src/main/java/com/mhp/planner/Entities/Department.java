package com.mhp.planner.Entities;

import lombok.Data;
import javax.persistence.*;

@Entity
@Table(name = "department")
@Data
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column (name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

}
