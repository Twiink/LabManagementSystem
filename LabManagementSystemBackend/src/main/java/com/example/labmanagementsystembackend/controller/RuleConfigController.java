package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.dto.request.RuleConfigUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.RuleConfigResponse;
import com.example.labmanagementsystembackend.service.RuleConfigService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rule-configs")
public class RuleConfigController extends BaseController {
    private final RuleConfigService ruleConfigService;

    public RuleConfigController(RuleConfigService ruleConfigService) {
        this.ruleConfigService = ruleConfigService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<RuleConfigResponse>> listRules(HttpServletRequest request) {
        return success(request, ruleConfigService.listRules());
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateRule(@PathVariable String key,
                                        @Valid @RequestBody RuleConfigUpdateRequest body,
                                        HttpServletRequest request) {
        ruleConfigService.updateRule(key, body.getValue(), body.getDescription());
        return success(request, null);
    }
}
