package com.example.labmanagementsystembackend.mapper;

import com.example.labmanagementsystembackend.domain.entity.RuleConfig;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RuleConfigMapper {
    List<RuleConfig> findAll();

    RuleConfig findByKey(@Param("keyName") String keyName);

    int upsertRule(RuleConfig ruleConfig);
}
