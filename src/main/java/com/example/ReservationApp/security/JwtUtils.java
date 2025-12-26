package com.example.ReservationApp.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;

/**
 * JWTトークンの生成・検証を行うユーティリティクラス
 */
@Service
public class JwtUtils {

    /** JWTトークンの有効期限（ミリ秒単位） */
    private static final long EXPIRATION_TIME_IN_MILLISEC = 1000L * 60L * 60L;
    // private static final long EXPIRATION_TIME_IN_MILLISEC = 1000L *5L;
    private static final long REFRESH_EXPIRATION_TIME = 1000L * 60L * 60L * 24L * 7L;

    private final StringRedisTemplate redisTemplate;

    public JwtUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private SecretKey key;
    /** application.propertiesから注入されるJWTシークレット文字列 */
    @Value("${secretJwtString}")
    private String secretJwtString;

    /**
     * Bean初期化時に呼び出。
     * secretJwtStringを使ってHMAC-SHA256署名用の鍵を生成。
     */
    @PostConstruct
    private void init() {
        byte[] keyByte = secretJwtString.getBytes(StandardCharsets.UTF_8);
        this.key = new SecretKeySpec(keyByte, "HmacSHA256");
    }

    /**
     * メールアドレスをサブジェクトとしてJWTトークンを生成
     * 
     * @param email トークンに埋め込むメールアドレス
     * @return 生成されたJWTトークン
     */
    public String generateToken(String email) {

        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_IN_MILLISEC))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(String email) {

        String refreshToken = Jwts.builder()
                .subject(email)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .signWith(key)
                .compact();
        redisTemplate.opsForValue().set(
                "refresh_token:" + email,  //overwrite data, 
                refreshToken,
                REFRESH_EXPIRATION_TIME,
                TimeUnit.MILLISECONDS);

        return refreshToken;
    }

    /**
     * JWTトークンからメールアドレス（ユーザー名）を取得
     * 
     * @param token JWTトークン
     * @return トークンに埋め込まれたメールアドレス
     */
    public String extractUsername(String token) {
        return extractClaims(token, Claims::getSubject);
    }

    /**
     * JWTトークンから任意のクレームを取得するメソッド
     * 
     * @param token          JWTトークン
     * @param claimsResolver Claimsから必要な情報を抽出するFunction
     * @param <T>            抽出する情報の型
     * @return 抽出した情報
     */
    public <T> T extractClaims(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * JWTトークンの全クレームを取得
     * 
     * @param token JWTトークン
     * @return クレーム情報
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }

    /**
     * JWTトークンが有効期限切れかを判定
     * 
     * @param token JWTトークン
     * @return 有効期限切れの場合はtrue
     */
    private Boolean isTokenExpired(String token) {
        return extractClaims(token, Claims::getExpiration).before(new Date());
    }

    /**
     * JWTトークンの検証を行う
     * 
     * @param token       JWTトークン
     * @param userDetails UserDetails情報
     * @return トークンがユーザーに対応しており、有効期限内であればtrue
     */
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public String refreshAccessToken(String email, String refreshToken) {
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(refreshToken)
                    .getPayload();
        } catch (Exception e) {
            throw new RuntimeException("不正なリフレッシュトークンです。署名の検証に失敗しました。");
        }

        Date expiration = claims.getExpiration();
        if (expiration.before(new Date())) {    
            throw new RuntimeException("リフレッシュトークンの有効期限が切れています。");
        }

        String storedRefreshToken = redisTemplate.opsForValue().get("refresh_token:" + email);

        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new RuntimeException("無効または期限切れのリフレッシュトークン");
        }

        return generateToken(email);
    }

    public void revokeRefreshToken(String email) {
        redisTemplate.delete("refresh_token:" + email);
    }

}
