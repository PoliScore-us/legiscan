package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanSupplementView {

    @JsonProperty("supplement_id")
    private int supplementId;

    @JsonProperty("bill_id")
    private int billId;

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("type_id")
    private int typeId;

    @JsonProperty("type")
    private String typeCode;

    private String title;

    private String description;

    @JsonProperty("mime")
    private String mimeCode;

    @JsonProperty("mime_id")
    private int mimeId;
    
    @JsonIgnore
    public LegiscanMimeType getMime() {
    	return LegiscanMimeType.fromValue(mimeId);
    }

    @JsonProperty("supplement_size")
    private int supplementSize;

    @JsonProperty("supplement_hash")
    private String supplementHash;

    private String doc;
}
