package com.fleet.dispatch.dto.response;

import com.fleet.dispatch.enums.JobStatus;
import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        String description,
        String pickupAddress,
        String deliveryAddress,
        JobStatus status,
        LocalDateTime scheduledAt,
        Long driverId,
        String driverName
) {
}
