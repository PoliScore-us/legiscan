
package us.poliscore.legiscan.view;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import us.poliscore.legiscan.ObjectOrArrayDeserializer.LegiscanCommitteeViewListDeserializer;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LegiscanBillView {
    
	public static String getCacheKey(Integer billId) {
		return "getbill/" + billId;
	}
	
    @JsonProperty("bill_id")
    private Integer billId;
    
    @JsonProperty("change_hash")
    private String changeHash;
    
    @JsonProperty("session_id")
    private Integer sessionId;
    
    @JsonProperty("session")
    private LegiscanSessionView session;
    
    @JsonProperty("url")
    private String url;
    
    @JsonProperty("state_link")
    private String stateLink;
    
    @JsonProperty("completed")
    private Integer completed;
    
    @JsonProperty("status")
    private Integer status;
    
    @JsonProperty("status_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate statusDate;
    
    @JsonProperty("progress")
    private List<LegiscanProgressView> progress;
    
    @JsonProperty("state")
    private String state;
    
    @JsonProperty("state_id")
    private Integer stateId;
    
    @JsonProperty("bill_number")
    private String billNumber;
    
    @JsonProperty("bill_type")
    private String billType;
    
    @JsonProperty("bill_type_id")
    private Integer billTypeId;
    
    @JsonProperty("body")
    private String body;
    
    @JsonProperty("body_id")
    private Integer bodyId;
    
    @JsonProperty("current_body")
    private String currentBody;
    
    @JsonProperty("current_body_id")
    private Integer currentBodyId;
    
    @JsonProperty("title")
    private String title;
    
    @JsonProperty("description")
    private String description;
    
    @JsonProperty("pending_committee_id")
    private Integer pendingCommitteeId;
    
    // Two different bills serialize this object completely differently.
    // This bill serializes it as an object:
    // https://api.legiscan.com/?key=123&op=getBill&id=1984092
    // And this bill serializes it as a list:
    // https://api.legiscan.com/?key=123&op=getBill&id=2014864
    @JsonProperty("committee")
    @JsonDeserialize(using = LegiscanCommitteeViewListDeserializer.class)
    private List<LegiscanCommitteeView> committee;
    
    private List<LegiscanReferralView> referrals;
    
    @JsonProperty("history")
    private List<LegiscanHistoryView> history;
    
    @JsonProperty("sponsors")
    private List<LegiscanSponsorView> sponsors;
    
    @JsonProperty("sasts")
    private List<LegiscanSastView> sasts;
    
    @JsonProperty("subjects")
    private List<LegiscanSubjectView> subjects;
    
    @JsonProperty("texts")
    private List<LegiscanTextMetadataView> texts;
    
    @JsonProperty("votes")
    private List<LegiscanVoteView> votes;
    
    @JsonProperty("amendments")
    private List<LegiscanAmendmentView> amendments;
    
    @JsonProperty("supplements")
    private List<LegiscanSupplementView> supplements;
    
    @JsonProperty("calendar")
    private List<LegiscanCalendarView> calendar;
}
