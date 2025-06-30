package us.poliscore.legiscan.view;

public enum LegiscanParty {
    DEMOCRAT("Democrat"),
    REPUBLICAN("Republican"),
    INDEPENDENT("Independent"),
    GREEN_PARTY("Green Party"),
    LIBERTARIAN("Libertarian"),
    NON_PARTISAN("Nonpartisan");

    private final String code;

    LegiscanParty(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanParty fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanParty fromCode(String code) {
        for (LegiscanParty party : values()) {
            if (party.code.equalsIgnoreCase(code)) {
                return party;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

