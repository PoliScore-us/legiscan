
package us.poliscore.legiscan.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanSessionView {
    
    @JsonProperty("session_id")
    private Integer sessionId;
    
    @JsonProperty("state_id")
    private Integer stateId;
    @JsonIgnore public LegiscanState getState() { return LegiscanState.fromId(stateId); }
    
    @JsonProperty("year_start")
    private Integer yearStart;
    
    @JsonProperty("year_end")
    private Integer yearEnd;
    
    @JsonProperty("prefile")
    private int prefileId;
    @JsonIgnore public boolean isPrefile() { return prefileId == 1; }
    
    @JsonProperty("sine_die")
    private int sineDieId;
    @JsonIgnore public boolean isSineDie() { return sineDieId == 1; }
    
    @JsonProperty("prior")
    private int priorId;
    @JsonIgnore public boolean isPrior() { return priorId == 1; }
    
    @JsonProperty("special")
    private int specialId;
    @JsonIgnore public boolean isSpecial() { return specialId == 1; }
    
    @JsonProperty("session_tag")
    private String sessionTag;
    
    @JsonProperty("session_title")
    private String sessionTitle;
    
    @JsonProperty("session_name")
    private String sessionName;
}
