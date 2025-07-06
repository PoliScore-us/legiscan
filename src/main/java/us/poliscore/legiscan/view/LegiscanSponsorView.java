package us.poliscore.legiscan.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanSponsorView extends LegiscanPeopleView {

    @JsonProperty("sponsor_type_id")
    private Integer sponsorTypeId;

    @JsonProperty("sponsor_order")
    private Integer sponsorOrder;

    @JsonProperty("state_federal")
    private Integer stateFederal;

    @JsonProperty("committee_sponsor")
    private Integer committeeSponsor;

    @JsonProperty("committee_id")
    private Integer committeeId;
}
