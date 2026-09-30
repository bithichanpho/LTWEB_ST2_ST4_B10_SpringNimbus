package web.programming.services;

import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import web.programming.exceptions.JwtExpiredException;
import web.programming.exceptions.JwtMalformedException;
import web.programming.exceptions.JwtSignatureException;

/**
 * Tạo và xác thực JWT (HS256) bằng thư viện Nimbus JOSE + JWT.
 */
@Service
public class JwtService {

	@Value("${security.jwt.secret-key}")
	private String secretKey;

	@Value("${security.jwt.expiration-time}")
	private long jwtExpiration;

	public String extractUsername(String token) {
		return extractClaim(token, JWTClaimsSet::getSubject);
	}

	public <T> T extractClaim(String token, Function<JWTClaimsSet, T> claimsResolver) {
		final JWTClaimsSet claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		return buildToken(extraClaims, userDetails, jwtExpiration);
	}

	public long getExpirationTime() {
		return jwtExpiration;
	}

	private String buildToken(
			Map<String, Object> extraClaims,
			UserDetails userDetails,
			long expiration
	) {
		final long now = System.currentTimeMillis();

		JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder();
		extraClaims.forEach(claims::claim);
		claims.subject(userDetails.getUsername())
				.issueTime(new Date(now))
				// hết hạn sau "expiration" ms (security.jwt.expiration-time trong application.properties)
				.expirationTime(new Date(now + expiration));

		SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());

		try {
			signedJWT.sign(new MACSigner(getSignInKey()));
		} catch (JOSEException e) {
			throw new IllegalStateException("Không thể ký JWT", e);
		}

		return signedJWT.serialize();
	}

	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Date extractExpiration(String token) {
		return extractClaim(token, JWTClaimsSet::getExpirationTime);
	}

	/**
	 * Parse token, kiểm tra thuật toán + chữ ký + thời hạn rồi trả về các claim.
	 */
	private JWTClaimsSet extractAllClaims(String token) {
		try {
			SignedJWT signedJWT = SignedJWT.parse(token);

			// Chỉ chấp nhận HS256 (chặn alg=none hoặc đổi thuật toán)
			if (!JWSAlgorithm.HS256.equals(signedJWT.getHeader().getAlgorithm())) {
				throw new JwtSignatureException("Unsupported JWT algorithm");
			}

			if (!signedJWT.verify(new MACVerifier(getSignInKey()))) {
				throw new JwtSignatureException("JWT signature does not match locally computed signature");
			}

			JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

			Date exp = claims.getExpirationTime();
			if (exp == null || exp.before(new Date())) {
				throw new JwtExpiredException("JWT expired at " + exp);
			}

			return claims;
		} catch (ParseException e) {
			throw new JwtMalformedException("Malformed JWT", e);
		} catch (JOSEException e) {
			throw new JwtSignatureException("Cannot verify JWT signature: " + e.getMessage());
		}
	}

	private byte[] getSignInKey() {
		// Giữ nguyên cách giải mã Base64 như bản cũ để token/khóa cũ vẫn dùng được
		return Base64.getDecoder().decode(secretKey);
	}

}
