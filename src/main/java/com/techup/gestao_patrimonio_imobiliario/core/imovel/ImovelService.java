package com.techup.gestao_patrimonio_imobiliario.core.imovel;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.imovel.ImovelRequest;
import com.techup.gestao_patrimonio_imobiliario.core.auth.AutenticacaoAtual;
import com.techup.gestao_patrimonio_imobiliario.data.endereco.EnderecoMapper;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelEntity;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelRepository;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelMapper;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioRepository;

@Service
@Transactional
public class ImovelService {

    private final ImovelRepository imovelRepository;
    private final UsuarioRepository usuarioRepository;

    public ImovelService(ImovelRepository imovelRepository, UsuarioRepository usuarioRepository) {
        this.imovelRepository = imovelRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Imovel criar(ImovelRequest request) {
        UsuarioEntity usuarioEntity = buscarUsuarioEntity(AutenticacaoAtual.usuarioId());
        LocalDateTime agora = LocalDateTime.now();
        ImovelEntity entity = ImovelEntity.builder()
                .id(UUID.randomUUID())
                .usuario(usuarioEntity)
                .nome(request.getNome())
                .tipo(request.getTipo())
                .status(request.getStatus())
                .valorAquisicao(request.getValorAquisicao())
                .dataAquisicao(request.getDataAquisicao())
                .valorAtual(request.getValorAtual())
                .endereco(EnderecoMapper.toEmbeddable(request.getEndereco()))
                .dataCriacao(agora)
                .dataAtualizacao(agora)
                .build();
        return ImovelMapper.toDomain(imovelRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<Imovel> listar() {
        return imovelRepository.findAllByUsuarioId(AutenticacaoAtual.usuarioId()).stream()
                .map(ImovelMapper::toDomain)
                .toList();
    }

    @Transactional(readOnly = true)
    public Imovel buscarPorId(UUID id) {
        return ImovelMapper.toDomain(buscarImovelEntity(id));
    }

    public Imovel atualizar(UUID id, ImovelRequest request) {
        ImovelEntity existente = buscarImovelEntity(id);
        existente.setNome(request.getNome());
        existente.setTipo(request.getTipo());
        existente.setStatus(request.getStatus());
        existente.setValorAquisicao(request.getValorAquisicao());
        existente.setDataAquisicao(request.getDataAquisicao());
        existente.setValorAtual(request.getValorAtual());
        existente.setEndereco(EnderecoMapper.toEmbeddable(request.getEndereco()));
        existente.setDataAtualizacao(LocalDateTime.now());
        return ImovelMapper.toDomain(imovelRepository.save(existente));
    }

    public void deletar(UUID id) {
        imovelRepository.delete(buscarImovelEntity(id));
    }

    /** Busca o imovel garantindo que pertence ao usuario autenticado (404 caso contrario). */
    private ImovelEntity buscarImovelEntity(UUID id) {
        return imovelRepository.findByIdAndUsuarioId(id, AutenticacaoAtual.usuarioId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imovel nao encontrado: " + id));
    }

    private UsuarioEntity buscarUsuarioEntity(UUID usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + usuarioId));
    }
}
