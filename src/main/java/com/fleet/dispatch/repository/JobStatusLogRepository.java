package com.fleet.dispatch.repository;

import com.fleet.dispatch.entity.JobStatusLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobStatusLogRepository extends JpaRepository<JobStatusLog, Long> {
    List<JobStatusLog> findAllByJobIdOrderByChangedAtAsc(Long jobId);
}
