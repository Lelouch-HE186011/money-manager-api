package com.example.moneymanager.controller;

import com.example.moneymanager.dto.request.UserRequestDTO;
import com.example.moneymanager.dto.response.UserResponseDTO;
import com.example.moneymanager.helper.ApiResponse;
import com.example.moneymanager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class UserController {
    private final UserService profileService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDTO>> registerProfile(@Valid @RequestBody UserRequestDTO inputUser) {
        UserResponseDTO registeredUser = profileService.registerProfile(inputUser);
        return ApiResponse.created(registeredUser);
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateProfile(@RequestParam String token) {
        boolean isActivated = profileService.activateProfile(token);
        if (isActivated) {
            return ResponseEntity.ok("Profile activated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid activation token");
        }
    }

}
