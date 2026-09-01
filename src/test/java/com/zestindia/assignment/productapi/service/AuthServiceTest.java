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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private CustomUserDetailsService userDetailsService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                authService,
                "refreshTokenExpirationMs",
                604800000L
        );
    }

    @Test
    void registerCreatesUserWithUserRoleAndReturnsTokens() {
        Role userRole = mock(Role.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(roleRepository.findByName("ROLE_USER"))
                .thenReturn(Optional.of(userRole));

        when(passwordEncoder.encode("StrongPass@123"))
                .thenReturn("hashed-password");

        when(appUserRepository.save(any(AppUser.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(userDetailsService.loadUserByUsername("aekansh"))
                .thenReturn(userDetails);

        when(jwtService.generateAccessToken(userDetails))
                .thenReturn("access-token");

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.register(
                new RegisterRequest(
                        "  aekansh  ",
                        "  AEKANSH@EXAMPLE.COM  ",
                        "StrongPass@123"
                )
        );

        ArgumentCaptor<AppUser> userCaptor =
                ArgumentCaptor.forClass(AppUser.class);

        verify(appUserRepository).save(userCaptor.capture());

        AppUser savedUser = userCaptor.getValue();

        assertThat(savedUser.getUsername()).isEqualTo("aekansh");
        assertThat(savedUser.getEmail()).isEqualTo("aekansh@example.com");
        assertThat(savedUser.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(savedUser.getRoles()).contains(userRole);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void registerRejectsDuplicateUsername() {
        when(appUserRepository.existsByUsername("aekansh"))
                .thenReturn(true);

        assertThatThrownBy(() -> authService.register(
                new RegisterRequest(
                        "aekansh",
                        "aekansh@example.com",
                        "StrongPass@123"
                )
        ))
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT)
                );
    }

    @Test
    void loginRejectsIncorrectPassword() {
        AppUser user = mock(AppUser.class);

        when(appUserRepository.findByUsername("aekansh"))
                .thenReturn(Optional.of(user));

        when(user.isEnabled()).thenReturn(true);
        when(user.getPasswordHash()).thenReturn("stored-hash");

        when(passwordEncoder.matches(
                "WrongPassword",
                "stored-hash"
        )).thenReturn(false);

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("aekansh", "WrongPassword")
        ))
                .isInstanceOfSatisfying(
                        ResponseStatusException.class,
                        exception -> assertThat(exception.getStatusCode())
                                .isEqualTo(HttpStatus.UNAUTHORIZED)
                );
    }

    @Test
    void refreshRevokesOldTokenAndReturnsNewTokenPair() {
        AppUser user = mock(AppUser.class);
        RefreshToken oldToken = mock(RefreshToken.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(oldToken));

        when(oldToken.isRevoked()).thenReturn(false);
        when(oldToken.isExpired()).thenReturn(false);
        when(oldToken.getUser()).thenReturn(user);

        when(user.getUsername()).thenReturn("aekansh");

        when(userDetailsService.loadUserByUsername("aekansh"))
                .thenReturn(userDetails);

        when(jwtService.generateAccessToken(userDetails))
                .thenReturn("new-access-token");

        when(refreshTokenRepository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AuthResponse response = authService.refresh(
                new RefreshTokenRequest("old-refresh-token")
        );

        verify(oldToken).revoke();
        verify(refreshTokenRepository).save(oldToken);

        assertThat(response.accessToken())
                .isEqualTo("new-access-token");

        assertThat(response.refreshToken())
                .isNotBlank()
                .isNotEqualTo("old-refresh-token");
    }

    @Test
    void logoutRevokesExistingRefreshToken() {
        RefreshToken refreshToken = mock(RefreshToken.class);

        when(refreshTokenRepository.findByTokenHash(anyString()))
                .thenReturn(Optional.of(refreshToken));

        when(refreshToken.isRevoked()).thenReturn(false);

        authService.logout(
                new RefreshTokenRequest("refresh-token")
        );

        verify(refreshToken).revoke();
        verify(refreshTokenRepository).save(refreshToken);
    }
}