package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.domain.entity.Lab;
import com.example.labmanagementsystembackend.dto.request.LabCreateRequest;
import com.example.labmanagementsystembackend.dto.request.LabUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.LabResponse;
import com.example.labmanagementsystembackend.mapper.LabMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabService {
    private final LabMapper labMapper;
    private final AuditLogService auditLogService;

    public LabService(LabMapper labMapper, AuditLogService auditLogService) {
        this.labMapper = labMapper;
        this.auditLogService = auditLogService;
    }

    public List<LabResponse> listLabs(String status, String keyword, int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        return labMapper.findLabs(status, keyword, PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(LabService::toResponse)
                .collect(Collectors.toList());
    }

    public long countLabs(String status, String keyword) {
        return labMapper.countLabs(status, keyword);
    }

    public LabResponse createLab(Long actorId, LabCreateRequest request) {
        Lab lab = new Lab();
        lab.setName(request.getName());
        lab.setLocation(request.getLocation());
        lab.setCapacity(request.getCapacity());
        lab.setOpenTimeStart(request.getOpenTimeStart());
        lab.setOpenTimeEnd(request.getOpenTimeEnd());
        lab.setStatus("IDLE");
        labMapper.insertLab(lab);

        // 记录审计日志
        String detail = String.format("{\"name\":\"%s\",\"location\":\"%s\",\"capacity\":%d}",
                lab.getName(), lab.getLocation(), lab.getCapacity());
        auditLogService.record(actorId, "CREATE", "LAB", lab.getId(), detail);

        return toResponse(labMapper.findById(lab.getId()));
    }

    public LabResponse updateLab(Long actorId, Long id, LabUpdateRequest request) {
        Lab existing = labMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Lab not found");
        }

        // 记录变更前的信息
        String oldName = existing.getName();
        String oldLocation = existing.getLocation();
        Integer oldCapacity = existing.getCapacity();

        existing.setName(request.getName());
        existing.setLocation(request.getLocation());
        existing.setCapacity(request.getCapacity());
        // 只在值不为 null 时更新开放时间
        if (request.getOpenTimeStart() != null) {
            existing.setOpenTimeStart(request.getOpenTimeStart());
        }
        if (request.getOpenTimeEnd() != null) {
            existing.setOpenTimeEnd(request.getOpenTimeEnd());
        }
        labMapper.updateLab(existing);

        // 记录审计日志
        String detail = String.format("{\"name\":\"%s\",\"location\":\"%s\",\"capacity\":%d,\"oldName\":\"%s\",\"oldLocation\":\"%s\",\"oldCapacity\":%d}",
                existing.getName(), existing.getLocation(), existing.getCapacity(), oldName, oldLocation, oldCapacity);
        auditLogService.record(actorId, "UPDATE", "LAB", id, detail);

        return toResponse(labMapper.findById(id));
    }

    public void updateStatus(Long actorId, Long id, String status) {
        Lab existing = labMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Lab not found");
        }

        String oldStatus = existing.getStatus();
        labMapper.updateStatus(id, status);

        // 记录审计日志
        String detail = String.format("{\"labName\":\"%s\",\"newStatus\":\"%s\",\"oldStatus\":\"%s\"}",
                existing.getName(), status, oldStatus);
        auditLogService.record(actorId, "UPDATE", "LAB", id, detail);
    }

    public Lab getLabEntity(Long id) {
        Lab lab = labMapper.findById(id);
        if (lab == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Lab not found");
        }
        return lab;
    }

    public static LabResponse toResponse(Lab lab) {
        return new LabResponse(lab.getId(), lab.getName(), lab.getLocation(), lab.getCapacity(),
                lab.getOpenTimeStart(), lab.getOpenTimeEnd(), lab.getStatus());
    }
}
