package com.mhp.planner.Repository;

import com.mhp.planner.Entities.InviteQuestionResponse;
import com.mhp.planner.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InviteQuestionRepository extends JpaRepository<InviteQuestionResponse,Long> {
}