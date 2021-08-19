package com.mhp.planner.Repository;

import com.mhp.planner.Entities.Invite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvitesRepository extends JpaRepository<Invite,Long> {

    void deleteAllByUserInvited_Id(Long id);
    List<Invite> findByStatus(String status);

    
}
