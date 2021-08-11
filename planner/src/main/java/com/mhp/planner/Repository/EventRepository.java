package com.mhp.planner.Repository;

import com.mhp.planner.Entities.Event;
import com.mhp.planner.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@Transactional
public interface EventRepository extends JpaRepository<Event,Long> {
    List<Event> findAllByEventDateAfter(LocalDateTime date);
}
