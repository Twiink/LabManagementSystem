package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.ReservationSeriesItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReservationSeriesItemMapper {
    int insertItem(ReservationSeriesItem item);
}
