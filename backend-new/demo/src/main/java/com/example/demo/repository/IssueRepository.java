package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.dto.CategoryAnalyticsDTO;
import com.example.demo.dto.StatusAnalyticsDTO;
import com.example.demo.entity.Issue;

public interface IssueRepository
        extends JpaRepository<Issue, Long> {

    @Query("""
        SELECT new com.example.demo.dto.CategoryAnalyticsDTO(
            i.category,
            COUNT(i)
        )
        FROM Issue i
        WHERE i.createdAt BETWEEN :startDate AND :endDate
        GROUP BY i.category
    """)
    List<CategoryAnalyticsDTO> getCategoryAnalytics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
        SELECT new com.example.demo.dto.StatusAnalyticsDTO(
            i.status,
            COUNT(i)
        )
        FROM Issue i
        WHERE i.createdAt BETWEEN :startDate AND :endDate
        GROUP BY i.status
    """)
    List<StatusAnalyticsDTO> getStatusAnalytics(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query(value = """
        SELECT TO_CHAR(created_at, 'Mon') AS month,
               COUNT(*) AS count
        FROM issue
        GROUP BY month
    """, nativeQuery = true)
    List<Object[]> getMonthlyAnalytics();
}