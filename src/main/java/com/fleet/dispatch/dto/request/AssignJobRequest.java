package com.fleet.dispatch.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AssignJobRequest {

    @NotNull(message = "Driver ID is required")
    private Long driverId;
}
