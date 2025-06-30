package us.poliscore.legiscan.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanSastView {

    @JsonProperty("type_id")
    private Integer typeId;

    @JsonProperty("type")
    private String typeCode;
    
    public LegiscanSastType getSastType() {
    	return LegiscanSastType.fromValue(typeId);
    }

    @JsonProperty("sast_bill_number")
    private String sastBillNumber;

    @JsonProperty("sast_bill_id")
    private Integer sastBillId;
}
