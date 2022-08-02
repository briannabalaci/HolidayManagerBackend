package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.enums.Role;
import com.internship.holiday_manager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u.id from User u where u.email=:email and u.password=:password")
    Integer findByEmailAndPassword(@Param("email") String email, @Param("password") String password);
}
