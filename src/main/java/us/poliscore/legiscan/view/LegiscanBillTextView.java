package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanBillTextView {

	@JsonProperty("doc_id")
    protected int docId;

    @JsonProperty("bill_id")
    protected int billId;

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    protected LocalDate date;
    
    @JsonProperty("type")
    protected String typeCode;

    @JsonProperty("type_id")
    protected int typeId;
    
    @JsonIgnore
    public LegiscanTextType getType() {
    	return LegiscanTextType.fromValue(typeId);
    }

    @JsonProperty("mime")
    protected String mimeCode;

    @JsonProperty("mime_id")
    protected int mimeId;
    
    @JsonIgnore
    public LegiscanMimeType getMime() {
    	return LegiscanMimeType.fromValue(mimeId);
    }

    @JsonProperty("text_size")
    protected int textSize;

    @JsonProperty("text_hash")
    protected String textHash;

    /**
     * A base64 encoded string. 
     */
    protected String doc;
}

