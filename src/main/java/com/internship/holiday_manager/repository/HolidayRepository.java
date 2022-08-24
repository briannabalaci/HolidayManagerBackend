package com.internship.holiday_manager.repository;

import com.internship.holiday_manager.entity.Holiday;
import com.internship.holiday_manager.entity.User;
import com.internship.holiday_manager.entity.enums.HolidayStatus;
import com.internship.holiday_manager.entity.enums.HolidayType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HolidayRepository extends JpaRepository<Holiday,Long> {

    @Query("select h from Holiday h where h.user.id=:id ")
    List<Holiday> findUsersHolidays(@Param("id") Long id);

    @Query("select u from Holiday u where u.id=:id")
    Holiday findByID(@Param("id") Long id);

    List<Holiday> findAllByTypeAndUserId(HolidayType type, Long user_id);

    List<Holiday> findAllByStatusAndUserId(HolidayStatus status, Long user_id);

    List<Holiday> findAllByStatusAndTypeAndUserId(HolidayStatus status, HolidayType type, Long user_id);

    List<Holiday> findByUserId(Long userId);

    @Query("SELECT h from Holiday h where h.type =:type and h.user.id in " +
            "(SELECT u from User u where lower(u.forname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.forname) like lower(concat('%', :surname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :surname,'%')))")
    List<Holiday> filterByTypeAndUserName(@Param("type") HolidayType type,@Param("forname") String forname, @Param("surname") String surname);

    @Query("SELECT h from Holiday h where h.type =:type and h.user.id in " +
            "(SELECT u from User u where lower(u.forname) like lower(concat('%', :name,'%')) or " +
            "lower(u.surname) like lower(concat('%', :name,'%'))) ")
    List<Holiday> filterByTypeAndOneUserName(@Param("type") HolidayType type,@Param("name") String name);

    List<Holiday> findAllByType(HolidayType type);
    @Query("SELECT h from Holiday h where h.user.id in " +
            "(SELECT u from User u where lower(u.forname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.forname) like lower(concat('%', :surname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :surname,'%')))")
    List<Holiday> filterByUserName(@Param("surname") String surname, @Param("forname") String forname);
    @Query("SELECT h from Holiday h where h.user.id in " +
            "(SELECT u from User u where lower(u.forname) like lower(concat('%', :name,'%')) or " +
            "lower(u.surname) like lower(concat('%', :name,'%')))")
    List<Holiday> filterByOneUserName(@Param("name") String name);


}

