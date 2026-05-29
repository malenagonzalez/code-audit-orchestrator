package com.codeaudit.orchestrator.service;

import com.codeaudit.orchestrator.dto.request.LoginRequest;
import com.codeaudit.orchestrator.dto.request.RegisterRequest;
import com.codeaudit.orchestrator.dto.response.AuthResponse;
import com.codeaudit.orchestrator.dto.response.UserDto;
import com.codeaudit.orchestrator.entity.User;
import com.codeaudit.orchestrator.exception.ConflictException;
import com.codeaudit.orchestrator.exception.NotFoundException;
import com.codeaudit.orchestrator.repository.UserRepository;
import com.codeaudit.orchestrator.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException("El email ya está registrado");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new ConflictException("El nombre de usuario ya está en uso");
        }

        User user = User.builder()
                .username(request.username())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, jwtExpiration / 1000,
                new UserDto(user.getId(), user.getUsername(), user.getEmail()));
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtService.generateToken(userDetails);
        return new AuthResponse(token, jwtExpiration / 1000,
                new UserDto(user.getId(), user.getUsername(), user.getEmail()));
    }
}
