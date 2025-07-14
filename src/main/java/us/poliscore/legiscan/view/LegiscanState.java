package us.poliscore.legiscan.view;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.NoArgsConstructor;
import us.poliscore.legiscan.view.LegiscanState.LegiscanStateDeserializer;
import us.poliscore.legiscan.view.LegiscanState.LegiscanStateSerializer;


@JsonSerialize(using = LegiscanStateSerializer.class)
@JsonDeserialize(using = LegiscanStateDeserializer.class)
public enum LegiscanState {
    ALABAMA("AL"),
    ALASKA("AK"),
    ARIZONA("AZ"),
    ARKANSAS("AR"),
    CALIFORNIA("CA"),
    COLORADO("CO"),
    CONNECTICUT("CT"),
    DELAWARE("DE"),
    FLORIDA("FL"),
    GEORGIA("GA"),
    HAWAII("HI"),
    IDAHO("ID"),
    ILLINOIS("IL"),
    INDIANA("IN"),
    IOWA("IA"),
    KANSAS("KS"),
    KENTUCKY("KY"),
    LOUISIANA("LA"),
    MAINE("ME"),
    MARYLAND("MD"),
    MASSACHUSETTS("MA"),
    MICHIGAN("MI"),
    MINNESOTA("MN"),
    MISSISSIPPI("MS"),
    MISSOURI("MO"),
    MONTANA("MT"),
    NEBRASKA("NE"),
    NEVADA("NV"),
    NEW_HAMPSHIRE("NH"),
    NEW_JERSEY("NJ"),
    NEW_MEXICO("NM"),
    NEW_YORK("NY"),
    NORTH_CAROLINA("NC"),
    NORTH_DAKOTA("ND"),
    OHIO("OH"),
    OKLAHOMA("OK"),
    OREGON("OR"),
    PENNSYLVANIA("PA"),
    RHODE_ISLAND("RI"),
    SOUTH_CAROLINA("SC"),
    SOUTH_DAKOTA("SD"),
    TENNESSEE("TN"),
    TEXAS("TX"),
    UTAH("UT"),
    VERMONT("VT"),
    VIRGINIA("VA"),
    WASHINGTON("WA"),
    WASHINGTON_DC("DC"),
    WEST_VIRGINIA("WV"),
    WISCONSIN("WI"),
    WYOMING("WY"),
	
    // Congress (not a state but is included in Legiscan "state" codes
    CONGRESS("US"),
	
	// U.S. territories (also not states, but are included for completeness
    AMERICAN_SAMOA("AS"),
    GUAM("GU"),
    NORTHERN_MARIANA_ISLANDS("MP"),
    PUERTO_RICO("PR"),
    VIRGIN_ISLANDS("VI");

    private final String abbreviation;

    LegiscanState(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int getId() {
        return ordinal() + 1;
    }
    
    @Override
    @JsonValue
    public String toString() {
    	return getAbbreviation();
    }

    public static LegiscanState fromId(int id) {
        if (id <= 0 || id > values().length) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return values()[id - 1];
    }

    @JsonCreator
    public static LegiscanState fromAbbreviation(String abbr) {
        for (LegiscanState state : values()) {
            if (state.abbreviation.equalsIgnoreCase(abbr)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown abbreviation: " + abbr);
    }
    
    @NoArgsConstructor
    public static class LegiscanStateDeserializer extends JsonDeserializer<LegiscanState> {
        @Override
        public LegiscanState deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String abbr = p.getText();
            return LegiscanState.fromAbbreviation(abbr);
        }
    }
    
    @NoArgsConstructor
    public static class LegiscanStateSerializer extends JsonSerializer<LegiscanState> {
        @Override
        public void serialize(LegiscanState value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeString(value.toString());
        }
    }
}
