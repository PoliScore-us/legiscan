package us.poliscore.legiscan.view;

public enum LegiscanVoteStatus {
    YEA("Yea"),
    NAY("Nay"),
    ABSTAIN("Not Voting / Abstain"),
    ABSENT("Absent / Excused");

    private final String description;

    LegiscanVoteStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanVoteStatus fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanVoteStatus fromDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty.");
        }

        String normalized = description.trim().toLowerCase();

        for (LegiscanVoteStatus status : values()) {
            String statusDesc = status.description.toLowerCase();

            if (statusDesc.contains(normalized) || normalized.contains(status.name().toLowerCase())) {
                return status;
            }

            // Additional common word mappings
            switch (normalized) {
                case "yes":
                case "yea":
                    return YEA;
                case "no":
                case "nay":
                    return NAY;
                case "abstain":
                case "not voting":
                case "notvoting":
                    return ABSTAIN;
                case "absent":
                case "excused":
                    return ABSENT;
            }
        }

        throw new IllegalArgumentException("Unknown description: " + description);
    }

}
