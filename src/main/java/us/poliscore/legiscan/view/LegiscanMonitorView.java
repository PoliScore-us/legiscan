package us.poliscore.legiscan.view;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanMonitorView {
    private int bill_id;
    private String state;
    private String number;
    private int stance;
    private String change_hash;
    private String url;
    
    @JsonProperty("status_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate statusDate;
    
    private int status;
    
    @JsonProperty("last_action_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate lastActionDate;
    
    private String last_action;
    private String title;
    private String description;
    
    public static class MonitorListDeserializer extends JsonDeserializer<List<LegiscanMonitorView>> {

        @Override
        public List<LegiscanMonitorView> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = p.getCodec();
            JsonNode node = codec.readTree(p);

            List<LegiscanMonitorView> result = new ArrayList<>();

            if (node.isArray()) {
                // If the monitorlist is a JSON array
                for (JsonNode item : node) {
                    result.add(codec.treeToValue(item, LegiscanMonitorView.class));
                }
            } else if (node.isObject()) {
                // If the monitorlist is a JSON object with numeric keys
                Iterator<JsonNode> elements = node.elements();
                while (elements.hasNext()) {
                    JsonNode item = elements.next();
                    result.add(codec.treeToValue(item, LegiscanMonitorView.class));
                }
            }

            return result;
        }
    }
}
