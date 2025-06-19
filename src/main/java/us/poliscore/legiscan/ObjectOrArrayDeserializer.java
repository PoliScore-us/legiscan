package us.poliscore.legiscan;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import us.poliscore.legiscan.view.LegiscanCommitteeView;

public class ObjectOrArrayDeserializer<T> extends JsonDeserializer<List<T>> {

    private final Class<T> clazz;

    public ObjectOrArrayDeserializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public List<T> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        ObjectCodec codec = p.getCodec();
        JsonNode node = codec.readTree(p);

        List<T> result = new ArrayList<>();

        if (node.isArray()) {
            for (JsonNode item : node) {
                result.add(codec.treeToValue(item, clazz));
            }
        } else if (node.isObject()) {
            result.add(codec.treeToValue(node, clazz));
        }

        return result;
    }
    
    public static class LegiscanCommitteeViewListDeserializer extends ObjectOrArrayDeserializer<LegiscanCommitteeView> {
        public LegiscanCommitteeViewListDeserializer() {
            super(LegiscanCommitteeView.class);
        }
    }
}
