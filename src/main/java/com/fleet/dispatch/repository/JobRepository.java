package com.fleet.dispatch.repository;

import com.fleet.dispatch.entity.Job;
import com.fleet.dispatch.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findAllByStatus(JobStatus status);
    List<Job> findAllByDriverIdAndScheduledAtBetween(Long driverId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT j FROM Job j WHERE j.driver.id = :driverId AND (j.status = 'IN_PROGRESS' OR j.status = 'ASSIGNED')")
    Optional<Job> findActiveJobByDriverId(@Param("driverId") Long driverId);
}
