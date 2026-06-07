package com.fleet.dispatch.dto.request;

import com.fleet.dispatch.enums.JobStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJobStatusRequest {

    @NotNull(message = "New status is required")
    private JobStatus newStatus;
}
