package com.example.moneymanager.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ExchangeTokenResponse {
    private String accessToken;

    private String refreshToken;

    private String tokenType = "Bearer";

    private LoginResponseDTO.UserLogin user;

}
