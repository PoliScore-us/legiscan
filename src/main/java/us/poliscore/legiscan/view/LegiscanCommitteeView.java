package us.poliscore.legiscan.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanCommitteeView {

    @JsonProperty("committee_id")
    private Integer committeeId;

    @JsonProperty("chamber")
    private String chamber;

    @JsonProperty("chamber_id")
    private Integer chamberId;

    @JsonProperty("name")
    private String name;
}
