package com.blog.be.admin.domain.repository;

import com.blog.be.admin.domain.entity.Report;
import com.blog.be.admin.domain.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long>, JpaSpecificationExecutor<Report> {
    long countByStatus(ReportStatus status);
}
