package us.poliscore.legiscan.view;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * The 'chamber' string on the bill's history object is never fully documented anywhere so this list is a work in progress.
 */
public enum LegiscanChamber {
	SENATE("S"),
	HOUSE("H"),
	UNICAM("L"),
	NOT_APPLICABLE("");
	
	private String code;
	
	LegiscanChamber(String code) {
		this.code = code;
	}
	
	@JsonValue
	public String getCode() {
		return code;
	}

	@JsonCreator
    public static LegiscanChamber fromCode(String code) {
		if (code == null || code.isBlank()) return NOT_APPLICABLE;
		
        for (LegiscanChamber chamber : values()) {
            if (chamber.code.equalsIgnoreCase(code)) {
                return chamber;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
