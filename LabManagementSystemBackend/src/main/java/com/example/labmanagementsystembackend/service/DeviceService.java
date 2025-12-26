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

    public DeviceService(DeviceMapper deviceMapper, LabService labService) {
        this.deviceMapper = deviceMapper;
        this.labService = labService;
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

    public DeviceResponse createDevice(DeviceCreateRequest request) {
        labService.getLabEntity(request.getLabId());
        Device device = new Device();
        device.setLabId(request.getLabId());
        device.setName(request.getName());
        device.setModel(request.getModel());
        device.setStatus("IDLE");
        deviceMapper.insertDevice(device);
        return toResponse(deviceMapper.findById(device.getId()));
    }

    public DeviceResponse updateDevice(Long id, DeviceUpdateRequest request) {
        Device existing = deviceMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Device not found");
        }
        labService.getLabEntity(request.getLabId());
        existing.setLabId(request.getLabId());
        existing.setName(request.getName());
        existing.setModel(request.getModel());
        deviceMapper.updateDevice(existing);
        return toResponse(deviceMapper.findById(id));
    }

    public void updateStatus(Long id, String status) {
        Device existing = deviceMapper.findById(id);
        if (existing == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Device not found");
        }
        deviceMapper.updateStatus(id, status);
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
