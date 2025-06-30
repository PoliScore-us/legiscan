package us.poliscore.legiscan.view;

public enum LegiscanTextType {
    INTRODUCED("Introduced"),
    COMMITTEE_SUBSTITUTE("Committee Substitute"),
    AMENDED("Amended"),
    ENGROSSED("Engrossed"),
    ENROLLED("Enrolled"),
    CHAPTERED("Chaptered"),
    FISCAL_NOTE("Fiscal Note"),
    ANALYSIS("Analysis"),
    DRAFT("Draft"),
    CONFERENCE_SUBSTITUTE("Conference Substitute"),
    PREFILED("Prefiled"),
    VETO_MESSAGE("Veto Message"),
    VETO_RESPONSE("Veto Response"),
    SUBSTITUTE("Substitute");

    private final String code;

    LegiscanTextType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanTextType fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanTextType fromCode(String code) {
        for (LegiscanTextType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

