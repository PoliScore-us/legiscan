package us.poliscore.legiscan.view;

public enum LegiscanStance {
    WATCH("Watch"),
    SUPPORT("Support"),
    OPPOSE("Oppose");

    private final String code;

    LegiscanStance(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal();
    }

    public static LegiscanStance fromValue(int value) {
        if (value < 0 || value >= values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value];
    }

    public static LegiscanStance fromCode(String code) {
        for (LegiscanStance stance : values()) {
            if (stance.code.equalsIgnoreCase(code)) {
                return stance;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

