package com.arcadia.auth.service;

import com.arcadia.auth.dto.request.LoginRequest;
import com.arcadia.auth.dto.request.RegisterRequest;
import com.arcadia.auth.dto.response.AuthResponse;
import com.arcadia.auth.dto.response.UserResponse;
import com.arcadia.auth.mapper.UserMapper;
import com.arcadia.common.exception.EmailAlreadyExistsException;
import com.arcadia.common.exception.NicknameAlreadyExistsException;
import com.arcadia.common.exception.UnauthorizedException;
import com.arcadia.entity.RefreshToken;
import com.arcadia.entity.Role;
import com.arcadia.entity.User;
import com.arcadia.repository.RefreshTokenRepository;
import com.arcadia.repository.RoleRepository;
import com.arcadia.repository.UserRepository;
import com.arcadia.security.CustomUserDetails;
import com.arcadia.security.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {

    private static final String DEFAULT_ROLE = "ROLE_USER";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RefreshTokenRepository refreshTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new NicknameAlreadyExistsException("Nickname already taken: " + request.nickname());
        }

        User user = User.builder()
                .nickname(request.nickname())
                .email(request.email().toLowerCase(Locale.ROOT))
                .passwordHash(passwordEncoder.encode(request.password()))
                .emailVerified(false)
                .enabled(true)
                .build();

        Role userRole = roleRepository.findByName(DEFAULT_ROLE)
                .orElseGet(() -> roleRepository.save(Role.builder().name(DEFAULT_ROLE).build()));
        user.addRole(userRole);

        user = userRepository.save(user);
        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
            CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
            return buildAuthResponse(principal.getUser());
        } catch (AuthenticationException ex) {
            throw new UnauthorizedException("Invalid credentials");
        }
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (stored.isRevoked() || stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        stored.setRevoked(true);
        refreshTokenRepository.save(stored);

        return buildAuthResponse(stored.getUser());
    }

    private AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = toUserDetails(user);
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshJwt = jwtService.generateRefreshToken(userDetails);
        LocalDateTime refreshExpiresAt = jwtService.extractClaim(refreshJwt, Claims::getExpiration)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        refreshTokenRepository.findByUserIdAndRevokedFalse(user.getId())
                .forEach(token -> token.setRevoked(true));
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .token(refreshJwt)
                .expiresAt(refreshExpiresAt)
                .build());

        UserResponse userResponse = userMapper.toResponse(user);
        return AuthResponse.of(accessToken, refreshJwt, jwtService.getAccessTokenTtlSeconds(), userResponse);
    }

    private CustomUserDetails toUserDetails(User user) {
        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
        return new CustomUserDetails(user, authorities);
    }
}
