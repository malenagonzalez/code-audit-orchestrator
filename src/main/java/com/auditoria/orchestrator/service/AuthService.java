package com.auditoria.orchestrator.service;

import com.auditoria.orchestrator.dto.request.*;
import com.auditoria.orchestrator.dto.response.*;
import com.auditoria.orchestrator.entity.User;
import com.auditoria.orchestrator.exception.ConflictException;
import com.auditoria.orchestrator.repository.UserRepository;
import com.auditoria.orchestrator.security.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("El email ya está registrado");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("El nombre de usuario ya está en uso");
        }

        var user = userRepository.save(User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password()))
                .build());

        return buildAuthResponse(user);
    }

    public AuthResponse login(LoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email(), req.password()));

        var user = userRepository.findByEmail(req.email()).orElseThrow();
        return buildAuthResponse(user);
    }

    private AuthResponse buildAuthResponse(User user) {
        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtTokenProvider.generateToken(ud);
        return new AuthResponse(token, expirationMs / 1000,
                new UserDto(user.getId(), user.getUsername(), user.getEmail()));
    }
}
