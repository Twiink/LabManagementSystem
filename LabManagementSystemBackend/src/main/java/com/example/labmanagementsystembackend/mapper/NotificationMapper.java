package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.Notification;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface NotificationMapper {
    int insertNotification(Notification notification);

    Notification findById(@Param("id") Long id);

    List<Notification> findNotifications(@Param("userId") Long userId,
                                         @Param("status") String status,
                                         @Param("offset") int offset,
                                         @Param("pageSize") int pageSize);

    long countNotifications(@Param("userId") Long userId, @Param("status") String status);

    int markRead(@Param("id") Long id);
}
