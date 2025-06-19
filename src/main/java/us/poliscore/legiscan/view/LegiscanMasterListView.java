package us.poliscore.legiscan.view;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanMasterListView {

    private Map<String, BillSummary> bills = new HashMap<>();
    
    private LegiscanSessionView session;

    // Legiscan for some reason is putting a 'session' object inside the 'bills' map that it returns. So we need a custom parser.
    @JsonProperty("bills")
    public void setRawBills(Map<String, Object> raw) {
        bills = new HashMap<>();
        ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            if ("session".equals(entry.getKey())) {
                session = mapper.convertValue(entry.getValue(), LegiscanSessionView.class);
            } else {
                BillSummary summary = mapper.convertValue(entry.getValue(), BillSummary.class);
                bills.put(entry.getKey(), summary);
            }
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BillSummary {
        @JsonProperty("bill_id")
        private int billId;

        private String number;

        @JsonProperty("change_hash")
        private String changeHash;

        private String url;

        @JsonProperty("status_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate statusDate;

        private String status;

        @JsonProperty("last_action_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate lastActionDate;

        @JsonProperty("last_action")
        private String lastAction;

        private String title;
        private String description;
    }
}
