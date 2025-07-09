
package us.poliscore.legiscan.view;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    protected Integer billId;
    
    @JsonProperty("change_hash")
    protected String changeHash;
    
    @JsonProperty("session_id")
    protected Integer sessionId;
    
    @JsonProperty("session")
    protected LegiscanSessionView session;
    
    @JsonProperty("url")
    protected String url;
    
    @JsonProperty("state_link")
    protected String stateLink;
    
    /**
     * DEPRECATED DO NOT USE
     */
    @JsonProperty("completed")
    protected Integer completed;
    
    @JsonProperty("status")
    protected Integer statusId;
    
    @JsonIgnore
    public LegiscanStatus getStatus() {
    	return LegiscanStatus.fromValue(statusId);
    }
    
    @JsonProperty("status_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    protected LocalDate statusDate;
    
    @JsonProperty("progress")
    protected List<LegiscanProgressView> progress;
    
    @JsonProperty("state")
    protected String stateCode;
    
    @JsonProperty("state_id")
    protected Integer stateId;
    
    @JsonIgnore
    public LegiscanState getState() {
    	return LegiscanState.fromId(stateId);
    }
    
    @JsonProperty("bill_number")
    protected String billNumber;
    
    @JsonProperty("bill_type")
    protected String billTypeCode;
    
    @JsonProperty("bill_type_id")
    protected Integer billTypeId;
    
    @JsonIgnore
    public LegiscanBillType getBillType() {
    	return LegiscanBillType.fromValue(billTypeId);
    }
    
    @JsonProperty("body")
    protected String body;
    
    @JsonProperty("body_id")
    protected Integer bodyId;
    
    @JsonProperty("current_body")
    protected String currentBody;
    
    @JsonProperty("current_body_id")
    protected Integer currentBodyId;
    
    @JsonProperty("title")
    protected String title;
    
    @JsonProperty("description")
    protected String description;
    
    @JsonProperty("pending_committee_id")
    protected Integer pendingCommitteeId;
    
    // Two different bills serialize this object completely differently.
    // This bill serializes it as an object:
    // https://api.legiscan.com/?key=123&op=getBill&id=1984092
    // And this bill serializes it as a list:
    // https://api.legiscan.com/?key=123&op=getBill&id=2014864
    @JsonProperty("committee")
    @JsonDeserialize(using = LegiscanCommitteeViewListDeserializer.class)
    protected List<LegiscanCommitteeView> committee;
    
    protected List<LegiscanReferralView> referrals;
    
    @JsonProperty("history")
    protected List<LegiscanHistoryView> history;
    
    @JsonProperty("sponsors")
    protected List<LegiscanSponsorView> sponsors;
    
    @JsonProperty("sasts")
    protected List<LegiscanSastView> sasts;
    
    @JsonProperty("subjects")
    protected List<LegiscanSubjectView> subjects;
    
    @JsonProperty("texts")
    protected List<LegiscanTextMetadataView> texts;
    
    @JsonProperty("votes")
    protected List<LegiscanVoteView> votes;
    
    @JsonProperty("amendments")
    protected List<LegiscanAmendmentView> amendments;
    
    @JsonProperty("supplements")
    protected List<LegiscanSupplementView> supplements;
    
    @JsonProperty("calendar")
    protected List<LegiscanCalendarView> calendar;
}
