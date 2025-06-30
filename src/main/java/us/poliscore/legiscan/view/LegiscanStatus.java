package us.poliscore.legiscan.view;

public enum LegiscanStatus {
    NA("N/A"),
    INTRODUCED("Introduced"),
    ENGROSSED("Engrossed"),
    ENROLLED("Enrolled"),
    PASSED("Passed"),
    VETOED("Vetoed"),
    FAILED("Failed"),
    OVERRIDE("Override"),
    CHAPTERED("Chaptered"),
    REFER("Refer"),
    REPORT_PASS("Report Pass"),
    REPORT_DNP("Report DNP"),
    DRAFT("Draft");

    private final String code;

    LegiscanStatus(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal();
    }

    public static LegiscanStatus fromValue(int value) {
        if (value < 0 || value >= values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value];
    }

    public static LegiscanStatus fromCode(String code) {
        for (LegiscanStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

