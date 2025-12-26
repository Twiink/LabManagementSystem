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

    public LabService(LabMapper labMapper) {
        this.labMapper = labMapper;
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

    public LabResponse createLab(LabCreateRequest request) {
        Lab lab = new Lab();
        lab.setName(request.getName());
        lab.setLocation(request.getLocation());
        lab.setCapacity(request.getCapacity());
        lab.setOpenTimeStart(request.getOpenTimeStart());
        lab.setOpenTimeEnd(request.getOpenTimeEnd());
        lab.setStatus("IDLE");
        labMapper.insertLab(lab);
        return toResponse(labMapper.findById(lab.getId()));
    }

    public LabResponse updateLab(Long id, LabUpdateRequest request) {
        Lab existing = labMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Lab not found");
        }
        existing.setName(request.getName());
        existing.setLocation(request.getLocation());
        existing.setCapacity(request.getCapacity());
        existing.setOpenTimeStart(request.getOpenTimeStart());
        existing.setOpenTimeEnd(request.getOpenTimeEnd());
        labMapper.updateLab(existing);
        return toResponse(labMapper.findById(id));
    }

    public void updateStatus(Long id, String status) {
        Lab existing = labMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Lab not found");
        }
        labMapper.updateStatus(id, status);
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
