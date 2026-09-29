package com.military.assetmanagement.service;

import com.military.assetmanagement.dto.audit.AuditLogResponse;
import com.military.assetmanagement.dto.common.PageResponse;
import com.military.assetmanagement.entity.User;
import com.military.assetmanagement.repository.AuditLogRepository;
import com.military.assetmanagement.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> getAll(Pageable pageable) {
        Page<AuditLogResponse> page;

        if (securityUtils.isAdmin()) {
            log.debug("ADMIN fetching paginated audit logs, page={}, size={}",
                    pageable.getPageNumber(), pageable.getPageSize());
            page = auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                    .map(AuditLogResponse::from);
        } else {
            User currentUser = securityUtils.getCurrentUser();
            Long baseId = currentUser.getBase() != null ? currentUser.getBase().getId() : null;
            log.debug("BASE_COMMANDER fetching audit logs for baseId={}", baseId);
            page = auditLogRepository.findByBaseIdOrderByCreatedAtDesc(baseId, pageable)
                    .map(AuditLogResponse::from);
        }

        return PageResponse.of(page);
    }

    public List<AuditLogResponse> getRecent() {
        if (securityUtils.isAdmin()) {
            log.debug("ADMIN fetching recent audit logs");
            return auditLogRepository.findTop20ByOrderByCreatedAtDesc()
                    .stream()
                    .map(AuditLogResponse::from)
                    .toList();
        }

        User currentUser = securityUtils.getCurrentUser();
        Long baseId = currentUser.getBase() != null ? currentUser.getBase().getId() : null;
        log.debug("BASE_COMMANDER fetching recent audit logs for baseId={}", baseId);
        Pageable top20 = PageRequest.of(0, 20);
        return auditLogRepository.findByBaseIdOrderByCreatedAtDesc(baseId, top20)
                .stream()
                .map(AuditLogResponse::from)
                .toList();
    }
}
