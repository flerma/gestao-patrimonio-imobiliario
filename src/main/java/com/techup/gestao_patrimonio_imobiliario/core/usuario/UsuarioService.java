package com.techup.gestao_patrimonio_imobiliario.core.usuario;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.techup.gestao_patrimonio_imobiliario.api.usuario.UsuarioRequest;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusUsuario;
import com.techup.gestao_patrimonio_imobiliario.data.auth.RefreshTokenRepository;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoEntity;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoRepository;
import com.techup.gestao_patrimonio_imobiliario.data.imovel.ImovelRepository;
import com.techup.gestao_patrimonio_imobiliario.data.inquilino.InquilinoRepository;
import com.techup.gestao_patrimonio_imobiliario.data.notificacao.DispositivoPushRepository;
import com.techup.gestao_patrimonio_imobiliario.data.pagamentoaluguel.PagamentoAluguelRepository;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioEntity;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioRepository;
import com.techup.gestao_patrimonio_imobiliario.data.usuario.UsuarioMapper;

@Service
@Transactional
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final DispositivoPushRepository dispositivoPushRepository;
    private final ImovelRepository imovelRepository;
    private final InquilinoRepository inquilinoRepository;
    private final ContratoRepository contratoRepository;
    private final PagamentoAluguelRepository pagamentoAluguelRepository;

    public UsuarioService(UsuarioRepository usuarioRepository,
                          RefreshTokenRepository refreshTokenRepository,
                          DispositivoPushRepository dispositivoPushRepository,
                          ImovelRepository imovelRepository,
                          InquilinoRepository inquilinoRepository,
                          ContratoRepository contratoRepository,
                          PagamentoAluguelRepository pagamentoAluguelRepository) {
        this.usuarioRepository = usuarioRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.dispositivoPushRepository = dispositivoPushRepository;
        this.imovelRepository = imovelRepository;
        this.inquilinoRepository = inquilinoRepository;
        this.contratoRepository = contratoRepository;
        this.pagamentoAluguelRepository = pagamentoAluguelRepository;
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
                .role(request.getRole())
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
                .withRole(request.getRole())
                .withDataAtualizacao(LocalDateTime.now());
        UsuarioEntity salvo = usuarioRepository.save(UsuarioMapper.toEntity(atualizado));
        return UsuarioMapper.toDomain(salvo);
    }

    public void deletar(UUID id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado: " + id);
        }
        excluirDadosDoUsuario(id);
        usuarioRepository.deleteById(id);
    }

    /**
     * Exclusao em cascata de tudo o que pertence ao usuario, na ordem exigida
     * pelas chaves estrangeiras: alugueis (pagamentos) -> contratos -> imoveis
     * e inquilinos -> sessoes (refresh tokens) e aparelhos de push. Tudo na
     * mesma transacao do deletar: se algo falhar, nada e apagado.
     */
    private void excluirDadosDoUsuario(UUID usuarioId) {
        // Contratos dos imoveis do usuario e, por garantia, os dos inquilinos
        // dele (normalmente o mesmo conjunto, ja que os dados sao isolados por usuario).
        Set<ContratoEntity> contratos = new LinkedHashSet<>(contratoRepository.findAllByImovelUsuarioId(usuarioId));
        contratos.addAll(contratoRepository.findAllByInquilinoUsuarioId(usuarioId));
        contratos.forEach(contrato -> pagamentoAluguelRepository.deleteByContratoId(contrato.getId()));
        pagamentoAluguelRepository.flush();
        contratoRepository.deleteAll(contratos);
        contratoRepository.flush();

        imovelRepository.deleteAll(imovelRepository.findAllByUsuarioId(usuarioId));
        inquilinoRepository.deleteAll(inquilinoRepository.findAllByUsuarioId(usuarioId));
        refreshTokenRepository.deleteByUsuarioId(usuarioId);
        dispositivoPushRepository.deleteByUsuarioId(usuarioId);
        imovelRepository.flush();
    }
}
