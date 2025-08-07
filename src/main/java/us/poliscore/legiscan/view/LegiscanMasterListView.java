package us.poliscore.legiscan.view;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import lombok.Data;
import us.poliscore.legiscan.view.LegiscanMasterListView.LegiscanMasterListViewDeserializer;
import us.poliscore.legiscan.view.LegiscanMasterListView.LegiscanMasterListViewSerializer;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(using = LegiscanMasterListViewSerializer.class)
@JsonDeserialize(using = LegiscanMasterListViewDeserializer.class)
public class LegiscanMasterListView {

    private Map<String, BillSummary> bills = new HashMap<>();
    
    private LegiscanSessionView session;

    // Legiscan for some reason is putting a 'session' object inside the 'bills' map that it returns. So we need a custom parser.
    public static class LegiscanMasterListViewDeserializer extends JsonDeserializer<LegiscanMasterListView> {
        @Override
        public LegiscanMasterListView deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            LegiscanMasterListView result = new LegiscanMasterListView();
            Map<String, LegiscanMasterListView.BillSummary> bills = new HashMap<>();
            ObjectMapper mapper = (ObjectMapper) p.getCodec();

            if (p.currentToken() == null) p.nextToken(); // advance to START_OBJECT
            if (p.currentToken() != JsonToken.START_OBJECT)
                throw new IOException("Expected START_OBJECT");

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.getCurrentName();
                p.nextToken(); // move to value
                if ("session".equals(fieldName)) {
                    LegiscanSessionView session = mapper.readValue(p, LegiscanSessionView.class);
                    result.setSession(session);
                } else {
                    LegiscanMasterListView.BillSummary bill = mapper.readValue(p, LegiscanMasterListView.BillSummary.class);
                    bills.put(fieldName, bill);
                }
            }
            result.setBills(bills);
            return result;
        }
    }
    
    public static class LegiscanMasterListViewSerializer extends StdSerializer<LegiscanMasterListView> {

        public LegiscanMasterListViewSerializer() {
            super(LegiscanMasterListView.class);
        }

        @Override
        public void serialize(LegiscanMasterListView value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            // Serialize all bill entries
            for (Map.Entry<String, LegiscanMasterListView.BillSummary> entry : value.getBills().entrySet()) {
                gen.writeObjectField(entry.getKey(), entry.getValue());
            }
            // Add the session as an additional property
            if (value.getSession() != null) {
                gen.writeObjectField("session", value.getSession());
            }
            gen.writeEndObject();
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
