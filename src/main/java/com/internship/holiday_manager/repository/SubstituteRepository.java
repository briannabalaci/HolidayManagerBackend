package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.Substitute;
import com.internship.holiday_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SubstituteRepository extends JpaRepository<Substitute,Long> {

    List<Substitute> findBySubstitute(User user);

    List<Substitute> findByTeamLead(User user);

    Substitute findByHoliday(Holiday holiday);


}
