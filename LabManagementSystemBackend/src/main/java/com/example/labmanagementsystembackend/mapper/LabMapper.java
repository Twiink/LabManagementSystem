package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.Lab;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LabMapper {
    Lab findById(@Param("id") Long id);

    List<Lab> findLabs(@Param("status") String status,
                       @Param("keyword") String keyword,
                       @Param("offset") int offset,
                       @Param("pageSize") int pageSize);

    long countLabs(@Param("status") String status, @Param("keyword") String keyword);

    int insertLab(Lab lab);

    int updateLab(Lab lab);

    int updateStatus(@Param("id") Long id, @Param("status") String status);
}
