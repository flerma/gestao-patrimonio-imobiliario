package com.techup.gestao_patrimonio_imobiliario.data.usuario;

import java.util.Optional;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.core.enums.ProvedorAutenticacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<UsuarioEntity, UUID> {

    Optional<UsuarioEntity> findByEmail(String email);

    Optional<UsuarioEntity> findByEmailIgnoreCase(String email);

    Optional<UsuarioEntity> findByProvedorAutenticacaoAndIdUsuarioProvedor(
            ProvedorAutenticacao provedorAutenticacao, String idUsuarioProvedor);

    boolean existsByEmail(String email);

    boolean existsByTelefone(String telefone);
}
