package com.fleet.dispatch.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fleet.dispatch.dto.request.AssignJobRequest;
import com.fleet.dispatch.dto.request.CreateJobRequest;
import com.fleet.dispatch.dto.request.UpdateJobStatusRequest;
import com.fleet.dispatch.dto.response.JobResponse;
import com.fleet.dispatch.enums.JobStatus;
import com.fleet.dispatch.exception.DriverUnavailableException;
import com.fleet.dispatch.exception.InvalidStatusTransitionException;
import com.fleet.dispatch.exception.JobNotFoundException;
import com.fleet.dispatch.service.JobService;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobController.class)
@MockBean(JpaMetamodelMappingContext.class)
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JobService jobService;

    @Test
    void createJob_withValidPayload_shouldReturn201() throws Exception {
        CreateJobRequest request = CreateJobRequest.builder()
                .description("Sample Job")
                .pickupAddress("Pickup")
                .deliveryAddress("Delivery")
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .build();

        JobResponse response = new JobResponse(1L, "Sample Job", "Pickup", "Delivery", JobStatus.UNASSIGNED, request.getScheduledAt(), null, null);

        when(jobService.createJob(any(CreateJobRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Sample Job"))
                .andExpect(jsonPath("$.status").value("UNASSIGNED"));
    }

    @Test
    void createJob_withMissingFields_shouldReturn400() throws Exception {
        mockMvc.perform(post("/api/jobs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void assignJob_whenDriverUnavailable_shouldReturn409() throws Exception {
        AssignJobRequest request = new AssignJobRequest(1L);
        when(jobService.assignJob(eq(1L), eq(1L))).thenThrow(new DriverUnavailableException("Driver busy"));

        mockMvc.perform(patch("/api/jobs/1/assign")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Driver busy"));
    }

    @Test
    void updateJobStatus_withIllegalTransition_shouldReturn422() throws Exception {
        UpdateJobStatusRequest request = UpdateJobStatusRequest.builder().newStatus(JobStatus.IN_PROGRESS).build();
        when(jobService.updateJobStatus(eq(1L), any(UpdateJobStatusRequest.class)))
                .thenThrow(new InvalidStatusTransitionException("Invalid transition"));

        mockMvc.perform(patch("/api/jobs/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Invalid transition"));
    }

    @Test
    void getJobById_whenNotFound_shouldReturn404() throws Exception {
        when(jobService.getJobById(999L)).thenThrow(new JobNotFoundException("Job not found: 999"));

        mockMvc.perform(get("/api/jobs/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Job not found: 999"));
    }
}
