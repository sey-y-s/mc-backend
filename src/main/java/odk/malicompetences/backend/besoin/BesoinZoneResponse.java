package odk.malicompetences.backend.besoin;

import java.util.UUID;
public record BesoinZoneResponse(UUID id, UUID communeId, String communeNom, UUID territoireId, String territoireNom) {}
