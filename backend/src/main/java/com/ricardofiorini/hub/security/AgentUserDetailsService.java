package com.ricardofiorini.hub.security;

import com.ricardofiorini.hub.model.Agent;
import com.ricardofiorini.hub.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AgentUserDetailsService implements UserDetailsService {

    private final AgentRepository agentRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Agent agent = agentRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Agente não encontrado: " + email));

        return User.builder()
                .username(agent.getEmail())
                .password(agent.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_" + agent.getRole().name()))
                .build();
    }
}
