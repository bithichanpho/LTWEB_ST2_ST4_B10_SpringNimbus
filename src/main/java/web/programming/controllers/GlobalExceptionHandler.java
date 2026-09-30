package web.programming.controllers;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import web.programming.exceptions.JwtExpiredException;
import web.programming.exceptions.JwtMalformedException;
import web.programming.exceptions.JwtSignatureException;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(Exception.class)
	public ProblemDetail handleSecurityException(Exception exception) {
		ProblemDetail errorDetail = null;

		// TODO send this stack trace to an observability tool
		exception.printStackTrace();

		if (exception instanceof BadCredentialsException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The username or password is incorrect");

			return errorDetail;
		}

		if (exception instanceof AccountStatusException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "The account is locked");
		}

		if (exception instanceof AccessDeniedException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(403), exception.getMessage());
			errorDetail.setProperty("description", "You are not authorized to access this resource");
		}

		if (exception instanceof JwtSignatureException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The JWT signature is invalid");
		}

		if (exception instanceof JwtExpiredException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The JWT token has expired");
		}

		if (exception instanceof JwtMalformedException) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(401), exception.getMessage());
			errorDetail.setProperty("description", "The JWT token is malformed");
		}

		if (errorDetail == null) {
			errorDetail = ProblemDetail.forStatusAndDetail(HttpStatusCode.valueOf(500), exception.getMessage());
			errorDetail.setProperty("description", "Unknown internal server error.");
		}

		return errorDetail;
	}

}
