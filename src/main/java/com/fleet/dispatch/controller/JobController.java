package com.fleet.dispatch.controller;

import com.fleet.dispatch.dto.request.AssignJobRequest;
import com.fleet.dispatch.dto.request.CreateJobRequest;
import com.fleet.dispatch.dto.request.UpdateJobStatusRequest;
import com.fleet.dispatch.dto.response.JobResponse;
import com.fleet.dispatch.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody CreateJobRequest request) {
        JobResponse response = jobService.createJob(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<JobResponse>> getAllJobs() {
        List<JobResponse> response = jobService.getAllJobs();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJobById(@PathVariable Long id) {
        JobResponse response = jobService.getJobById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<JobResponse> assignJob(
            @PathVariable Long id,
            @Valid @RequestBody AssignJobRequest request) {
        JobResponse response = jobService.assignJob(id, request.getDriverId());
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<JobResponse> updateJobStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateJobStatusRequest request) {
        JobResponse response = jobService.updateJobStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
