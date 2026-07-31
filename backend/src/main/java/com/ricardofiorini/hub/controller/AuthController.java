package com.ricardofiorini.hub.controller;

import com.ricardofiorini.hub.dto.AuthDtos.LoginRequest;
import com.ricardofiorini.hub.dto.AuthDtos.LoginResponse;
import com.ricardofiorini.hub.model.Agent;
import com.ricardofiorini.hub.repository.AgentRepository;
import com.ricardofiorini.hub.security.AgentUserDetailsService;
import com.ricardofiorini.hub.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AgentUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AgentRepository agentRepository;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.email());
        Agent agent = agentRepository.findByEmail(request.email()).orElseThrow();

        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(new LoginResponse(token, agent.getName(), agent.getEmail(), agent.getRole().name()));
    }
}
