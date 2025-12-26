package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.util.TimeUtil;
import com.example.labmanagementsystembackend.domain.entity.RuleConfig;
import com.example.labmanagementsystembackend.dto.response.RuleConfigResponse;
import com.example.labmanagementsystembackend.mapper.RuleConfigMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RuleConfigService {
    private final RuleConfigMapper ruleConfigMapper;
    private final ObjectMapper objectMapper;

    public RuleConfigService(RuleConfigMapper ruleConfigMapper, ObjectMapper objectMapper) {
        this.ruleConfigMapper = ruleConfigMapper;
        this.objectMapper = objectMapper;
    }

    public List<RuleConfigResponse> listRules() {
        return ruleConfigMapper.findAll().stream()
                .map(RuleConfigService::toResponse)
                .collect(Collectors.toList());
    }

    public void updateRule(String key, Object value, String description) {
        RuleConfig config = new RuleConfig();
        config.setKeyName(key);
        config.setDescription(description);
        config.setValue(toJson(value));
        ruleConfigMapper.upsertRule(config);
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Invalid rule config value", ex);
        }
    }

    private static RuleConfigResponse toResponse(RuleConfig config) {
        return new RuleConfigResponse(config.getId(), config.getKeyName(), config.getValue(), config.getDescription(),
                TimeUtil.fromUtcLocalDateTime(config.getUpdatedAt()));
    }
}
