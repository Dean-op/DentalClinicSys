package com.dentalclinic.controller;

import com.dentalclinic.common.ApiResponse;
import com.dentalclinic.entity.UserAccount;
import com.dentalclinic.security.JwtService;
import com.dentalclinic.security.UserPrincipal;
import com.dentalclinic.service.ClinicService;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ClinicService clinicService;

    public AuthController(AuthenticationManager authenticationManager, JwtService jwtService, ClinicService clinicService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.clinicService = clinicService;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Validated @RequestBody LoginRequest request) {
        var authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        UserAccount account = clinicService.users().selectById(principal.id);
        account.lastLoginAt = LocalDateTime.now();
        clinicService.users().updateById(account);
        return ApiResponse.ok(Map.of("token", jwtService.issue(principal), "user", clinicService.meOf(principal)));
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Validated @RequestBody RegisterRequest request) {
        UserAccount account = clinicService.registerPatient(request.username(), request.password(), request.name(), request.phone());
        return ApiResponse.ok(Map.of("id", account.id, "username", account.username));
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record RegisterRequest(@NotBlank String username, @NotBlank String password, String name, String phone) {}
}
