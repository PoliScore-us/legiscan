package us.poliscore.legiscan.view;

public enum LegiscanBillType {
    BILL("B"),
    RESOLUTION("R"),
    CONCURRENT_RESOLUTION("CR"),
    JOINT_RESOLUTION("JR"),
    JOINT_RESOLUTION_CONSTITUTIONAL_AMENDMENT("JRCA"),
    EXECUTIVE_ORDER("EO"),
    CONSTITUTIONAL_AMENDMENT("CA"),
    MEMORIAL("M"),
    CLAIM("CL"),
    COMMENDATION("C"),
    COMMITTEE_STUDY_REQUEST("CSR"),
    JOINT_MEMORIAL("JM"),
    PROCLAMATION("P"),
    STUDY_REQUEST("SR"),
    ADDRESS("A"),
    CONCURRENT_MEMORIAL("CM"),
    INITIATIVE("I"),
    PETITION("PET"),
    STUDY_BILL("SB"),
    INITIATIVE_PETITION("IP"),
    REPEAL_BILL("RB"),
    REMONSTRATION("RM"),
    COMMITTEE_BILL("CB");

    private final String code;

    LegiscanBillType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return this.ordinal() + 1; // 1-based value
    }

    public static LegiscanBillType fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanBillType fromCode(String code) {
        for (LegiscanBillType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}
