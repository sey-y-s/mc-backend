package odk.malicompetences.backend.talent;

import odk.malicompetences.backend.besoin.Besoin;
import odk.malicompetences.backend.citoyen.Citoyen;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class SuiviBesoinTalentMapper {

    /**
     * Crée le suivi. Le statut initial et le score sont contrôlés
     * par le service métier, pas par les données envoyées par le client.
     */
    public SuiviBesoinTalent toEntity(
            SuiviBesoinTalentRequest request,
            Besoin besoin,
            Citoyen citoyen
    ) {
        if (request == null || besoin == null || citoyen == null) {
            throw new IllegalArgumentException(
                    "Le suivi, le besoin et le profil talent sont obligatoires."
            );
        }

        SuiviBesoinTalent suivi = new SuiviBesoinTalent();
        suivi.setBesoin(besoin);
        suivi.setCitoyen(citoyen);

        // Le suivi commence toujours à l'étude.
        suivi.setStatut(StatutSuiviBesoinTalent.A_L_ETUDE);

        // Le score et les détails de correspondance sont calculés par le backend.
        suivi.setScoreCorrespondance(null);
        suivi.setDetailsCorrespondance(null);

        // Note privée à l'organisation ; son accès est contrôlé à la lecture.
        suivi.setNoteInterne(request.noteInterne());

        return suivi;
    }

    /**
     * Seules les modifications autorisées par le service métier
     * doivent arriver ici.
     */
    public void updateEntity(
            SuiviBesoinTalentRequest request,
            SuiviBesoinTalent suivi
    ) {
        if (request == null || suivi == null) {
            return;
        }

        if (request.noteInterne() != null) {
            suivi.setNoteInterne(request.noteInterne());
        }

        // Le statut, le score et le matching ne sont pas modifiés ici.
        // Les dates sont gérées par la base de données.
    }

    public SuiviBesoinTalentResponse toResponse(SuiviBesoinTalent suivi) {
        return toResponse(suivi, false);
    }

    /**
     * inclureNoteInterne doit être vrai uniquement pour l'organisation
     * propriétaire du besoin ou un administrateur autorisé.
     */
    public SuiviBesoinTalentResponse toResponse(
            SuiviBesoinTalent suivi,
            boolean inclureNoteInterne
    ) {
        if (suivi == null) {
            return null;
        }

        Besoin besoin = suivi.getBesoin();
        Citoyen citoyen = suivi.getCitoyen();

        UUID organisationId = besoin != null && besoin.getOrganisation() != null
                ? besoin.getOrganisation().getId()
                : null;

        UUID besoinId = besoin != null ? besoin.getId() : null;
        String besoinTitre = besoin != null ? besoin.getTitre() : null;

        // Ne jamais exposer citoyen.getId(), qui est l'identifiant interne.
        UUID identifiantPublic = citoyen != null
                ? citoyen.getIdentifiantPublic()
                : null;

        String nomProfil = identifiantPublic != null
                ? "Profil-" + identifiantPublic.toString().substring(0, 8)
                : null;

        return new SuiviBesoinTalentResponse(
                suivi.getId(),
                organisationId,
                besoinId,
                besoinTitre,
                identifiantPublic,
                nomProfil,
                suivi.getStatut(),
                suivi.getScoreCorrespondance(),
                suivi.getDetailsCorrespondance(),
                inclureNoteInterne ? suivi.getNoteInterne() : null,
                suivi.getDateCreation(),
                suivi.getDateMiseAJour()
        );
    }
}