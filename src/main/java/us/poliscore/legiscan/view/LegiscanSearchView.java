package us.poliscore.legiscan.view;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanSearchView {

    private Summary summary;

    // More api weirdness - is it an array or a map?
    @JsonDeserialize(using = SearchResultsDeserializer.class)
    private List<Result> results = new ArrayList<>();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Summary {
        private String page;
        private String range;
        private String relevancy;
        private int count;

        @JsonProperty("page_current")
        private int pageCurrent;

        @JsonProperty("page_total")
        private int pageTotal;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private int relevance;
        private String state;

        @JsonProperty("bill_number")
        private String billNumber;

        @JsonProperty("bill_id")
        private int billId;

        @JsonProperty("change_hash")
        private String changeHash;

        private String url;

        @JsonProperty("text_url")
        private String textUrl;

        @JsonProperty("research_url")
        private String researchUrl;

        @JsonProperty("last_action_date")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate lastActionDate;

        @JsonProperty("last_action")
        private String lastAction;

        private String title;

        // Constructor that maps from a generic map (safely, with checks)
        public Result(Map<?, ?> map) {
            this.relevance = getInt(map.get("relevance"));
            this.state = getString(map.get("state"));
            this.billNumber = getString(map.get("bill_number"));
            this.billId = getInt(map.get("bill_id"));
            this.changeHash = getString(map.get("change_hash"));
            this.url = getString(map.get("url"));
            this.textUrl = getString(map.get("text_url"));
            this.researchUrl = getString(map.get("research_url"));
            this.lastActionDate = getDate(map.get("last_action_date"));
            this.lastAction = getString(map.get("last_action"));
            this.title = getString(map.get("title"));
        }
        
        private LocalDate getDate(Object val) {
            if (val instanceof String s) {
                try {
                    return LocalDate.parse(s); // defaults to ISO-8601: yyyy-MM-dd
                } catch (Exception ignored) {}
            }
            return null;
        }

        private String getString(Object val) {
            return val != null ? val.toString() : null;
        }

        private int getInt(Object val) {
            if (val instanceof Number n) {
                return n.intValue();
            } else if (val instanceof String s) {
                try {
                    return Integer.parseInt(s);
                } catch (NumberFormatException ignored) {}
            }
            return 0;
        }
    }
    
    public static class SearchResultsDeserializer extends JsonDeserializer<List<LegiscanSearchView.Result>> {

        @Override
        public List<LegiscanSearchView.Result> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = p.getCodec();
            JsonNode root = codec.readTree(p);
            List<LegiscanSearchView.Result> results = new ArrayList<>();

            JsonNode resultsNode = root.get("results");

            if (resultsNode == null || resultsNode.isNull()) return results;

            if (resultsNode.isArray()) {
                for (JsonNode item : resultsNode) {
                    results.add(codec.treeToValue(item, LegiscanSearchView.Result.class));
                }
            } else if (resultsNode.isObject()) {
                Iterator<JsonNode> elements = resultsNode.elements();
                while (elements.hasNext()) {
                    JsonNode item = elements.next();
                    results.add(codec.treeToValue(item, LegiscanSearchView.Result.class));
                }
            }

            return results;
        }
    }

}
