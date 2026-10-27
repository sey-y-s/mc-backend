package odk.malicompetences.backend.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;

    public AuditLogService(AuditLogRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public void enregistrer(String action, String targetType, UUID targetId, String resultCode) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        AuditLog log = new AuditLog();
        log.setActorId(auth == null ? null : auth.getName());
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId == null ? null : targetId.toString());
        log.setResultCode(resultCode);
        repository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> lister(int page, int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        return repository.findAll(PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}
