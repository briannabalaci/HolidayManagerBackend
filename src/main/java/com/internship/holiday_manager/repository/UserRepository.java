package com.internship.holiday_manager.repository;
import com.internship.holiday_manager.dto.UserDto;
import com.internship.holiday_manager.entity.Team;
import com.internship.holiday_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password);

    User findByEmail(@Param("email") String email);

    @Query("SELECT u from User u where u.team.id is null")
    List<User> findUsersWithoutTeam();


}
