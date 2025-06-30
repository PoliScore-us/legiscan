package us.poliscore.legiscan.view;

public enum LegiscanSastType {
    SAME_AS("Same As"),
    SIMILAR_TO("Similar To"),
    REPLACED_BY("Replaced By"),
    REPLACES("Replaces"),
    CROSS_FILED("Cross-filed"),
    ENABLING_FOR("Enabling For"),
    ENABLED_BY("Enabled By"),
    RELATED("Related"),
    CARRY_OVER("Carry Over");

    private final String code;

    LegiscanSastType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanSastType fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanSastType fromCode(String code) {
        for (LegiscanSastType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

