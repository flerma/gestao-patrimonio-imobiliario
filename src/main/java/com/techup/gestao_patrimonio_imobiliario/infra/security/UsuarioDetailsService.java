package com.techup.gestao_patrimonio_imobiliario.infra.security;

import java.util.List;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioRepository;

/**
 * Implementação mínima exigida pelo contrato do Spring Security. A filtragem
 * de autenticação da API (JwtAuthenticationFilter) resolve a identidade
 * diretamente a partir das claims do JWT, sem consultar o banco a cada
 * requisição - este serviço fica disponível para eventuais usos futuros de
 * @AuthenticationPrincipal / login por formulário.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UsuarioEntity usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        return new User(
                usuario.getEmail(),
                usuario.getSenha() != null ? usuario.getSenha() : "",
                List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        "ROLE_" + usuario.getRole().name())));
    }
}
