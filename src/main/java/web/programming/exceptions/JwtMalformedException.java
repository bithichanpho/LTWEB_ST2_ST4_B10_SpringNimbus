package web.programming.exceptions;

/** Chuỗi token không phải là JWT hợp lệ (sai định dạng, không parse được). */
public class JwtMalformedException extends RuntimeException {

	public JwtMalformedException(String message, Throwable cause) {
		super(message, cause);
	}
}
