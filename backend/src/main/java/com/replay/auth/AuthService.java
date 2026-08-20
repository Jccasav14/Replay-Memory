package com.replay.auth;

import com.replay.auth.dto.AuthResponse;
import com.replay.auth.dto.LoginRequest;
import com.replay.auth.dto.RefreshTokenRequest;
import com.replay.auth.dto.RegisterRequest;
import com.replay.common.BadRequestException;
import com.replay.common.ResourceNotFoundException;
import com.replay.common.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final StringRedisTemplate redisTemplate;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("An account with this email already exists");
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .role(Role.ROLE_USER)
                .status(UserStatus.ACTIVE)
                .settings(new UserSettings())
                .build();

        User savedUser = userRepository.save(user);
        log.info("Registered new user: {} (ID: {})", savedUser.getEmail(), savedUser.getId());

        String accessToken = tokenProvider.generateAccessToken(savedUser);
        String refreshToken = tokenProvider.generateRefreshToken(savedUser);

        saveRefreshTokenInRedis(savedUser.getId(), null, refreshToken);

        return buildAuthResponse(savedUser, accessToken, refreshToken);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedException("Account is locked. Please contact support.");
        }

        String accessToken = tokenProvider.generateAccessToken(user);
        String refreshToken = tokenProvider.generateRefreshToken(user);

        saveRefreshTokenInRedis(user.getId(), request.getDeviceId(), refreshToken);
        log.info("User logged in: {} (ID: {})", user.getEmail(), user.getId());

        return buildAuthResponse(user, accessToken, refreshToken);
    }

    public AuthResponse refresh(RefreshTokenRequest request) {
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new UnauthorizedException("Invalid or expired refresh token");
        }

        String userId = tokenProvider.getUserIdFromToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String storedRefreshToken = getStoredRefreshToken(userId, request.getDeviceId());
        if (storedRefreshToken == null || !storedRefreshToken.equals(request.getRefreshToken())) {
            throw new UnauthorizedException("Refresh token was revoked or replaced");
        }

        String newAccessToken = tokenProvider.generateAccessToken(user);
        String newRefreshToken = tokenProvider.generateRefreshToken(user);

        saveRefreshTokenInRedis(user.getId(), request.getDeviceId(), newRefreshToken);

        return buildAuthResponse(user, newAccessToken, newRefreshToken);
    }

    public void logout(String token, String deviceId) {
        if (token != null && token.startsWith("Bearer ")) {
            String jwt = token.substring(7);
            if (tokenProvider.validateToken(jwt)) {
                String userId = tokenProvider.getUserIdFromToken(jwt);
                String jti = tokenProvider.getJtiFromToken(jwt);

                // Blacklist token in Redis until expiration
                try {
                    redisTemplate.opsForValue().set("auth:token:blacklist:" + jti, "revoked",
                            tokenProvider.getExpirationMs(), TimeUnit.MILLISECONDS);
                    redisTemplate.delete(getRedisRefreshKey(userId, deviceId));
                    log.info("User {} logged out successfully. Token {} blacklisted.", userId, jti);
                } catch (Exception e) {
                    log.warn("Redis not accessible during logout blacklisting: {}", e.getMessage());
                }
            }
        }
    }

    private void saveRefreshTokenInRedis(String userId, String deviceId, String refreshToken) {
        try {
            String key = getRedisRefreshKey(userId, deviceId);
            redisTemplate.opsForValue().set(key, refreshToken, 7, TimeUnit.DAYS);
        } catch (Exception e) {
            log.warn("Redis unavailable for refresh token storage: {}", e.getMessage());
        }
    }

    private String getStoredRefreshToken(String userId, String deviceId) {
        try {
            return redisTemplate.opsForValue().get(getRedisRefreshKey(userId, deviceId));
        } catch (Exception e) {
            log.warn("Redis unavailable for refresh token check: {}", e.getMessage());
            return null;
        }
    }

    private String getRedisRefreshKey(String userId, String deviceId) {
        return "auth:refresh:" + userId + ":" + (deviceId != null ? deviceId : "default");
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresInMs(tokenProvider.getExpirationMs())
                .user(AuthResponse.UserDto.builder()
                        .id(user.getId())
                        .email(user.getEmail())
                        .fullName(user.getFullName())
                        .role(user.getRole())
                        .build())
                .build();
    }
}
