package odk.malicompetences.backend.audit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;

import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository repository;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<HttpServletRequest> requestProvider;

    public AuditLogService(AuditLogRepository repository, ObjectMapper objectMapper,
                           ObjectProvider<HttpServletRequest> requestProvider) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.requestProvider = requestProvider;
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> lister(int page, int size) {
        int pageSecurisee = Math.max(0, page);
        int tailleSecurisee = Math.clamp(size, 1, 100);
        return repository.findAll(PageRequest.of(pageSecurisee, tailleSecurisee, Sort.by(Sort.Direction.DESC, "creeLe")));
    }

    @Transactional
    public void enregistrer(String action, String typeRessource, UUID ressourceId, String resultat) {
        enregistrer(action, typeRessource, ressourceId, resultat, null);
    }

    @Transactional
    public void enregistrer(String action, String typeRessource, UUID ressourceId, String resultat, Object details) {
        AuditLog log = new AuditLog();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            try { log.setActeurId(UUID.fromString(auth.getName())); } catch (IllegalArgumentException ignored) { }
        }
        log.setAction(action);
        log.setTypeRessource(typeRessource);
        log.setRessourceId(ressourceId);
        log.setResultat(resultat);
        if (details != null) log.setDetailsJson(objectMapper.valueToTree(details));
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request != null) {
            String ip = request.getRemoteAddr();
            if (ip != null && ip.length() <= 45) log.setAdresseIp(ip);
        }
        repository.save(log);
    }
}
