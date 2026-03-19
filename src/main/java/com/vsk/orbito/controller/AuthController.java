package com.vsk.orbito.controller;



import com.vsk.orbito.dto.request.LoginRequest;
import com.vsk.orbito.dto.request.RegisterRequest;
import com.vsk.orbito.dto.response.ApiResponse;
import com.vsk.orbito.dto.response.AuthResponse;
import com.vsk.orbito.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register, Login, Token refresh")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(
                ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login and get JWT token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Authorization") String authHeader) {
        String refreshToken = authHeader.substring(7);
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.success("Token refreshed successfully", response));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current logged-in user info")
    public ResponseEntity<ApiResponse<String>> me(
            @RequestHeader("Authorization") String authHeader) {
        return ResponseEntity.ok(
                ApiResponse.success("Authenticated", "You are logged in"));
    }
}
//```
//
//        ---
//
//        ## Step 4 — Run it
//
//        In IntelliJ, right-click `OrbitoApplication.java` → Run. Watch the console. You should see:
//        ```
//Started OrbitoApplication in 3.x seconds
//Hibernate: create table users (...)
//```
//
//MySQL will auto-create the `orbito_db` database and `users` table.
//
//---
//
//        ## Step 5 — Test in Postman
//
//**Register:**
//        ```
//POST http://localhost:8080/api/auth/register
//Content-Type: application/json
//
//{
//    "name": "Sai Kishore",
//        "email": "sai@orbito.com",
//        "password": "sai123",
//        "role": "DEVELOPER"
//}
//```
//
//        **Login:**
//        ```
//POST http://localhost:8080/api/auth/login
//Content-Type: application/json
//
//{
//    "email": "sai@orbito.com",
//        "password": "sai123"
//}
//```
//
//        **Swagger UI** — open in browser:
//        ```
//        http://localhost:8080/swagger-ui.html
