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

    @Query(value = "SELECT h from Holiday h where h.user.team.teamLeader.id =:teamleadId and " +
            " h.type =:type and h.user.id in " +
            "(SELECT u from User u where u.id != teamleadId and ( lower(u.forname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.forname) like lower(concat('%', :surname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :surname,'%'))))", nativeQuery = true)
    List<Holiday> filterByTypeAndUserName(@Param("teamleadId") Long teamleadId,@Param("type") HolidayType type,@Param("forname") String forname, @Param("surname") String surname);

    @Query(value = "SELECT h from Holiday h where h.user.team.teamLeader.id =:teamleadId and " +
            " h.type =:type and h.user.id in " +
            "(SELECT u from User u where u.id != teamleadId and ( u.id != teamleadId and ( lower(u.forname) like lower(concat('%', :name,'%')) or " +
            "lower(u.surname) like lower(concat('%', :name,'%'))))) ", nativeQuery = true)
    List<Holiday> filterByTypeAndOneUserName(@Param("teamleadId") Long teamleadId,@Param("type") HolidayType type,@Param("name") String name);

    @Query(value = "SELECT h from Holiday h where h.user.team.teamLeader.id =:teamleadId and h.user.id in" +
            "(SELECT u from User u where u.id != teamleadId and ( lower(u.forname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.forname) like lower(concat('%', :surname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :forname,'%')) or " +
            "lower(u.surname) like lower(concat('%', :surname,'%'))))", nativeQuery = true)
    List<Holiday> filterByUserName(@Param("teamleadId") Long teamleadId,@Param("surname") String surname, @Param("forname") String forname);

    @Query(value = "SELECT h from Holiday h where h.user.team.teamLeader.id =:teamleadId and h.user.id in " +
            "(SELECT u from User u where u.id != teamleadId and ( lower(u.forname) like lower(concat('%', :name,'%')) or " +
            "lower(u.surname) like lower(concat('%', :name,'%'))))", nativeQuery = true)
    List<Holiday> filterByOneUserName(@Param("teamleadId") Long teamleadId,@Param("name") String name);

    @Query(value = "SELECT h from Holiday h where h.user.team.teamLeader.id =:teamleadId" +
            " and h.type =:type and h.user.id != teamleadId", nativeQuery = true)
    List<Holiday> filterByType(@Param("teamleadId") Long teamleadId, @Param("type") HolidayType type);

}

