package com.firstTry.Adventure.utils;

import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Json Web Token util
 * 
 * @author Roger
 */
public class JWTUtil {

	public static String createId() {
		return UUID.randomUUID().toString().toUpperCase().replace("-", "");
	}

	/**
	 * 由字符串生成加密key
	 * 
	 * @return
	 */
	private static SecretKey generalKey() {
		String seckey = "#GHFCV??%&UY`|564TYVUr*&%)(1&*2345hGVHJ$W^%HB)";
		byte[] encodedKey = Base64.decodeBase64(seckey);
		SecretKey key = new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
		return key;
	}

	/**
	 * 解密jwt
	 * 
	 * @param jwt
	 * @return
	 * @throws Exception
	 */
	public static Claims parseJWT(String jwt) throws Exception {
		SecretKey key = generalKey();
		Claims claims = Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody();
		return claims;
	}

	/**
	 * 创建jwt
	 * 
	 * @param subject
	 * @param ttlMillis
	 * @param sessionData
	 * @return
	 */
	public static String createJWT(String id, String subject, long ttlMillis) {

		long nowMillis = System.currentTimeMillis();
		long expMillis = nowMillis + ttlMillis;
		Date exp = new Date(expMillis);

		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		Date now = new Date(nowMillis);
		SecretKey key = generalKey();
		JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setExpiration(exp).signWith(signatureAlgorithm, key);

		return builder.compact();
	}
	
//	public static void main(String[] args) {
//		String jwt = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJzZXNzaW9uLjlENDZBMjZBRkQzRjQ5RUY4NEQ2RDUyMjFDRkNDQ0YwIiwiaWF0IjoxNTA0NDk0OTA4LCJzdWIiOiIxMzU5MTQ0MjI0MCIsImV4cCI6MTUwNDU4MTMwOH0.0H9_E-7FVXpgueKSKKTKXHAgoB30Uni2tAvSWI0jvEA";
//		try {
//			Claims claims = parseJWT(jwt);
//			System.out.println(claims.getSubject());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
