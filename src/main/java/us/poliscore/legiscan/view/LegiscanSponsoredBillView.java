package us.poliscore.legiscan.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanSponsoredBillView {

    @JsonProperty("session_id")
    private Integer sessionId;

    @JsonProperty("bill_id")
    private Integer billId;

    @JsonProperty("number")
    private String number;
}
