package us.poliscore.legiscan.view;

public enum LegiscanSupplementType {
    FISCAL_NOTE("Fiscal Note"),
    ANALYSIS("Analysis"),
    FISCAL_NOTE_ANALYSIS("Fiscal Note/Analysis"),
    VOTE_IMAGE("Vote Image"),
    LOCAL_MANDATE("Local Mandate"),
    CORRECTIONS_IMPACT("Corrections Impact"),
    MISCELLANEOUS("Miscellaneous"),
    VETO_LETTER("Veto Letter");

    private final String code;

    LegiscanSupplementType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanSupplementType fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanSupplementType fromCode(String code) {
        for (LegiscanSupplementType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

