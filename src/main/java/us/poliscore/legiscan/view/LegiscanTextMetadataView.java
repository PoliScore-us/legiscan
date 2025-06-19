
package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanTextMetadataView {
    
    @JsonProperty("doc_id")
    private Integer docId;
    
    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    
    @JsonProperty("type")
    private String type;
    
    @JsonProperty("type_id")
    private Integer typeId;
    
    @JsonProperty("mime")
    private String mime;
    
    @JsonProperty("mime_id")
    private Integer mimeId;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("state_link")
    private String stateLink;
    
    @JsonProperty("text_size")
    private Integer textSize;
    
    private String text_hash;
}
