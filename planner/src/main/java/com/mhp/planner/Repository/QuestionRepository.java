package com.mhp.planner.Repository;

import com.mhp.planner.Entities.Question;
import com.mhp.planner.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long> {

    @Transactional
    void deleteById(Long id);
}
