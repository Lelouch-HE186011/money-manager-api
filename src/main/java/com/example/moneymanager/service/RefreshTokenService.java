package com.example.moneymanager.service;


import com.example.moneymanager.entity.RefreshToken;
import com.example.moneymanager.helper.exception.ResourceNotFoundException;
import com.example.moneymanager.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;

    public void createRefreshToken(RefreshToken rf) {
        this.refreshTokenRepository.save(rf);
    }

    public RefreshToken findByToken(String token) {
        return this.refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Refresh token not found"));
    }

    public void deleteById(Long id) {
        this.refreshTokenRepository.deleteById(id);
    }

}
