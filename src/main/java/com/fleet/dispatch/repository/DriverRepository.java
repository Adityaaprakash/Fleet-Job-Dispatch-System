package com.fleet.dispatch.repository;

import com.fleet.dispatch.entity.Driver;
import com.fleet.dispatch.enums.DriverStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    List<Driver> findAllByStatus(DriverStatus status);
    Optional<Driver> findByLicenseNumber(String licenseNumber);
}
