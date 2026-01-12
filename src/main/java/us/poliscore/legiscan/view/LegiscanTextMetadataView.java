
package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanTextMetadataView {
    
    @JsonProperty("doc_id")
    private Integer docId;
    
    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonDeserialize(using = LegiscanLocalDateDeserializer.class)
    private LocalDate date;
    
    @JsonProperty("type")
    private String typeCode;
    
    @JsonProperty("type_id")
    private Integer typeId;
    
    @JsonIgnore
    public LegiscanTextType getType() {
    	return LegiscanTextType.fromValue(typeId);
    }
    
    @JsonProperty("mime")
    private String mimeCode;
    
    @JsonProperty("mime_id")
    private Integer mimeId;
    
    @JsonIgnore
    public LegiscanMimeType getMime() {
    	return LegiscanMimeType.fromValue(mimeId);
    }
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("state_link")
    private String stateLink;
    
    @JsonProperty("text_size")
    private Integer textSize;
    
    private String text_hash;
}
