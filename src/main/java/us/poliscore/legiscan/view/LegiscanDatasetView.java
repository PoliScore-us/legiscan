package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanDatasetView {

    @JsonProperty("state_id")
    private int stateId;

    @JsonProperty("session_id")
    private int sessionId;

    private int special;

    @JsonProperty("year_start")
    private int yearStart;

    @JsonProperty("year_end")
    private int yearEnd;

    @JsonProperty("session_name")
    private String sessionName;

    @JsonProperty("session_title")
    private String sessionTitle;

    @JsonProperty("dataset_hash")
    private String datasetHash;
    
    @JsonProperty("dataset_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate datasetDate;

    @JsonProperty("dataset_size")
    private int datasetSize;

    @JsonProperty("access_key")
    private String accessKey;
    
    // Exists only on the 'getDataset' response
    private String mime;
    
    // Exists only on the 'getDataset' response
    private String zip;
}
