package com.example.moneymanager.controller;

import com.example.moneymanager.helper.ApiResponse;
import com.example.moneymanager.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashoardController {
    private final DashboardService  dashboardService;

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardData() {
        Map<String,Object> dashboarData = this.dashboardService.getDashboardData();
        return ApiResponse.success(dashboarData);
    }
}
