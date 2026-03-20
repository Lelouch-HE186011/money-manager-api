package com.example.moneymanager.controller;

import com.example.moneymanager.dto.request.LoginRequestDTO;
import com.example.moneymanager.dto.response.ExchangeTokenResponse;
import com.example.moneymanager.dto.response.LoginResponseDTO;
import com.example.moneymanager.entity.RefreshToken;
import com.example.moneymanager.entity.User;
import com.example.moneymanager.helper.ApiResponse;
import com.example.moneymanager.service.JWTService;
import com.example.moneymanager.service.RefreshTokenService;
import com.example.moneymanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController {

    @Value("${minh.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/auth/login")
    public ResponseEntity<?> postLogin(@Valid @RequestBody LoginRequestDTO dto) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);

        User currentUser = this.userService.findUserByEmail(authentication.getName());

        String accessToken = this.jwtService.createAccessToken(authentication, currentUser.getId());
        String refreshToken = this.jwtService.createRefreshToken(currentUser);

        LoginResponseDTO res = new LoginResponseDTO();
        res.setAccessToken(accessToken);
        res.setUser(new LoginResponseDTO.UserLogin(currentUser.getId(), currentUser.getFullName(), authentication.getName()));
        res.setRefreshToken(refreshToken);

        //set cookie refresh token
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)              // bật nếu bạn dùng HTTPS
                .path("/")                 // cookie gửi mọi request
                .maxAge(refreshTokenExpiration)
                .sameSite("None")          // nếu FE/BE khác domain → cần "None"
                .build();

        ApiResponse<LoginResponseDTO> finalData = new ApiResponse<>(
                HttpStatus.OK, "", res, "");

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(finalData);
    }


    @PostMapping("/auth/refresh-with-cookie")
    public ResponseEntity<?> refreshTokenWithCookie(@CookieValue(required = false) String refreshToken) {
        ExchangeTokenResponse res = this.jwtService.handleExchangeToken(refreshToken);

        //set cookie refresh token
        ResponseCookie resCookies = ResponseCookie.from("refresh_token", res.getRefreshToken())
                .httpOnly(true)
                .secure(true)              // bật nếu bạn dùng HTTPS
                .path("/")                 // cookie gửi mọi request
                .maxAge(refreshTokenExpiration)
                .sameSite("None")          // nếu FE/BE khác domain → cần "None"
                .build();

        ApiResponse<ExchangeTokenResponse> finalData = new ApiResponse<>(
                HttpStatus.OK, "", res, "");

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, resCookies.toString()).body(finalData);

    }

    @GetMapping("/auth/account")
    public ResponseEntity<?> getAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt) auth.getPrincipal();

        String userId = jwt.getClaimAsString("id");
        String email = jwt.getSubject();
        String role = jwt.getClaimAsString("scope");
        String fullName = jwt.getClaimAsString("fullName");

        LoginResponseDTO.UserLogin user = new LoginResponseDTO.UserLogin();
        user.setId(Long.parseLong(userId));
        user.setFullName(fullName);
        user.setEmail(email);

        return ApiResponse.success(user);
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@AuthenticationPrincipal Jwt jwt,
                                    @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        String userId = jwt.getClaimAsString("id");
        String username = jwt.getSubject();

        RefreshToken currentTokenInDB = this.refreshTokenService.findByToken(refreshToken);
        this.refreshTokenService.deleteById(currentTokenInDB.getId());

        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refreshToken", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        ApiResponse<String> finalData = new ApiResponse<>(
                HttpStatus.OK, "", "ok", "");

        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString()).body(finalData);
    }
}
