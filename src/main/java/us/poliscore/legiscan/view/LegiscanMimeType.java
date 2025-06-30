package us.poliscore.legiscan.view;

public enum LegiscanMimeType {
    HTML("HTML", ".html"),
    PDF("PDF", ".pdf"),
    WORDPERFECT("WordPerfect", ".wpd"),
    MS_WORD("MS Word", ".doc"),
    RICH_TEXT_FORMAT("Rich Text Format", ".rtf"),
    MS_WORD_2007("MS Word 2007", ".docx");

    private final String code;
    private final String extension;

    LegiscanMimeType(String code, String extension) {
        this.code = code;
        this.extension = extension;
    }

    public String getCode() {
        return code;
    }

    public String getExtension() {
        return extension;
    }

    public int getValue() {
        return ordinal() + 1;
    }

    public static LegiscanMimeType fromValue(int value) {
        if (value <= 0 || value > values().length) {
            throw new IllegalArgumentException("Invalid value: " + value);
        }
        return values()[value - 1];
    }

    public static LegiscanMimeType fromCode(String code) {
        for (LegiscanMimeType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown code: " + code);
    }
}

