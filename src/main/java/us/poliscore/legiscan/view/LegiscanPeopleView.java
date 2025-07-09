
package us.poliscore.legiscan.view;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    
    @Data
    public static class LegiscanSessionPeopleView {
    	protected LegiscanSessionView session;
        protected List<LegiscanPeopleView> people;
    }
}
