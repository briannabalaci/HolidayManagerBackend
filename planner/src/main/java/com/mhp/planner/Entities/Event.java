package com.mhp.planner.Entities;

import lombok.Data;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "event")
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "title")
    private String title;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "location")
    private String location;

    @Column(name = "dress_code")
    private String dressCode;

    @Column(name = "cover_image" , columnDefinition = "TEXT")
    @Lob
    private String cover_image;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_fk", referencedColumnName = "id", nullable = false)
    private List<Invite> invites;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "event_fk", referencedColumnName = "id", nullable = false)
    private List<Question> questions;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "organizer_fk", referencedColumnName = "id", nullable = false)
    private User organizer;

    @Column(name = "time_limit")
    private int time_limit;

}
