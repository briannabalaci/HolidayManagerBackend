package com.mhp.planner.Entities;

import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "invite")
@Data
public class Invite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private long id;

    @Column(name = "status")
    private String status;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_invited_fk", referencedColumnName = "id", nullable = false)
    private User userInvited;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "invite_fk", referencedColumnName = "id", nullable = false)
    private List<InviteQuestionResponse> inviteQuestionResponses;
}
