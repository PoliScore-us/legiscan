package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanHistoryView {

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("action")
    private String action;

    @JsonProperty("chamber")
    private LegiscanChamber chamber;

    @JsonProperty("chamber_id")
    private Integer chamberId;

    @JsonProperty("importance")
    private Integer importance;
}
