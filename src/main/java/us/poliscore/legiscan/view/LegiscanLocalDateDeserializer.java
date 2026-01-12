package us.poliscore.legiscan.view;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class LegiscanLocalDateDeserializer extends JsonDeserializer<LocalDate> {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String raw = p.getValueAsString();
        if (raw == null) return null;

        String s = raw.trim();
        if (s.isEmpty() || s.equals("0000-00-00")) {
            return null;
        }

        try {
            return LocalDate.parse(s, FMT);
        } catch (DateTimeParseException e) {
            // If you’d rather fail hard, rethrow here instead of returning null.
            return null;
        }
    }
}
