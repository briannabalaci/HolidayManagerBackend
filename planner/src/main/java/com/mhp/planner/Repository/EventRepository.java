package com.mhp.planner.Repository;

import com.mhp.planner.Entities.Event;
import com.mhp.planner.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event,Long> {
}
