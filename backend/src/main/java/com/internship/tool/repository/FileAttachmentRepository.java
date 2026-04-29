package com.internship.tool.repository;

import com.internship.tool.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for FileAttachment entity.
 */
@Repository
public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    List<FileAttachment> findByControlId(Long controlId);

    Optional<FileAttachment> findByUuidFilename(String uuidFilename);
}
