package com.example.moneymanager.service;

import com.example.moneymanager.dto.response.ExchangeTokenResponse;
import com.example.moneymanager.dto.response.LoginResponseDTO;
import com.example.moneymanager.entity.RefreshToken;
import com.example.moneymanager.entity.User;
import com.example.moneymanager.helper.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JWTService {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    private final JwtEncoder jwtEncoder;

    private final RefreshTokenService refreshTokenService;

    @Value("${minh.jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpiration;

    @Value("${minh.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    public String getScope(Authentication authentication) {
        if (authentication != null) {
            String scope = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(a -> a.startsWith("ROLE_"))
                    .collect(Collectors.joining(" "));
            return scope;
        }

        return "UNKNOWN";
    }

    public String createAccessToken(Authentication authentication, Long userId) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        // ghép các quyền thành 1 string: "ROLE_USER ROLE_ADMIN"
        String scope = this.getScope(authentication);

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(authentication.getName())
                .claim("id", userId)
                .claim("scope",scope)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

    }

    public String generateSecureToken() {
        byte[] randomBytes = new byte[64]; // 512 bits
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);
        String token = this.generateSecureToken();
        RefreshToken rf = new RefreshToken();
        rf.setCreatedAt(now);
        rf.setExpiredAt(validity);
        rf.setToken(token);
        rf.setUser(user);
        this.refreshTokenService.createRefreshToken(rf);
        return token;

    }

    public ExchangeTokenResponse handleExchangeToken(String inputToken) {
        RefreshToken currentRefreshToken = this.refreshTokenService.findByToken(inputToken);

        Instant now = Instant.now();
        if(now.isAfter(currentRefreshToken.getExpiredAt())) {
            throw new ResourceNotFoundException("Refresh token đã hết hạn");
        }

        User currentUser = currentRefreshToken.getUser();
        String newRefreshToken = this.createRefreshToken(currentUser);
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        // @formatter:off
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(currentUser.getEmail())
                .claim("id", currentUser.getId())
                .claim("fullName", currentUser.getFullName())
                .build();
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        String accessToken = this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();

        // @formatter:on

        ExchangeTokenResponse exToken = new ExchangeTokenResponse();
        exToken.setAccessToken(accessToken);
        exToken.setRefreshToken(newRefreshToken);
        exToken.setUser(new LoginResponseDTO.UserLogin(currentUser.getId(), currentUser.getFullName(), currentUser.getEmail()));

        this.refreshTokenService.deleteById(currentRefreshToken.getId());

        return exToken;
    }


}
