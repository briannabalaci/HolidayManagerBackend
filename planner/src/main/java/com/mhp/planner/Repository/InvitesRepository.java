package com.mhp.planner.Repository;

import com.mhp.planner.Entities.Invite;
import com.mhp.planner.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvitesRepository extends JpaRepository<Invite,Long> {
    findBy
}
