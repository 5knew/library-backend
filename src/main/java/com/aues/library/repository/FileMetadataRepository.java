package com.aues.library.repository;

import com.aues.library.model.Author;
import com.aues.library.model.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long>, JpaSpecificationExecutor<Author> {

    List<FileMetadata> findByBookCopy_Id(Long bookCopyId);
}

