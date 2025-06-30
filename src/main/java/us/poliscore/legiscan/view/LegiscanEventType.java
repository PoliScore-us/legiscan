package us.poliscore.legiscan.view;

public enum LegiscanEventType {
    HEARING("Hearing"),
    EXECUTIVE_SESSION("Executive Session"),
    MARKUP_SESSION("Markup Session");

    private final String code;

    LegiscanEventType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanEventType fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanEventType fromCode(String code) {
        for (LegiscanEventType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

