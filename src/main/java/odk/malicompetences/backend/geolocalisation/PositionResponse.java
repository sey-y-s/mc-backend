package odk.malicompetences.backend.geolocalisation;

import java.time.LocalDateTime;

public record PositionResponse(Double latitude, Double longitude, LocalDateTime datePosition, boolean definie) {

    public static PositionResponse of(Double latitude, Double longitude, LocalDateTime date) {
        return new PositionResponse(latitude, longitude, date, latitude != null && longitude != null);
    }
}
