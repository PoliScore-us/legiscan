package us.poliscore.legiscan.view;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanCalendarView {

    @JsonProperty("type_id")
    private Integer typeId;

    @JsonProperty("type")
    private String type;

    @JsonProperty("date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    @JsonProperty("time")
    private String time;

    @JsonProperty("location")
    private String location;

    @JsonProperty("description")
    private String description;
}
