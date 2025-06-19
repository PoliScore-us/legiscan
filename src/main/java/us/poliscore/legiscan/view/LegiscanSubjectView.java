package us.poliscore.legiscan.view;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanSubjectView {

    @JsonProperty("subject_id")
    private Integer subjectId;

    @JsonProperty("subject_name")
    private String subjectName;
}
