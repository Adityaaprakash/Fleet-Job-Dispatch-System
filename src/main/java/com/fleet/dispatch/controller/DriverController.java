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
    public ResponseEntity<List<DriverResponse>> getAllDrivers(
            @RequestParam(required = false) DriverStatus status) {
        List<DriverResponse> response;
        if (status != null) {
            response = driverService.getDriversByStatus(status);
        } else {
            response = driverService.getAllDrivers();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable Long id) {
        DriverResponse response = driverService.getDriverById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/jobs")
    public ResponseEntity<List<JobResponse>> getJobsByDriverAndDate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<JobResponse> response = jobService.getJobsByDriverAndDate(id, date);
        return ResponseEntity.ok(response);
    }
}
