package us.poliscore.legiscan.view;

public enum LegiscanSponsorType {
    SPONSOR("Sponsor (Generic / Unspecified)"),
    PRIMARY_SPONSOR("Primary Sponsor"),
    CO_SPONSOR("Co-Sponsor"),
    JOINT_SPONSOR("Joint Sponsor");

    private final String code;

    LegiscanSponsorType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal();
    }

    public static LegiscanSponsorType fromValue(int value) {
        if (value < 0 || value >= values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value];
    }

    public static LegiscanSponsorType fromCode(String code) {
        for (LegiscanSponsorType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

