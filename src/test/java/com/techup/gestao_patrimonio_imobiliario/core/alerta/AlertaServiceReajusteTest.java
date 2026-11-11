package com.techup.gestao_patrimonio_imobiliario.core.alerta;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.techup.gestao_patrimonio_imobiliario.core.alerta.AlertaService.Reajuste;
import com.techup.gestao_patrimonio_imobiliario.core.enums.IndiceReajuste;
import com.techup.gestao_patrimonio_imobiliario.data.contrato.ContratoEntity;

class AlertaServiceReajusteTest {

    private static ContratoEntity contrato(LocalDate inicio, LocalDate fim, Integer periodo, IndiceReajuste indice) {
        ContratoEntity c = new ContratoEntity();
        c.setDataInicio(inicio);
        c.setDataFim(fim);
        c.setPeriodoReajuste(periodo);
        c.setIndiceReajuste(indice);
        return c;
    }

    @Test
    void mesQueCompleta12_24_36MesesEhMesDeReajuste() {
        ContratoEntity c = contrato(LocalDate.of(2025, 1, 1), null, 12, IndiceReajuste.IGP_M);
        for (int anos = 1; anos <= 3; anos++) {
            Reajuste r = AlertaService.calcularReajuste(c, LocalDate.of(2025 + anos, 1, 20));
            assertTrue(r.noMesAtual());
            assertEquals(12L * anos, r.meses());
            assertEquals(LocalDate.of(2025 + anos, 1, 1), r.data());
        }
    }

    @Test
    void foraDoMesDeReajusteRetornaOProximo() {
        ContratoEntity c = contrato(LocalDate.of(2025, 1, 1), null, 12, IndiceReajuste.IGP_M);
        Reajuste r = AlertaService.calcularReajuste(c, LocalDate.of(2025, 12, 15));
        assertFalse(r.noMesAtual());
        assertEquals(LocalDate.of(2026, 1, 1), r.data());
        // mes de inicio nao e reajuste
        assertFalse(AlertaService.calcularReajuste(c, LocalDate.of(2025, 1, 10)).noMesAtual());
    }

    @Test
    void periodoDe6MesesEDiaLimitadoAoFimDoMes() {
        ContratoEntity c = contrato(LocalDate.of(2025, 8, 31), null, 6, null);
        Reajuste r = AlertaService.calcularReajuste(c, LocalDate.of(2026, 2, 3));
        assertTrue(r.noMesAtual());
        assertEquals(LocalDate.of(2026, 2, 28), r.data());
    }

    @Test
    void semReajusteSemPeriodoOuAposOFimRetornaNull() {
        LocalDate hoje = LocalDate.of(2026, 1, 5);
        assertNull(AlertaService.calcularReajuste(
                contrato(LocalDate.of(2025, 1, 1), null, 12, IndiceReajuste.SEM_REAJUSTE), hoje));
        assertNull(AlertaService.calcularReajuste(
                contrato(LocalDate.of(2025, 1, 1), null, null, IndiceReajuste.IGP_M), hoje));
        assertNull(AlertaService.calcularReajuste(
                contrato(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31), 12, IndiceReajuste.IGP_M), hoje));
    }
}
