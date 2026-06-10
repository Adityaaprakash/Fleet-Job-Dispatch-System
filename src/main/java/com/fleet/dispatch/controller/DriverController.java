package com.fleet.dispatch.controller;

import com.fleet.dispatch.dto.response.DriverResponse;
import com.fleet.dispatch.dto.response.JobResponse;
import com.fleet.dispatch.enums.DriverStatus;
import com.fleet.dispatch.service.DriverService;
import com.fleet.dispatch.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
public class DriverController {

    private final DriverService driverService;
    private final JobService jobService;

    @GetMapping
    public ResponseEntity<List<DriverResponse>> getDrivers(
            @RequestParam(required = false) DriverStatus status) {
        if (status != null) {
            // Note: Called driverService instead of jobService as the method exists in DriverService
            return ResponseEntity.ok(driverService.getDriversByStatus(status));
        }
        return ResponseEntity.ok(driverService.getAllDrivers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<List<JobResponse>> getDriverJobs(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(jobService.getJobsByDriverAndDate(id, date));
    }
}
