package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface ReservationMapper {
    Reservation findById(@Param("id") Long id);

    List<Reservation> findReservations(@Param("labId") Long labId,
                                       @Param("deviceId") Long deviceId,
                                       @Param("requesterId") Long requesterId,
                                       @Param("status") List<String> status,
                                       @Param("type") String type,
                                       @Param("fromTime") LocalDateTime fromTime,
                                       @Param("toTime") LocalDateTime toTime,
                                       @Param("offset") int offset,
                                       @Param("pageSize") int pageSize);

    long countReservations(@Param("labId") Long labId,
                           @Param("deviceId") Long deviceId,
                           @Param("requesterId") Long requesterId,
                           @Param("status") List<String> status,
                           @Param("type") String type,
                           @Param("fromTime") LocalDateTime fromTime,
                           @Param("toTime") LocalDateTime toTime);

    int insertReservation(Reservation reservation);

    int updateReservationTime(Reservation reservation);

    int updateReservationStatus(Reservation reservation);

    List<Reservation> findConflicts(@Param("labId") Long labId,
                                    @Param("deviceId") Long deviceId,
                                    @Param("startTime") LocalDateTime startTime,
                                    @Param("endTime") LocalDateTime endTime,
                                    @Param("excludeId") Long excludeId);

    List<Reservation> findCalendar(@Param("labId") Long labId,
                                   @Param("deviceId") Long deviceId,
                                   @Param("fromTime") LocalDateTime fromTime,
                                   @Param("toTime") LocalDateTime toTime);
}
