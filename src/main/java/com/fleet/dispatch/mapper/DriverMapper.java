package com.fleet.dispatch.mapper;

import com.fleet.dispatch.dto.response.DriverResponse;
import com.fleet.dispatch.entity.Driver;
import org.springframework.stereotype.Component;

@Component
public class DriverMapper {

    public DriverResponse toResponse(Driver driver) {
        if (driver == null) {
            return null;
        }

        return new DriverResponse(
                driver.getId(),
                driver.getName(),
                driver.getLicenseNumber(),
                driver.getPhoneNumber(),
                driver.getStatus()
        );
    }
}
