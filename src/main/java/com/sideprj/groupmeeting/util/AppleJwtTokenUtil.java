package com.sideprj.groupmeeting.util;

import com.sideprj.groupmeeting.service.AuthService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.util.Date;

@Component
public class AppleJwtTokenUtil {
    private static String TEAM_ID;  // Your Apple Developer Team ID
    private static String KEY_ID;    // The Key ID from the .p8 file
    private static String applePrivateKeyPath;

    public static String generateToken() throws Exception {
        PrivateKey privateKey = AuthService.getPrivateKey(applePrivateKeyPath);
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        return Jwts.builder()
                   .setHeaderParam("kid", KEY_ID)
                   .setIssuer(TEAM_ID)
                   .setIssuedAt(now)
                   .setExpiration(new Date(nowMillis + 60 * 60 * 1000))  // Valid for one hour
                   .signWith(SignatureAlgorithm.ES256, privateKey)
                   .compact();
    }

    @Value("${apple.key.id}")
    public void setKeyId(String keyId) {
        KEY_ID = keyId;
    }

    @Value("${apple.team.id}")
    public void setTeamId(String teamId) {
        TEAM_ID = teamId;
    }

    @Value("${apple.private_key_path}")
    public void setApplePrivateKeyPath(String keyPath) {
        applePrivateKeyPath = keyPath;
    }
}
