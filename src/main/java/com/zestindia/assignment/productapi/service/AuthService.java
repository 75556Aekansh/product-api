package com.zestindia.assignment.productapi.service;

import com.zestindia.assignment.productapi.dto.request.LoginRequest;
import com.zestindia.assignment.productapi.dto.request.RefreshTokenRequest;
import com.zestindia.assignment.productapi.dto.request.RegisterRequest;
import com.zestindia.assignment.productapi.dto.response.AuthResponse;
import com.zestindia.assignment.productapi.entity.AppUser;
import com.zestindia.assignment.productapi.entity.RefreshToken;
import com.zestindia.assignment.productapi.entity.Role;
import com.zestindia.assignment.productapi.repository.AppUserRepository;
import com.zestindia.assignment.productapi.repository.RefreshTokenRepository;
import com.zestindia.assignment.productapi.repository.RoleRepository;
import com.zestindia.assignment.productapi.security.CustomUserDetailsService;
import com.zestindia.assignment.productapi.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Value("${application.security.jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    public AuthService(
            AppUserRepository appUserRepository,
            RoleRepository roleRepository,
            RefreshTokenRepository refreshTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService
    ) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (appUserRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Username is already in use"
            );
        }

        if (appUserRepository.existsByEmail(request.email())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already in use"
            );
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new IllegalStateException("ROLE_USER is missing"));

        AppUser user = new AppUser(
                request.username().trim(),
                request.email().trim().toLowerCase(),
                passwordEncoder.encode(request.password())
        );

        user.addRole(userRole);
        AppUser savedUser = appUserRepository.save(user);

        return issueTokens(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = appUserRepository.findByUsername(request.username())
                .orElseThrow(() -> invalidCredentials());

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw invalidCredentials();
        }

        return issueTokens(user);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken oldToken = refreshTokenRepository
                .findByTokenHash(hashToken(request.refreshToken()))
                .orElseThrow(() -> invalidRefreshToken());

        if (oldToken.isRevoked() || oldToken.isExpired()) {
            throw invalidRefreshToken();
        }

        oldToken.revoke();
        refreshTokenRepository.save(oldToken);

        return issueTokens(oldToken.getUser());
    }

    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(hashToken(request.refreshToken()))
                .ifPresent(token -> {
                    if (!token.isRevoked()) {
                        token.revoke();
                        refreshTokenRepository.save(token);
                    }
                });
    }

    private AuthResponse issueTokens(AppUser user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        String accessToken = jwtService.generateAccessToken(userDetails);
        String rawRefreshToken = UUID.randomUUID().toString() + UUID.randomUUID();
        String refreshTokenHash = hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken(
                refreshTokenHash,
                user,
                Instant.now().plusMillis(refreshTokenExpirationMs)
        );

        refreshTokenRepository.save(refreshToken);

        return new AuthResponse(
                accessToken,
                rawRefreshToken,
                "Bearer"
        );
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid username or password"
        );
    }

    private ResponseStatusException invalidRefreshToken() {
        return new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Refresh token is invalid, expired, or revoked"
        );
    }

    private String hashToken(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));

            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}