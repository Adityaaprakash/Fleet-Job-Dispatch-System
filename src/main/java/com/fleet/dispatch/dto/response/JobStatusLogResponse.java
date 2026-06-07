package com.fleet.dispatch.dto.response;

import com.fleet.dispatch.enums.JobStatus;
import java.time.LocalDateTime;

public record JobStatusLogResponse(
        Long id,
        Long jobId,
        JobStatus fromStatus,
        JobStatus toStatus,
        LocalDateTime changedAt,
        String notes
) {
}
