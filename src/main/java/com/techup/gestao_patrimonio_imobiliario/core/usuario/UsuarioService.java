package com.techup.gestao_patrimonio_imobiliario.core.usuario;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.usuario.UsuarioRequest;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusUsuario;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioRepository;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioMapper;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario criar(UsuarioRequest request) {
        LocalDateTime agora = LocalDateTime.now();
        Usuario usuario = Usuario.builder()
                .id(UUID.randomUUID())
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .provedorAutenticacao(request.getProvedorAutenticacao())
                .idUsuarioProvedor(request.getIdUsuarioProvedor())
                .status(request.getStatus() != null ? request.getStatus() : StatusUsuario.ATIVO)
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
        UsuarioEntity salvo = usuarioRepository.save(UsuarioMapper.toEntity(usuario));
        return UsuarioMapper.toDomain(salvo);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id)
                .map(UsuarioMapper::toDomain)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + id));
    }

    public Usuario atualizar(UUID id, UsuarioRequest request) {
        Usuario existente = buscarPorId(id);
        Usuario atualizado = existente
                .withNome(request.getNome())
                .withEmail(request.getEmail())
                .withTelefone(request.getTelefone())
                .withProvedorAutenticacao(request.getProvedorAutenticacao())
                .withIdUsuarioProvedor(request.getIdUsuarioProvedor())
                .withStatus(request.getStatus() != null ? request.getStatus() : existente.getStatus())
                .withDataAtualizacao(LocalDateTime.now());
        UsuarioEntity salvo = usuarioRepository.save(UsuarioMapper.toEntity(atualizado));
        return UsuarioMapper.toDomain(salvo);
    }

    public void deletar(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + id);
        }
        usuarioRepository.deleteById(id);
    }
}
