package us.poliscore.legiscan.exception;

public class QuotaExceededException extends LegiscanException {
	private static final long serialVersionUID = -313491072084625819L;

	public QuotaExceededException(String message) {
		super(message);
	}
}
