package odk.malicompetences.backend.besoin;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController @RequestMapping("/api/v1/besoins/{besoinId}/zones")
@PreAuthorize("hasAnyRole('ORGANISATION','ADMIN','SUPER_ADMIN')")
public class BesoinZoneController {
    private final BesoinZoneService service;
    public BesoinZoneController(BesoinZoneService service) { this.service=service; }
    @GetMapping public List<BesoinZoneResponse> lister(@PathVariable UUID besoinId) { return service.lister(besoinId); }
    @PutMapping public List<BesoinZoneResponse> remplacer(@PathVariable UUID besoinId, @RequestBody List<@Valid BesoinZoneRequest> zones) { return service.remplacer(besoinId,zones); }
}
