package web.programming.exceptions;

/** Chữ ký của JWT không hợp lệ (sai khóa, sai thuật toán hoặc token bị sửa). */
public class JwtSignatureException extends RuntimeException {

	public JwtSignatureException(String message) {
		super(message);
	}
}
