package com.internship.tool.repository;

import com.internship.tool.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    // Search by keyword in name or description
    @Query("""
           SELECT a FROM Assessment a
           WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    List<Assessment> searchByKeyword(@Param("keyword") String keyword);

    // Find by status
    List<Assessment> findByStatus(String status);
}