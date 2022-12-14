package com.internship.holiday_manager.repository;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.UserType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findByEmailAndPassword(@Param("email") String email, @Param("password") String password);

    User findByEmail(@Param("email") String email);

    @Query("SELECT u from User u where u.team.id is null")
    List<User> findUsersWithoutTeam();

    List<User> findByTeamIsNullAndTypeNot(UserType userType);

    @Query("SELECT u from User u where lower(u.forname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.forname) like lower(concat('%', :surname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :surname,'%'))")
    List<User> filterByName(@Param("forname") String forname, @Param("surname") String surname);



}
