package com.example.labmanagementsystembackend.service;

import com.example.labmanagementsystembackend.common.error.BusinessException;
import com.example.labmanagementsystembackend.common.error.ErrorCode;
import com.example.labmanagementsystembackend.common.util.PageUtil;
import com.example.labmanagementsystembackend.domain.entity.Device;
import com.example.labmanagementsystembackend.dto.request.DeviceCreateRequest;
import com.example.labmanagementsystembackend.dto.request.DeviceUpdateRequest;
import com.example.labmanagementsystembackend.dto.response.DeviceResponse;
import com.example.labmanagementsystembackend.mapper.DeviceMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DeviceService {
    private final DeviceMapper deviceMapper;
    private final LabService labService;
    private final AuditLogService auditLogService;

    public DeviceService(DeviceMapper deviceMapper, LabService labService, AuditLogService auditLogService) {
        this.deviceMapper = deviceMapper;
        this.labService = labService;
        this.auditLogService = auditLogService;
    }

    public List<DeviceResponse> listDevices(Long labId, String status, String keyword, int page, int pageSize) {
        int safePage = PageUtil.normalizePage(page);
        int safeSize = PageUtil.normalizePageSize(pageSize);
        return deviceMapper.findDevices(labId, status, keyword, PageUtil.offset(safePage, safeSize), safeSize)
                .stream()
                .map(DeviceService::toResponse)
                .collect(Collectors.toList());
    }

    public long countDevices(Long labId, String status, String keyword) {
        return deviceMapper.countDevices(labId, status, keyword);
    }

    public DeviceResponse createDevice(Long actorId, DeviceCreateRequest request) {
        labService.getLabEntity(request.getLabId());
        Device device = new Device();
        device.setLabId(request.getLabId());
        device.setName(request.getName());
        device.setModel(request.getModel());
        device.setStatus("IDLE");
        deviceMapper.insertDevice(device);

        // 记录审计日志
        String detail = String.format("{\"name\":\"%s\",\"model\":\"%s\",\"labId\":%d}",
                device.getName(), device.getModel(), device.getLabId());
        auditLogService.record(actorId, "CREATE", "DEVICE", device.getId(), detail);

        return toResponse(deviceMapper.findById(device.getId()));
    }

    public DeviceResponse updateDevice(Long actorId, Long id, DeviceUpdateRequest request) {
        Device existing = deviceMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Device not found");
        }
        labService.getLabEntity(request.getLabId());

        // 记录变更前的信息
        String oldName = existing.getName();
        String oldModel = existing.getModel();
        Long oldLabId = existing.getLabId();

        existing.setLabId(request.getLabId());
        existing.setName(request.getName());
        existing.setModel(request.getModel());
        deviceMapper.updateDevice(existing);

        // 记录审计日志
        String detail = String.format("{\"name\":\"%s\",\"model\":\"%s\",\"labId\":%d,\"oldName\":\"%s\",\"oldModel\":\"%s\",\"oldLabId\":%d}",
                existing.getName(), existing.getModel(), existing.getLabId(), oldName, oldModel, oldLabId);
        auditLogService.record(actorId, "UPDATE", "DEVICE", id, detail);

        return toResponse(deviceMapper.findById(id));
    }

    public void updateStatus(Long actorId, Long id, String status) {
        Device existing = deviceMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Device not found");
        }

        String oldStatus = existing.getStatus();
        deviceMapper.updateStatus(id, status);

        // 记录审计日志
        String detail = String.format("{\"deviceName\":\"%s\",\"newStatus\":\"%s\",\"oldStatus\":\"%s\"}",
                existing.getName(), status, oldStatus);
        auditLogService.record(actorId, "UPDATE", "DEVICE", id, detail);
    }

    public Device getDeviceEntity(Long id) {
        Device device = deviceMapper.findById(id);
        if (device == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Device not found");
        }
        return device;
    }

    public static DeviceResponse toResponse(Device device) {
        return new DeviceResponse(device.getId(), device.getLabId(), device.getName(), device.getModel(), device.getStatus());
    }
}
