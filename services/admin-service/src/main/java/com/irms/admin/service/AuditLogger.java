package com.irms.admin.service;

import com.irms.admin.domain.AuditLog;
import com.irms.admin.repository.AuditLogRepository;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {

    private final AuditLogRepository auditLogRepository;

    public AuditLogger(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String action, String entityName, String entityId, String username, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityName(entityName);
        log.setEntityId(entityId);
        log.setPerformedBy(username);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
