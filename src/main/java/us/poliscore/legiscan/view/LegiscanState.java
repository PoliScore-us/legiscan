package us.poliscore.legiscan.view;

public enum LegiscanState {
    ALABAMA("AL"),
    ALASKA("AK"),
    ARIZONA("AZ"),
    ARKANSAS("AR"),
    CALIFORNIA("CA"),
    COLORADO("CO"),
    CONNECTICUT("CT"),
    DELAWARE("DE"),
    FLORIDA("FL"),
    GEORGIA("GA"),
    HAWAII("HI"),
    IDAHO("ID"),
    ILLINOIS("IL"),
    INDIANA("IN"),
    IOWA("IA"),
    KANSAS("KS"),
    KENTUCKY("KY"),
    LOUISIANA("LA"),
    MAINE("ME"),
    MARYLAND("MD"),
    MASSACHUSETTS("MA"),
    MICHIGAN("MI"),
    MINNESOTA("MN"),
    MISSISSIPPI("MS"),
    MISSOURI("MO"),
    MONTANA("MT"),
    NEBRASKA("NE"),
    NEVADA("NV"),
    NEW_HAMPSHIRE("NH"),
    NEW_JERSEY("NJ"),
    NEW_MEXICO("NM"),
    NEW_YORK("NY"),
    NORTH_CAROLINA("NC"),
    NORTH_DAKOTA("ND"),
    OHIO("OH"),
    OKLAHOMA("OK"),
    OREGON("OR"),
    PENNSYLVANIA("PA"),
    RHODE_ISLAND("RI"),
    SOUTH_CAROLINA("SC"),
    SOUTH_DAKOTA("SD"),
    TENNESSEE("TN"),
    TEXAS("TX"),
    UTAH("UT"),
    VERMONT("VT"),
    VIRGINIA("VA"),
    WASHINGTON("WA"),
    WASHINGTON_DC("DC"),
    WEST_VIRGINIA("WV"),
    WISCONSIN("WI"),
    WYOMING("WY"),
	CONGRESS("US");

    private final String abbreviation;

    LegiscanState(String abbreviation) {
        this.abbreviation = abbreviation;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public int getId() {
        return ordinal() + 1;
    }

    public static LegiscanState fromId(int id) {
        if (id <= 0 || id > values().length) {
            throw new IllegalArgumentException("Invalid id: " + id);
        }
        return values()[id - 1];
    }

    public static LegiscanState fromAbbreviation(String abbr) {
        for (LegiscanState state : values()) {
            if (state.abbreviation.equalsIgnoreCase(abbr)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown abbreviation: " + abbr);
    }
}
