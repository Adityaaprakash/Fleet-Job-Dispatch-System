package com.fleet.dispatch.config;

import com.fleet.dispatch.entity.Driver;
import com.fleet.dispatch.entity.Job;
import com.fleet.dispatch.entity.JobStatusLog;
import com.fleet.dispatch.entity.Vehicle;
import com.fleet.dispatch.enums.DriverStatus;
import com.fleet.dispatch.enums.JobStatus;
import com.fleet.dispatch.repository.DriverRepository;
import com.fleet.dispatch.repository.JobRepository;
import com.fleet.dispatch.repository.JobStatusLogRepository;
import com.fleet.dispatch.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final JobRepository jobRepository;
    private final JobStatusLogRepository jobStatusLogRepository;

    @Override
    public void run(String... args) {
        if (driverRepository.count() > 0) {
            return; // Data already exists
        }

        // 1. Seed Drivers
        Driver alice = Driver.builder()
                .name("Alice Sharma")
                .licenseNumber("LIC-ALICE-123")
                .status(DriverStatus.AVAILABLE)
                .build();

        Driver bob = Driver.builder()
                .name("Bob Mensah")
                .licenseNumber("LIC-BOB-456")
                .status(DriverStatus.AVAILABLE)
                .build();

        Driver carol = Driver.builder()
                .name("Carol Diaz")
                .licenseNumber("LIC-CAROL-789")
                .status(DriverStatus.OFFLINE)
                .build();

        driverRepository.saveAll(List.of(alice, bob, carol));

        // 2. Seed Vehicles
        Vehicle van = Vehicle.builder()
                .plateNumber("VAN-001")
                .type("VAN")
                .driver(alice)
                .build();

        Vehicle truck = Vehicle.builder()
                .plateNumber("TRUCK-002")
                .type("TRUCK")
                .driver(bob)
                .build();

        Vehicle sedan = Vehicle.builder()
                .plateNumber("SEDAN-003")
                .type("SEDAN")
                .driver(null)
                .build();

        vehicleRepository.saveAll(List.of(van, truck, sedan));

        // 3. Seed Jobs
        LocalDateTime today = LocalDateTime.now();
        LocalDateTime tomorrow = today.plusDays(1);
        LocalDateTime yesterday = today.minusDays(1);

        // 2 UNASSIGNED jobs (tomorrow 09:00, 14:00)
        Job job1 = Job.builder()
                .description("Morning Delivery Tomorrow")
                .pickupAddress("A")
                .deliveryAddress("B")
                .status(JobStatus.UNASSIGNED)
                .scheduledAt(tomorrow.withHour(9).withMinute(0).withSecond(0).withNano(0))
                .build();

        Job job2 = Job.builder()
                .description("Afternoon Delivery Tomorrow")
                .pickupAddress("C")
                .deliveryAddress("D")
                .status(JobStatus.UNASSIGNED)
                .scheduledAt(tomorrow.withHour(14).withMinute(0).withSecond(0).withNano(0))
                .build();

        // 1 ASSIGNED job linked to Alice (today 11:00)
        Job job3 = Job.builder()
                .description("Midday Job Today")
                .pickupAddress("E")
                .deliveryAddress("F")
                .driver(alice)
                .status(JobStatus.ASSIGNED)
                .scheduledAt(today.withHour(11).withMinute(0).withSecond(0).withNano(0))
                .build();

        // 1 IN_PROGRESS job linked to Bob (today 08:00)
        Job job4 = Job.builder()
                .description("Early Morning Job Today")
                .pickupAddress("G")
                .deliveryAddress("H")
                .driver(bob)
                .status(JobStatus.IN_PROGRESS)
                .scheduledAt(today.withHour(8).withMinute(0).withSecond(0).withNano(0))
                .build();

        // 1 COMPLETED job linked to Alice (yesterday 10:00)
        Job job5 = Job.builder()
                .description("Yesterday's Task")
                .pickupAddress("I")
                .deliveryAddress("J")
                .driver(alice)
                .status(JobStatus.COMPLETED)
                .scheduledAt(yesterday.withHour(10).withMinute(0).withSecond(0).withNano(0))
                .build();

        jobRepository.saveAll(List.of(job1, job2, job3, job4, job5));

        // 4. Seed JobStatusLog for Bob's IN_PROGRESS job
        JobStatusLog log1 = JobStatusLog.builder()
                .job(job4)
                .fromStatus(JobStatus.UNASSIGNED)
                .toStatus(JobStatus.ASSIGNED)
                .changedAt(job4.getScheduledAt().minusMinutes(30))
                .notes("Assigned to Bob")
                .build();

        JobStatusLog log2 = JobStatusLog.builder()
                .job(job4)
                .fromStatus(JobStatus.ASSIGNED)
                .toStatus(JobStatus.IN_PROGRESS)
                .changedAt(job4.getScheduledAt())
                .notes("Bob started the job")
                .build();

        jobStatusLogRepository.saveAll(List.of(log1, log2));
    }
}
