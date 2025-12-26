package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface AuditLogMapper {
    int insertLog(AuditLog log);

    List<AuditLog> findLogs(@Param("actorId") Long actorId,
                            @Param("targetType") String targetType,
                            @Param("targetId") Long targetId,
                            @Param("fromTime") LocalDateTime fromTime,
                            @Param("toTime") LocalDateTime toTime,
                            @Param("offset") int offset,
                            @Param("pageSize") int pageSize);

    long countLogs(@Param("actorId") Long actorId,
                   @Param("targetType") String targetType,
                   @Param("targetId") Long targetId,
                   @Param("fromTime") LocalDateTime fromTime,
                   @Param("toTime") LocalDateTime toTime);
}
