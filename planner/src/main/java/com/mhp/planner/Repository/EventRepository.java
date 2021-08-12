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
    List<Event> findAllByInvites_UserInvited_Email(String email);
    List<Event> findAllByInvites_UserInvited_EmailAndEventDateAfter(String email, LocalDateTime date);
    List<Event> findAllByInvites_UserInvited_EmailAndInvites_Status(String email, String status);
}
