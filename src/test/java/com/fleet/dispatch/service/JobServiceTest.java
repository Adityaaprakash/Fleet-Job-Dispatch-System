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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private JobStatusLogRepository jobStatusLogRepository;

    @Mock
    private JobMapper jobMapper;

    @InjectMocks
    private JobService jobService;

    @Test
    void createJob_shouldSaveWithUnassignedStatus_andReturnMappedResponse() {
        // Arrange
        CreateJobRequest request = CreateJobRequest.builder()
                .description("Test Job")
                .pickupAddress("Pickup 1")
                .deliveryAddress("Delivery 1")
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .build();

        Job savedJob = Job.builder()
                .id(1L)
                .status(JobStatus.UNASSIGNED)
                .build();

        JobResponse response = new JobResponse(1L, "Test Job", "Pickup 1", "Delivery 1", JobStatus.UNASSIGNED, savedJob.getScheduledAt(), null, null);

        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);
        when(jobMapper.toResponse(savedJob)).thenReturn(response);

        // Act
        JobResponse result = jobService.createJob(request);

        // Assert
        assertThat(result).isNotNull();
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getStatus()).isEqualTo(JobStatus.UNASSIGNED);
    }

    @Test
    void assignJob_shouldThrowJobNotFoundException_whenJobMissing() {
        // Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobService.assignJob(1L, 1L))
                .isInstanceOf(JobNotFoundException.class);
        
        verify(driverRepository, never()).findById(any());
    }

    @Test
    void assignJob_shouldThrowDriverNotFoundException_whenDriverMissing() {
        // Arrange
        Job job = Job.builder().id(1L).status(JobStatus.UNASSIGNED).build();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(driverRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobService.assignJob(1L, 1L))
                .isInstanceOf(DriverNotFoundException.class);
    }

    @Test
    void assignJob_shouldThrowDriverUnavailableException_whenDriverHasActiveJob() {
        // Arrange
        Job job = Job.builder().id(1L).status(JobStatus.UNASSIGNED).build();
        Driver driver = Driver.builder().id(1L).name("John Doe").build();
        Job activeJob = Job.builder().id(2L).status(JobStatus.ASSIGNED).build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(jobRepository.findActiveJobByDriverId(1L)).thenReturn(Optional.of(activeJob));

        // Act & Assert
        assertThatThrownBy(() -> jobService.assignJob(1L, 1L))
                .isInstanceOf(DriverUnavailableException.class);
        
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void assignJob_shouldSetStatusAssignedAndSaveLog_onSuccess() {
        // Arrange
        Job job = Job.builder().id(1L).status(JobStatus.UNASSIGNED).build();
        Driver driver = Driver.builder().id(1L).name("John Doe").build();
        
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(driverRepository.findById(1L)).thenReturn(Optional.of(driver));
        when(jobRepository.findActiveJobByDriverId(1L)).thenReturn(Optional.empty());
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        jobService.assignJob(1L, 1L);

        // Assert
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getStatus()).isEqualTo(JobStatus.ASSIGNED);
        assertThat(jobCaptor.getValue().getDriver()).isEqualTo(driver);

        ArgumentCaptor<JobStatusLog> logCaptor = ArgumentCaptor.forClass(JobStatusLog.class);
        verify(jobStatusLogRepository, times(1)).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getFromStatus()).isEqualTo(JobStatus.UNASSIGNED);
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(JobStatus.ASSIGNED);
    }

    @Test
    void updateJobStatus_shouldThrowInvalidStatusTransitionException_forIllegalTransition() {
        // Arrange
        Job job = Job.builder().id(1L).status(JobStatus.COMPLETED).build();
        UpdateJobStatusRequest request = UpdateJobStatusRequest.builder().newStatus(JobStatus.IN_PROGRESS).build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));

        // Act & Assert
        assertThatThrownBy(() -> jobService.updateJobStatus(1L, request))
                .isInstanceOf(InvalidStatusTransitionException.class);
        
        verify(jobRepository, never()).save(any(Job.class));
    }

    @Test
    void updateJobStatus_shouldSucceed_forLegalTransition() {
        // Arrange
        Job job = Job.builder().id(1L).status(JobStatus.ASSIGNED).build();
        UpdateJobStatusRequest request = UpdateJobStatusRequest.builder().newStatus(JobStatus.IN_PROGRESS).build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        jobService.updateJobStatus(1L, request);

        // Assert
        ArgumentCaptor<Job> jobCaptor = ArgumentCaptor.forClass(Job.class);
        verify(jobRepository).save(jobCaptor.capture());
        assertThat(jobCaptor.getValue().getStatus()).isEqualTo(JobStatus.IN_PROGRESS);

        ArgumentCaptor<JobStatusLog> logCaptor = ArgumentCaptor.forClass(JobStatusLog.class);
        verify(jobStatusLogRepository).save(logCaptor.capture());
        assertThat(logCaptor.getValue().getFromStatus()).isEqualTo(JobStatus.ASSIGNED);
        assertThat(logCaptor.getValue().getToStatus()).isEqualTo(JobStatus.IN_PROGRESS);
    }

    @Test
    void updateJobStatus_shouldAllowCancellation_fromAnyNonTerminalStatus() {
        // Arrange
        Job job = Job.builder().id(1L).status(JobStatus.ASSIGNED).build();
        UpdateJobStatusRequest request = UpdateJobStatusRequest.builder().newStatus(JobStatus.CANCELLED).build();

        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        jobService.updateJobStatus(1L, request);

        // Assert
        verify(jobRepository).save(any(Job.class));
    }

    @Test
    void getJobById_shouldThrowJobNotFoundException_whenMissing() {
        // Arrange
        when(jobRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> jobService.getJobById(1L))
                .isInstanceOf(JobNotFoundException.class);
    }
}
