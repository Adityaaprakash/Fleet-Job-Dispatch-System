package com.fleet.dispatch.service;

import com.fleet.dispatch.dto.request.CreateJobRequest;
import com.fleet.dispatch.dto.request.UpdateJobStatusRequest;
import com.fleet.dispatch.dto.response.JobResponse;
import com.fleet.dispatch.entity.Driver;
import com.fleet.dispatch.entity.Job;
import com.fleet.dispatch.entity.JobStatusLog;
import com.fleet.dispatch.enums.JobStatus;
import com.fleet.dispatch.exception.DriverNotFoundException;
import com.fleet.dispatch.exception.DriverUnavailableException;
import com.fleet.dispatch.exception.InvalidStatusTransitionException;
import com.fleet.dispatch.exception.JobNotFoundException;
import com.fleet.dispatch.mapper.JobMapper;
import com.fleet.dispatch.repository.DriverRepository;
import com.fleet.dispatch.repository.JobRepository;
import com.fleet.dispatch.repository.JobStatusLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final DriverRepository driverRepository;
    private final JobStatusLogRepository jobStatusLogRepository;
    private final JobMapper jobMapper;

    @Transactional
    public JobResponse createJob(CreateJobRequest request) {
        Job job = Job.builder()
                .description(request.getDescription())
                .pickupAddress(request.getPickupAddress())
                .deliveryAddress(request.getDeliveryAddress())
                .scheduledAt(request.getScheduledAt())
                .status(JobStatus.UNASSIGNED)
                .build();

        Job savedJob = jobRepository.save(job);
        return jobMapper.toResponse(savedJob);
    }

    @Transactional
    public JobResponse assignJob(Long jobId, Long driverId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));

        Driver driver = driverRepository.findById(driverId)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found: " + driverId));

        jobRepository.findActiveJobByDriverId(driverId)
                .ifPresent(activeJob -> {
                    throw new DriverUnavailableException("Driver " + driverId + " is currently on an active job");
                });

        JobStatus previousStatus = job.getStatus();
        job.setDriver(driver);
        job.setStatus(JobStatus.ASSIGNED);
        Job savedJob = jobRepository.save(job);

        JobStatusLog log = JobStatusLog.builder()
                .job(savedJob)
                .fromStatus(previousStatus)
                .toStatus(JobStatus.ASSIGNED)
                .notes("Assigned to driver: " + driver.getName())
                .build();
        jobStatusLogRepository.save(log);

        return jobMapper.toResponse(savedJob);
    }

    @Transactional
    public JobResponse updateJobStatus(Long jobId, UpdateJobStatusRequest request) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));

        JobStatus previousStatus = job.getStatus();
        JobStatus newStatus = request.getNewStatus();

        validateTransition(previousStatus, newStatus);

        job.setStatus(newStatus);
        Job savedJob = jobRepository.save(job);

        JobStatusLog log = JobStatusLog.builder()
                .job(savedJob)
                .fromStatus(previousStatus)
                .toStatus(newStatus)
                .notes("Status updated")
                .build();
        jobStatusLogRepository.save(log);

        return jobMapper.toResponse(savedJob);
    }

    private void validateTransition(JobStatus from, JobStatus to) {
        // Terminal statuses (COMPLETED, CANCELLED) cannot transition to anything
        if (from == JobStatus.COMPLETED || from == JobStatus.CANCELLED) {
            throw new InvalidStatusTransitionException("Invalid transition: " + from + " -> " + to);
        }

        // Any job -> CANCELLED (CANCELLED is always a legal target from any non-terminal status)
        if (to == JobStatus.CANCELLED) {
            return;
        }

        // Legal transitions:
        // UNASSIGNED -> ASSIGNED
        // ASSIGNED -> IN_PROGRESS
        // IN_PROGRESS -> COMPLETED
        boolean isValid = switch (from) {
            case UNASSIGNED -> to == JobStatus.ASSIGNED;
            case ASSIGNED -> to == JobStatus.IN_PROGRESS;
            case IN_PROGRESS -> to == JobStatus.COMPLETED;
            default -> false;
        };

        if (!isValid) {
            throw new InvalidStatusTransitionException("Invalid transition: " + from + " -> " + to);
        }
    }

    public List<JobResponse> getJobsByDriverAndDate(Long driverId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.atTime(23, 59, 59);
        return jobRepository.findAllByDriverIdAndScheduledAtBetween(driverId, start, end)
                .stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobResponse> getAllJobs() {
        return jobRepository.findAll()
                .stream()
                .map(jobMapper::toResponse)
                .collect(Collectors.toList());
    }

    public JobResponse getJobById(Long jobId) {
        return jobRepository.findById(jobId)
                .map(jobMapper::toResponse)
                .orElseThrow(() -> new JobNotFoundException("Job not found: " + jobId));
    }
}
