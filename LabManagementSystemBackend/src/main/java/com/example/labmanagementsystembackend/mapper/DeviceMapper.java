package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceMapper {
    Device findById(@Param("id") Long id);

    List<Device> findDevices(@Param("labId") Long labId,
                             @Param("status") String status,
                             @Param("keyword") String keyword,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    long countDevices(@Param("labId") Long labId, @Param("status") String status, @Param("keyword") String keyword);

    int insertDevice(Device device);

    int updateDevice(Device device);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
