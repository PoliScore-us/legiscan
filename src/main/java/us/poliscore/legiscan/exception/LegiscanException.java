package us.poliscore.legiscan.exception;

public class LegiscanException extends RuntimeException {
    private static final long serialVersionUID = 7507881246860239354L;

	public LegiscanException(String message) {
        super(message);
    }

    public LegiscanException(String message, Throwable cause) {
        super(message, cause);
    }
}
