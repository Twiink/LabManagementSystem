package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.Approval;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ApprovalMapper {
    int insertApproval(Approval approval);
}
