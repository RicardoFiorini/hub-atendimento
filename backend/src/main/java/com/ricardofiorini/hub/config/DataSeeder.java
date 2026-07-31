package com.ricardofiorini.hub.config;

import com.ricardofiorini.hub.model.Agent;
import com.ricardofiorini.hub.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (agentRepository.count() == 0) {
            Agent admin = Agent.builder()
                    .name("Ricardo Fiorini")
                    .email("admin@hub.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Agent.Role.ADMIN)
                    .build();
            agentRepository.save(admin);
            System.out.println(">>> Agente padrão criado: admin@hub.local / admin123 (troque a senha em produção)");
        }
    }
}
