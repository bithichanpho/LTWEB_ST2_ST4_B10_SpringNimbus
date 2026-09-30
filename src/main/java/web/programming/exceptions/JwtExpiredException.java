package web.programming.exceptions;

/** JWT đã hết hạn (claim "exp" nhỏ hơn thời điểm hiện tại). */
public class JwtExpiredException extends RuntimeException {

	public JwtExpiredException(String message) {
		super(message);
	}
}
