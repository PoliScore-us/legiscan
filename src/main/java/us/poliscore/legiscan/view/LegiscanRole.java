package us.poliscore.legiscan.view;

public enum LegiscanRole {
    REPRESENTATIVE("Representative / Lower Chamber"),
    SENATOR("Senator / Upper Chamber"),
    JOINT_CONFERENCE("Joint Conference");

    private final String code;

    LegiscanRole(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanRole fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanRole fromCode(String code) {
        for (LegiscanRole role : values()) {
            if (role.code.equalsIgnoreCase(code)) {
                return role;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

