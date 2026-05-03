package com.internship.tool.repository;

import com.internship.tool.entity.Assessment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AssessmentRepository extends JpaRepository<Assessment, Long> {

    @EntityGraph(attributePaths = {"createdBy"})
    List<Assessment> findByStatus(String status);

    @EntityGraph(attributePaths = {"createdBy"})
    Page<Assessment> findByStatus(String status, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy"})
    @Override
    Page<Assessment> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy"})
    @Override
    List<Assessment> findAll();

    @Query("SELECT a FROM Assessment a JOIN FETCH a.createdBy")
    List<Assessment> findAllWithUser();

    @EntityGraph(attributePaths = {"createdBy"})
    @Query("SELECT a FROM Assessment a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<Assessment> optimizedSearch(@Param("q") String q);
}
