package com.internship.tool.repository;

import com.internship.tool.entity.Control;
import com.internship.tool.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ControlRepository extends JpaRepository<Control, Long> {

    // Find all records by status
    List<Control> findByStatus(Status status);

    // Search by control name or owner
    @Query("""
           SELECT c FROM Control c
           WHERE LOWER(c.controlName) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(c.ownerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    List<Control> search(@Param("keyword") String keyword);

    // Find by created date range
    @Query("""
           SELECT c FROM Control c
           WHERE c.createdAt BETWEEN :startDate AND :endDate
           ORDER BY c.createdAt DESC
           """)
    List<Control> findByDateRange(@Param("startDate") LocalDateTime startDate,
                                  @Param("endDate") LocalDateTime endDate);

    // Filter by status with pagination
    Page<Control> findByStatus(Status status, Pageable pageable);

    // Dashboard stats
    long countByStatus(Status status);
}