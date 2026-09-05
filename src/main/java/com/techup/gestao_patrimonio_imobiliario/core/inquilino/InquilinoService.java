package com.techup.gestao_patrimonio_imobiliario.core.inquilino;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.inquilino.InquilinoRequest;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusInquilino;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoJpaRepository;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoMapper;

@Service
@Transactional
public class InquilinoService {

    private final InquilinoJpaRepository inquilinoJpaRepository;

    public InquilinoService(InquilinoJpaRepository inquilinoJpaRepository) {
        this.inquilinoJpaRepository = inquilinoJpaRepository;
    }

    public Inquilino criar(InquilinoRequest request) {
        LocalDateTime agora = LocalDateTime.now();
        Inquilino inquilino = Inquilino.builder()
                .id(UUID.randomUUID())
                .tipoPessoa(request.getTipoPessoa())
                .nome(request.getNome())
                .documento(request.getDocumento())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .dataNascimento(request.getDataNascimento())
                .observacoes(request.getObservacoes())
                .status(request.getStatus() != null ? request.getStatus() : StatusInquilino.ATIVO)
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
        InquilinoEntity salvo = inquilinoJpaRepository.save(InquilinoMapper.toEntity(inquilino));
        return InquilinoMapper.toDomain(salvo);
    }

    @Transactional(readOnly = true)
    public List<Inquilino> listar() {
        return inquilinoJpaRepository.findAll().stream()
                .map(InquilinoMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Inquilino buscarPorId(UUID id) {
        return inquilinoJpaRepository.findById(id)
                .map(InquilinoMapper::toDomain)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquilino nao encontrado: " + id));
    }

    public Inquilino atualizar(UUID id, InquilinoRequest request) {
        Inquilino existente = buscarPorId(id);
        Inquilino atualizado = existente
                .withTipoPessoa(request.getTipoPessoa())
                .withNome(request.getNome())
                .withDocumento(request.getDocumento())
                .withEmail(request.getEmail())
                .withTelefone(request.getTelefone())
                .withDataNascimento(request.getDataNascimento())
                .withObservacoes(request.getObservacoes())
                .withStatus(request.getStatus() != null ? request.getStatus() : existente.getStatus())
                .withDataAtualizacao(LocalDateTime.now());
        InquilinoEntity salvo = inquilinoJpaRepository.save(InquilinoMapper.toEntity(atualizado));
        return InquilinoMapper.toDomain(salvo);
    }

    public void deletar(UUID id) {
        if (!inquilinoJpaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Inquilino nao encontrado: " + id);
        }
        inquilinoJpaRepository.deleteById(id);
    }
}
