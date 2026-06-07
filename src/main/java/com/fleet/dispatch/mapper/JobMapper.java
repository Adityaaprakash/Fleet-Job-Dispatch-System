package com.fleet.dispatch.mapper;

import com.fleet.dispatch.dto.response.JobResponse;
import com.fleet.dispatch.dto.response.JobStatusLogResponse;
import com.fleet.dispatch.entity.Job;
import com.fleet.dispatch.entity.JobStatusLog;
import org.springframework.stereotype.Component;

@Component
public class JobMapper {

    public JobResponse toResponse(Job job) {
        if (job == null) {
            return null;
        }

        Long driverId = null;
        String driverName = null;

        if (job.getDriver() != null) {
            driverId = job.getDriver().getId();
            driverName = job.getDriver().getName();
        }

        return new JobResponse(
                job.getId(),
                job.getDescription(),
                job.getPickupAddress(),
                job.getDeliveryAddress(),
                job.getStatus(),
                job.getScheduledAt(),
                driverId,
                driverName
        );
    }

    public JobStatusLogResponse toLogResponse(JobStatusLog log) {
        if (log == null) {
            return null;
        }

        return new JobStatusLogResponse(
                log.getId(),
                log.getJob() != null ? log.getJob().getId() : null,
                log.getFromStatus(),
                log.getToStatus(),
                log.getChangedAt(),
                log.getNotes()
        );
    }
}
