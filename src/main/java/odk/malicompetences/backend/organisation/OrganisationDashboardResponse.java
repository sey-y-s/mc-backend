package odk.malicompetences.backend.organisation;

public record OrganisationDashboardResponse(
        long besoinsOuverts,
        long talentsSuivis,
        long misesEnRelation,
        long notificationsNonLues
) {
}
