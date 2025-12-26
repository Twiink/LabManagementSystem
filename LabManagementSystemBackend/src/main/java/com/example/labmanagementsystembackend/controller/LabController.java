package com.example.labmanagementsystembackend.controller;

import com.example.labmanagementsystembackend.common.api.ApiResponse;
import com.example.labmanagementsystembackend.common.api.PageResponse;
import com.example.labmanagementsystembackend.dto.request.LabCreateRequest;
import com.example.labmanagementsystembackend.dto.request.LabStatusUpdateRequest;
import com.example.labmanagementsystembackend.dto.request.LabUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.LabResponse;
import com.example.labmanagementsystembackend.service.LabService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/labs")
public class LabController extends BaseController {
    private final LabService labService;

    public LabController(LabService labService) {
        this.labService = labService;
    }

    @GetMapping
    public ApiResponse<PageResponse<LabResponse>> listLabs(@RequestParam(required = false) String status,
                                                           @RequestParam(required = false) String keyword,
                                                           @RequestParam(defaultValue = "1") int page,
                                                           @RequestParam(defaultValue = "20") int pageSize,
                                                           HttpServletRequest request) {
        List<LabResponse> labs = labService.listLabs(status, keyword, page, pageSize);
        long total = labService.countLabs(status, keyword);
        return success(request, new PageResponse<>(labs, page, pageSize, total));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LabResponse> createLab(@Valid @RequestBody LabCreateRequest body, HttpServletRequest request) {
        return success(request, labService.createLab(body));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<LabResponse> updateLab(@PathVariable Long id,
                                              @Valid @RequestBody LabUpdateRequest body,
                                              HttpServletRequest request) {
        return success(request, labService.updateLab(id, body));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> updateStatus(@PathVariable Long id,
                                          @Valid @RequestBody LabStatusUpdateRequest body,
                                          HttpServletRequest request) {
        labService.updateStatus(id, body.getStatus());
        return success(request, null);
    }
}
