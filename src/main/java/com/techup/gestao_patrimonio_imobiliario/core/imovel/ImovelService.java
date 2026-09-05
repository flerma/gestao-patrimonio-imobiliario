package com.techup.gestao_patrimonio_imobiliario.core.imovel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.imovel.ImovelRequest;
import com.techup.gestao_patrimonio_imobiliario.data.endereco.EnderecoMapper;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelEntity;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelJpaRepository;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelMapper;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioJpaRepository;

@Service
@Transactional
public class ImovelService {

    private final ImovelJpaRepository imovelJpaRepository;
    private final UsuarioJpaRepository usuarioJpaRepository;

    public ImovelService(ImovelJpaRepository imovelJpaRepository, UsuarioJpaRepository usuarioJpaRepository) {
        this.imovelJpaRepository = imovelJpaRepository;
        this.usuarioJpaRepository = usuarioJpaRepository;
    }

    public Imovel criar(ImovelRequest request) {
        UsuarioEntity usuarioEntity = buscarUsuarioEntity(request.getUsuarioId());
        LocalDateTime agora = LocalDateTime.now();
        ImovelEntity entity = ImovelEntity.builder()
                .id(UUID.randomUUID())
                .usuario(usuarioEntity)
                .nome(request.getNome())
                .tipo(request.getTipo())
                .status(request.getStatus())
                .valorAquisicao(request.getValorAquisicao())
                .valorAtual(request.getValorAtual())
                .endereco(EnderecoMapper.toEmbeddable(request.getEndereco()))
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
        return ImovelMapper.toDomain(imovelJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<Imovel> listar() {
        return imovelJpaRepository.findAll().stream()
                .map(ImovelMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Imovel buscarPorId(UUID id) {
        return ImovelMapper.toDomain(buscarImovelEntity(id));
    }

    public Imovel atualizar(UUID id, ImovelRequest request) {
        ImovelEntity existente = buscarImovelEntity(id);
        UsuarioEntity usuarioEntity = buscarUsuarioEntity(request.getUsuarioId());
        existente.setUsuario(usuarioEntity);
        existente.setNome(request.getNome());
        existente.setTipo(request.getTipo());
        existente.setStatus(request.getStatus());
        existente.setValorAquisicao(request.getValorAquisicao());
        existente.setValorAtual(request.getValorAtual());
        existente.setEndereco(EnderecoMapper.toEmbeddable(request.getEndereco()));
        existente.setDataAtualizacao(LocalDateTime.now());
        return ImovelMapper.toDomain(imovelJpaRepository.save(existente));
    }

    public void deletar(UUID id) {
        if (!imovelJpaRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado: " + id);
        }
        imovelJpaRepository.deleteById(id);
    }

    private ImovelEntity buscarImovelEntity(UUID id) {
        return imovelJpaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado: " + id));
    }

    private UsuarioEntity buscarUsuarioEntity(UUID usuarioId) {
        return usuarioJpaRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + usuarioId));
    }
}
