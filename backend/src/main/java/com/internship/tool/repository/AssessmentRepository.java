package com.internship.tool.repository;

import com.internship.tool.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    List<Assessment> findByStatus(String status);

    @Query("SELECT a FROM Assessment a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Assessment> searchByKeyword(@Param("keyword") String keyword);
}