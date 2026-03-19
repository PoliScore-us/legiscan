
package us.poliscore.legiscan.view;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanPeopleView {
    
	public static String getCacheKey(Integer peopleId) {
		return "getperson/" + peopleId;
	}
	
    @JsonProperty("people_id")
    protected Integer peopleId;
    
    @JsonProperty("person_hash")
    protected String personHash;
    
    @JsonProperty("state_id")
    protected Integer stateId;
    
    @JsonIgnore
    public LegiscanState getState() {
    	return LegiscanState.fromId(stateId);
    }
    
    @JsonProperty("party_id")
    protected Integer partyId;
    
    @JsonProperty("party")
    protected String partyCode;
    
    @JsonIgnore
    public LegiscanParty getParty() {
    	return LegiscanParty.fromValue(partyId);
    }
    
    @JsonProperty("role_id")
    protected Integer roleId;
    
    @JsonProperty("role")
    protected String roleCode;
    
    @JsonIgnore
    public LegiscanRole getRole() {
    	return LegiscanRole.fromValue(roleId);
    }
    
    @JsonProperty("name")
    protected String name;
    
    @JsonProperty("first_name")
    protected String firstName;
    
    @JsonProperty("middle_name")
    protected String middleName;
    
    @JsonProperty("last_name")
    protected String lastName;
    
    @JsonProperty("suffix")
    protected String suffix;
    
    @JsonProperty("nickname")
    protected String nickname;
    
    @JsonProperty("district")
    protected String district;
    
    @JsonProperty("ftm_eid")
    protected Integer ftmEid;
    
    @JsonProperty("votesmart_id")
    protected Integer votesmartId;
    
    @JsonProperty("opensecrets_id")
    protected String opensecretsId;
    
    @JsonProperty("knowwho_pid")
    protected Integer knowwhoPid;
    
    @JsonProperty("ballotpedia")
    protected String ballotpedia;
    
    @JsonProperty("bioguide_id")
    protected String bioguideId;
    
    @JsonProperty("committee_sponsor")
    protected Integer committeeSponsor;
    
    @JsonProperty("committee_id")
    protected Integer committeeId;
    
    @JsonProperty("bio")
    @JsonDeserialize(using = BioObjectOrArrayDeserializer.class)
    protected LegiscanPeopleBioView bio;
    
    @Data
    public static class LegiscanPeopleBioView {
    	
    	protected HashMap<String, String> social;
    	
    	@JsonProperty("capitol_address")
    	protected HashMap<String, String> capitolAddress;
    	
    	protected LegiscanPeopleBioLinks links;
    	
    }
    
    @Data
    public static class LegiscanPeopleBioLinks {
    	
    	protected HashMap<String, String> official;
    	
    	protected HashMap<String, String> personal;
    	
    }
    
    @Data
    public static class LegiscanSessionPeopleView {
    	protected LegiscanSessionView session;
        protected List<LegiscanPeopleView> people;
    }
    
    /**
     * Accepts either:
     *  - "bio": { ... }
     *  - "bio": [ { ... }, { ... } ]
     *
     * Strategy:
     *  - If array: take the first non-null element.
     *  - If empty array / null / missing: return null.
     */
    public static class BioObjectOrArrayDeserializer extends JsonDeserializer<LegiscanPeopleBioView> {

        @Override
        public LegiscanPeopleBioView deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            ObjectCodec codec = p.getCodec();
            JsonNode node = codec.readTree(p);

            if (node == null || node.isNull() || node.isMissingNode()) {
                return null;
            }

            // Ensure we have an ObjectMapper for tree->value conversion
            ObjectMapper mapper = (codec instanceof ObjectMapper) ? (ObjectMapper) codec : new ObjectMapper();

            if (node.isObject()) {
                return mapper.treeToValue(node, LegiscanPeopleBioView.class);
            }

            if (node.isArray()) {
                if (node.size() == 0) return null;

                // Option A (simple): first non-null
//                for (JsonNode el : node) {
//                    if (el != null && !el.isNull() && el.isObject()) {
//                        return mapper.treeToValue(el, LegiscanPeopleBioView.class);
//                    }
//                }
//                return null;

                // Option B (merge): uncomment to merge all elements instead
                 List<LegiscanPeopleBioView> bios = new ArrayList<>();
                 for (JsonNode el : node) {
                     if (el != null && !el.isNull() && el.isObject()) {
                         bios.add(mapper.treeToValue(el, LegiscanPeopleBioView.class));
                     }
                 }
                 return mergeBios(bios);
            }

            // Unexpected type (string/number/etc): treat as null or throw
            return null;
        }

        @SuppressWarnings("unused")
        private static LegiscanPeopleBioView mergeBios(List<LegiscanPeopleBioView> bios) {
            if (bios == null || bios.isEmpty()) return null;

            LegiscanPeopleBioView out = new LegiscanPeopleBioView();
            out.social = new HashMap<>();
            out.capitolAddress = new HashMap<>();
            out.links = new LegiscanPeopleBioLinks();
            out.links.official = new HashMap<>();
            out.links.personal = new HashMap<>();

            for (LegiscanPeopleBioView b : bios) {
                if (b == null) continue;

                if (b.social != null) out.social.putAll(b.social);
                if (b.capitolAddress != null) out.capitolAddress.putAll(b.capitolAddress);

                if (b.links != null) {
                    if (b.links.official != null) out.links.official.putAll(b.links.official);
                    if (b.links.personal != null) out.links.personal.putAll(b.links.personal);
                }
            }

            // normalize empties back to null if you prefer
            if (out.social != null && out.social.isEmpty()) out.social = null;
            if (out.capitolAddress != null && out.capitolAddress.isEmpty()) out.capitolAddress = null;
            if (out.links != null) {
                if (out.links.official != null && out.links.official.isEmpty()) out.links.official = null;
                if (out.links.personal != null && out.links.personal.isEmpty()) out.links.personal = null;
                if (out.links.official == null && out.links.personal == null) out.links = null;
            }

            return out;
        }
    }
}
