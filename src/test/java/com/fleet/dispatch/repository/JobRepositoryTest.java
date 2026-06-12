package com.fleet.dispatch.repository;

import com.fleet.dispatch.entity.Driver;
import com.fleet.dispatch.entity.Job;
import com.fleet.dispatch.enums.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class JobRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private JobRepository jobRepository;

    @Test
    void findAllByDriverIdAndScheduledAtBetween_shouldReturnOnlyJobsInRange() {
        // Arrange
        Driver driver = Driver.builder().name("John").licenseNumber("ABC123").build();
        entityManager.persist(driver);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = now.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        Job yesterday = Job.builder()
                .description("Yesterday")
                .pickupAddress("A").deliveryAddress("B")
                .status(JobStatus.UNASSIGNED)
                .scheduledAt(now.minusDays(1))
                .driver(driver)
                .build();
        
        Job today = Job.builder()
                .description("Today")
                .pickupAddress("A").deliveryAddress("B")
                .status(JobStatus.UNASSIGNED)
                .scheduledAt(now)
                .driver(driver)
                .build();

        Job tomorrow = Job.builder()
                .description("Tomorrow")
                .pickupAddress("A").deliveryAddress("B")
                .status(JobStatus.UNASSIGNED)
                .scheduledAt(now.plusDays(1))
                .driver(driver)
                .build();

        entityManager.persist(yesterday);
        entityManager.persist(today);
        entityManager.persist(tomorrow);
        entityManager.flush();

        // Act
        List<Job> jobs = jobRepository.findAllByDriverIdAndScheduledAtBetween(driver.getId(), startOfDay, endOfDay);

        // Assert
        assertThat(jobs).hasSize(1);
        assertThat(jobs.get(0).getDescription()).isEqualTo("Today");
    }

    @Test
    void findActiveJobByDriverId_shouldReturnJob_whenStatusIsInProgressOrAssigned() {
        // Arrange
        Driver driver = Driver.builder().name("John").licenseNumber("ABC1234").build();
        entityManager.persist(driver);

        Job job = Job.builder()
                .description("Active Job")
                .pickupAddress("A").deliveryAddress("B")
                .status(JobStatus.ASSIGNED)
                .scheduledAt(LocalDateTime.now())
                .driver(driver)
                .build();
        
        entityManager.persist(job);
        entityManager.flush();

        // Act
        Optional<Job> found = jobRepository.findActiveJobByDriverId(driver.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(JobStatus.ASSIGNED);
    }

    @Test
    void findActiveJobByDriverId_shouldReturnEmpty_whenDriverHasNoActiveJob() {
        // Arrange
        Driver driver = Driver.builder().name("John").licenseNumber("ABC12345").build();
        entityManager.persist(driver);

        Job job = Job.builder()
                .description("Completed Job")
                .pickupAddress("A").deliveryAddress("B")
                .status(JobStatus.COMPLETED)
                .scheduledAt(LocalDateTime.now())
                .driver(driver)
                .build();
        
        entityManager.persist(job);
        entityManager.flush();

        // Act
        Optional<Job> found = jobRepository.findActiveJobByDriverId(driver.getId());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void findActiveJobByDriverId_shouldReturnEmpty_whenDriverHasNoJobsAtAll() {
        // Arrange
        Driver driver = Driver.builder().name("John").licenseNumber("ABC123456").build();
        entityManager.persist(driver);
        entityManager.flush();

        // Act
        Optional<Job> found = jobRepository.findActiveJobByDriverId(driver.getId());

        // Assert
        assertThat(found).isEmpty();
    }
}
