package us.poliscore.legiscan.view;

public enum LegiscanPushReason {
    NEWBILL("Newbill"),
    STATUS_CHANGE("StatusChange"),
    CHAMBER("Chamber"),
    COMPLETE("Complete"),
    TITLE("Title"),
    DESCRIPTION("Description"),
    COMM_REFER("CommRefer"),
    COMM_REPORT("CommReport"),
    SPONSOR_ADD("SponsorAdd"),
    SPONSOR_REMOVE("SponsorRemove"),
    SPONSOR_CHANGE("SponsorChange"),
    HISTORY_ADD("HistoryAdd"),
    HISTORY_REMOVE("HistoryRemove"),
    HISTORY_REVISED("HistoryRevised"),
    HISTORY_MAJOR("HistoryMajor"),
    HISTORY_MINOR("HistoryMinor"),
    SUBJECT_ADD("SubjectAdd"),
    SUBJECT_REMOVE("SubjectRemove"),
    SAST("SAST"),
    TEXT("Text"),
    AMENDMENT("Amendment"),
    SUPPLEMENT("Supplement"),
    VOTE("Vote"),
    CALENDAR("Calendar"),
    PROGRESS("Progress");

    private final String code;

    LegiscanPushReason(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanPushReason fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanPushReason fromCode(String code) {
        for (LegiscanPushReason reason : values()) {
            if (reason.code.equalsIgnoreCase(code)) {
                return reason;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

