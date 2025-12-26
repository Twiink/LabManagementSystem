package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.ReservationSeries;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ReservationSeriesMapper {
    ReservationSeries findById(@Param("id") Long id);

    int insertSeries(ReservationSeries series);
}
