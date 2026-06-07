package com.fleet.dispatch.dto.response;

import com.fleet.dispatch.enums.DriverStatus;

public record DriverResponse(
        Long id,
        String name,
        String licenseNumber,
        String phoneNumber,
        DriverStatus status
) {
}
