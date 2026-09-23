package com.techup.gestao_patrimonio_imobiliario.core.inquilino;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.inquilino.InquilinoRequest;
import com.techup.gestao_patrimonio_imobiliario.core.auth.AutenticacaoAtual;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusInquilino;
import com.techup.gestao_patrimonio_imobiliario.data.endereco.EnderecoMapper;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoRepository;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoMapper;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioRepository;

@Service
@Transactional
public class InquilinoService {

    private final InquilinoRepository inquilinoRepository;
    private final UsuarioRepository usuarioRepository;

    public InquilinoService(InquilinoRepository inquilinoRepository, UsuarioRepository usuarioRepository) {
        this.inquilinoRepository = inquilinoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Inquilino criar(InquilinoRequest request) {
        UsuarioEntity usuarioEntity = buscarUsuarioEntity(AutenticacaoAtual.usuarioId());
        if (inquilinoRepository.existsByUsuarioIdAndDocumento(usuarioEntity.getId(), request.getDocumento())) {
            throw new DocumentoJaCadastradoException(
                    "Já existe um inquilino cadastrado com este CPF/CNPJ");
        }
        LocalDateTime agora = LocalDateTime.now();
        InquilinoEntity entity = InquilinoEntity.builder()
                .id(UUID.randomUUID())
                .usuario(usuarioEntity)
                .tipoPessoa(request.getTipoPessoa())
                .nome(request.getNome())
                .documento(request.getDocumento())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .dataNascimento(request.getDataNascimento())
                .endereco(EnderecoMapper.toEmbeddable(request.getEndereco()))
                .observacoes(request.getObservacoes())
                .status(request.getStatus() != null ? request.getStatus() : StatusInquilino.ATIVO)
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
        return InquilinoMapper.toDomain(inquilinoRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<Inquilino> listar() {
        return inquilinoRepository.findAllByUsuarioId(AutenticacaoAtual.usuarioId()).stream()
                .map(InquilinoMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Inquilino buscarPorId(UUID id) {
        return InquilinoMapper.toDomain(buscarInquilinoEntity(id));
    }

    public Inquilino atualizar(UUID id, InquilinoRequest request) {
        InquilinoEntity existente = buscarInquilinoEntity(id);
        if (inquilinoRepository.existsByUsuarioIdAndDocumentoAndIdNot(
                existente.getUsuario().getId(), request.getDocumento(), id)) {
            throw new DocumentoJaCadastradoException(
                    "Já existe um inquilino cadastrado com este CPF/CNPJ");
        }
        existente.setTipoPessoa(request.getTipoPessoa());
        existente.setNome(request.getNome());
        existente.setDocumento(request.getDocumento());
        existente.setEmail(request.getEmail());
        existente.setTelefone(request.getTelefone());
        existente.setDataNascimento(request.getDataNascimento());
        existente.setEndereco(EnderecoMapper.toEmbeddable(request.getEndereco()));
        existente.setObservacoes(request.getObservacoes());
        existente.setStatus(request.getStatus() != null ? request.getStatus() : existente.getStatus());
        existente.setDataAtualizacao(LocalDateTime.now());
        return InquilinoMapper.toDomain(inquilinoRepository.save(existente));
    }

    public void deletar(UUID id) {
        inquilinoRepository.delete(buscarInquilinoEntity(id));
    }

    /** Busca o inquilino garantindo que pertence ao usuario autenticado (404 caso contrario). */
    private InquilinoEntity buscarInquilinoEntity(UUID id) {
        return inquilinoRepository.findByIdAndUsuarioId(id, AutenticacaoAtual.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquilino nao encontrado: " + id));
    }

    private UsuarioEntity buscarUsuarioEntity(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + usuarioId));
    }
}
