package odk.malicompetences.backend.besoin;

import odk.malicompetences.backend.common.exception.ResourceNotFoundException;
import odk.malicompetences.backend.organisation.Organisation;
import odk.malicompetences.backend.organisation.OrganisationRepository;
import odk.malicompetences.backend.territoire.Commune;
import odk.malicompetences.backend.territoire.CommuneRepository;
import odk.malicompetences.backend.territoire.Territoire;
import odk.malicompetences.backend.territoire.TerritoireRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service @Transactional
public class BesoinZoneService {
    private final BesoinRepository besoinRepository; private final BesoinZoneRepository zoneRepository;
    private final OrganisationRepository organisationRepository; private final CommuneRepository communeRepository; private final TerritoireRepository territoireRepository;
    public BesoinZoneService(BesoinRepository b, BesoinZoneRepository z, OrganisationRepository o, CommuneRepository c, TerritoireRepository t) {
        besoinRepository=b; zoneRepository=z; organisationRepository=o; communeRepository=c; territoireRepository=t;
    }
    @Transactional(readOnly=true)
    public List<BesoinZoneResponse> lister(UUID besoinId) {
        Besoin besoin=besoinRepository.findById(besoinId).orElseThrow(()->new ResourceNotFoundException("Besoin introuvable."));
        verifierAcces(besoin);
        return zoneRepository.findByBesoin_IdOrderByIdAsc(besoinId).stream().map(this::response).toList();
    }
    public List<BesoinZoneResponse> remplacer(UUID besoinId, List<BesoinZoneRequest> zones) {
        Besoin besoin=besoinRepository.findById(besoinId).orElseThrow(()->new ResourceNotFoundException("Besoin introuvable."));
        verifierAcces(besoin);
        if (besoin.getStatut()!=StatutBesoin.BROUILLON) throw new IllegalStateException("Les zones ne peuvent être modifiées que sur un besoin brouillon.");
        List<BesoinZoneRequest> liste=zones==null?List.of():zones;
        if (liste.stream().anyMatch(z -> z==null || !z.valide())) throw new IllegalArgumentException("Chaque zone doit contenir une commune OU un territoire, jamais les deux.");
        zoneRepository.deleteByBesoin_Id(besoinId);
        for (BesoinZoneRequest r: liste) {
            BesoinZone z=new BesoinZone(); z.setBesoin(besoin);
            if (r.communeId()!=null) {
                Commune c=communeRepository.findById(r.communeId()).orElseThrow(()->new ResourceNotFoundException("Commune introuvable."));
                if (!c.isActif()) throw new IllegalArgumentException("La commune sélectionnée est inactive."); z.setCommune(c);
            } else {
                Territoire t=territoireRepository.findById(r.territoireId()).orElseThrow(()->new ResourceNotFoundException("Territoire introuvable."));
                if (!t.isActif()) throw new IllegalArgumentException("Le territoire sélectionné est inactif."); z.setTerritoire(t);
            }
            zoneRepository.save(z);
        }
        return listerInterne(besoinId);
    }
    private List<BesoinZoneResponse> listerInterne(UUID id) { return zoneRepository.findByBesoin_IdOrderByIdAsc(id).stream().map(this::response).toList(); }
    private BesoinZoneResponse response(BesoinZone z) { return new BesoinZoneResponse(z.getId(), z.getCommune()==null?null:z.getCommune().getId(), z.getCommune()==null?null:z.getCommune().getNom(), z.getTerritoire()==null?null:z.getTerritoire().getId(), z.getTerritoire()==null?null:z.getTerritoire().getNom()); }
    private void verifierAcces(Besoin besoin) {
        Authentication a=SecurityContextHolder.getContext().getAuthentication();
        if (a!=null && a.getAuthorities().stream().anyMatch(x -> x.getAuthority().equals("ROLE_ADMIN")||x.getAuthority().equals("ROLE_SUPER_ADMIN"))) return;
        UUID userId; try { userId=UUID.fromString(a==null?"":a.getName()); } catch (IllegalArgumentException e) { throw new AccessDeniedException("Authentification requise."); }
        Organisation org=organisationRepository.findByUtilisateur_Id(userId).orElseThrow(()->new AccessDeniedException("Profil organisation introuvable."));
        if (!org.getId().equals(besoin.getOrganisation().getId())) throw new AccessDeniedException("Ce besoin ne vous appartient pas.");
    }
}
