package com.example.demo.repository;

import com.example.demo.entity.Ticket;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AnalyticsRepository extends JpaRepository<Ticket, Long> {

    @Query("SELECT t.category, COUNT(t) FROM Ticket t GROUP BY t.category")
    List<Object[]> getCategoryAnalytics();

    @Query(
        value = """
            SELECT DATE_FORMAT(created_at, '%b') as month,
                   COUNT(*) as total
            FROM tickets
            GROUP BY month
            ORDER BY MIN(created_at)
        """,
        nativeQuery = true
    )
    List<Object[]> getMonthlyTrend();

    @Query("SELECT t.status, COUNT(t) FROM Ticket t GROUP BY t.status")
    List<Object[]> getStatusAnalytics();
}