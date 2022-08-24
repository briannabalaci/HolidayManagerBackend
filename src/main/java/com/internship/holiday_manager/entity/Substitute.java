package com.internship.holiday_manager.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
@Data
@Entity
@Table(name="substitute")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Substitute {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="start_date")
    private LocalDateTime startDate;

    @Column(name="end_date")
    private LocalDateTime endDate;

    @OneToOne()
    @JoinColumn(name = "substitute_id")
    @JsonBackReference
    private User substitute;

    @OneToOne()
    @JoinColumn(name = "teamlead_id")
    @JsonBackReference
    private User teamLead;
}
