
package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanVoteView {
    
    @JsonProperty("roll_call_id")
    private Integer rollCallId;
    
    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonProperty("desc")
    private String description;
    
    @JsonProperty("yea")
    private Integer yea;
    
    @JsonProperty("nay")
    private Integer nay;
    
    @JsonProperty("nv")
    private Integer nv;
    
    @JsonProperty("absent")
    private Integer absent;
    
    @JsonProperty("total")
    private Integer total;
    
    @JsonProperty("passed")
    private Integer passed;
    
    @JsonProperty("chamber")
    private String chamber;
    
    @JsonProperty("chamber_id")
    private Integer chamberId;
}
