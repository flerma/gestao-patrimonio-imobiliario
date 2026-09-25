package com.techup.gestao_patrimonio_imobiliario.core.contrato;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Calcula a data sugerida da primeira parcela de aluguel de um contrato e a
 * competencia (mes de referencia) a que ela se refere.
 *
 * <p>Regra da data sugerida: soma-se 30 dias a data de inicio da vigencia;
 * a partir dessa data-base, a primeira parcela cai no proximo dia igual ao
 * dia de vencimento configurado (no mesmo mes da data-base, se o dia de
 * vencimento ainda nao tiver passado nesse mes; no mes seguinte, caso
 * contrario). Ex.: inicio 02/01, vencimento dia 10 -> base 01/02 -> primeira
 * parcela 10/02. Inicio 15/01, vencimento dia 10 -> base 14/02 -> primeira
 * parcela 10/03 (dia 10 de fevereiro ja tinha passado da data-base).
 *
 * <p>Regra da competencia: se a primeira parcela cai no mesmo mes do inicio
 * da vigencia, a competencia e esse mesmo mes; caso contrario, e o mes
 * anterior ao mes da propria parcela.
 */
public final class PrimeiraParcelaCalculator {

    private PrimeiraParcelaCalculator() {
    }

    public static LocalDate sugerir(LocalDate dataInicio, int diaVencimento) {
        LocalDate base = dataInicio.plusDays(30);
        YearMonth mesAlvo = base.getDayOfMonth() <= diaVencimento
                ? YearMonth.from(base)
                : YearMonth.from(base).plusMonths(1);
        return mesAlvo.atDay(Math.min(diaVencimento, mesAlvo.lengthOfMonth()));
    }

    public static YearMonth competencia(LocalDate dataInicio, LocalDate dataPrimeiraParcela) {
        YearMonth mesInicio = YearMonth.from(dataInicio);
        YearMonth mesParcela = YearMonth.from(dataPrimeiraParcela);
        return mesParcela.equals(mesInicio) ? mesInicio : mesParcela.minusMonths(1);
    }
}
