package com.fleet.dispatch.service;

import com.fleet.dispatch.dto.response.DriverResponse;
import com.fleet.dispatch.entity.Driver;
import com.fleet.dispatch.enums.DriverStatus;
import com.fleet.dispatch.exception.DriverNotFoundException;
import com.fleet.dispatch.mapper.DriverMapper;
import com.fleet.dispatch.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;
    private final DriverMapper driverMapper;

    public List<DriverResponse> getAllDrivers() {
        return driverRepository.findAll()
                .stream()
                .map(driverMapper::toResponse)
                .collect(Collectors.toList());
    }

    public DriverResponse getDriverById(Long driverId) {
        return driverRepository.findById(driverId)
                .map(driverMapper::toResponse)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));
    }

    public List<DriverResponse> getDriversByStatus(DriverStatus status) {
        return driverRepository.findAllByStatus(status)
                .stream()
                .map(driverMapper::toResponse)
                .collect(Collectors.toList());
    }
}
