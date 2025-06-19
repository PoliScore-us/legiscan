package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanAmendmentView {
	
	@JsonProperty("amendment_id")
    private int amendmentId;

    private String chamber;

    @JsonProperty("chamber_id")
    private int chamberId;

    @JsonProperty("bill_id")
    private int billId;

    private int adopted;

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    private String title;

    private String description;

    private String mime;

    @JsonProperty("mime_id")
    private int mimeId;

    @JsonProperty("amendment_size")
    private int amendmentSize;

    @JsonProperty("amendment_hash")
    private String amendmentHash;

    private String doc;
}
