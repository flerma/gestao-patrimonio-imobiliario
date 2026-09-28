package com.techup.gestao_patrimonio_imobiliario.api.imovel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.techup.gestao_patrimonio_imobiliario.api.usuario.UsuarioResponse;
import com.techup.gestao_patrimonio_imobiliario.core.endereco.Endereco;
import com.techup.gestao_patrimonio_imobiliario.core.enums.StatusImovel;
import com.techup.gestao_patrimonio_imobiliario.core.enums.TipoImovel;
import com.techup.gestao_patrimonio_imobiliario.core.imovel.Imovel;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class ImovelResponse {

    UUID id;
    UsuarioResponse usuario;
    String nome;
    TipoImovel tipo;
    StatusImovel status;
    BigDecimal valorAquisicao;
    LocalDate dataAquisicao;
    BigDecimal valorAtual;
    Endereco endereco;
    LocalDateTime dataCriacao;
    LocalDateTime dataAtualizacao;
    /** Soma do valor pago em todas as parcelas de aluguel de todos os contratos do imovel. Nula quando nao calculada (ex.: listagem). */
    BigDecimal totalAlugueisPagos;

    public static ImovelResponse from(Imovel imovel) {
        if (imovel == null) {
            return null;
        }
        return ImovelResponse.builder()
                .id(imovel.getId())
                .usuario(UsuarioResponse.from(imovel.getUsuario()))
                .nome(imovel.getNome())
                .tipo(imovel.getTipo())
                .status(imovel.getStatus())
                .valorAquisicao(imovel.getValorAquisicao())
                .dataAquisicao(imovel.getDataAquisicao())
                .valorAtual(imovel.getValorAtual())
                .endereco(imovel.getEndereco())
                .dataCriacao(imovel.getDataCriacao())
                .dataAtualizacao(imovel.getDataAtualizacao())
                .totalAlugueisPagos(imovel.getTotalAlugueisPagos())
                .build();
    }
}
