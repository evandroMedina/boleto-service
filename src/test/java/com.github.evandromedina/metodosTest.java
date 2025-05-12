package com.github.evandromedina;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DataJpaTest
public class ClassesTestes {

    @Autowired
    private AtualizaDataBoleto atualizaDataBoleto;

    @Autowired
    private BoletoService boletoService;

    @Autowired
    private GeradorQrCodeService geradorQrCodeService;

    @Autowired
    private AtualizaValorBoleto atualizaValorBoleto;


    @Test
    void deveAtualizarDataSeForAnteriorECalendarioExistir() {

        LocalDateTime dataOriginal = LocalDateTime.now().minusDays(3); // 26/04/2025

        //Deve retornar a data atualizada ou data original
        LocalDateTime novaData = boletoService.obterNovaDataVencimento(dataOriginal, atualizaDataBoleto);

        assertNotEquals(dataOriginal, novaData, "A data deveria ter sido ajustada para próximo dia útil");
    }



    @Test
    void testIsFeriadoOuFinalDeSemana() {
        // Lista de feriados
        List<LocalDate> feriados = Arrays.asList(
                LocalDate.of(2025, 12, 25), // Natal
                LocalDate.of(2025, 1, 1)   // Ano Novo
        );

        // Teste para um feriado
        LocalDate feriado = LocalDate.of(2025, 12, 25);
        assertTrue(atualizaDataBoleto.isFeriadoOuFinalDeSemana(feriado, feriados));

        // Teste para um sábado
        LocalDate sabado = LocalDate.of(2025, 12, 27);
        assertTrue(atualizaDataBoleto.isFeriadoOuFinalDeSemana(sabado, feriados));

        // Teste para um domingo
        LocalDate domingo = LocalDate.of(2025, 12, 28);
        assertTrue(atualizaDataBoleto.isFeriadoOuFinalDeSemana(domingo, feriados));

        // Teste para um dia útil que não é feriado
        LocalDate diaUtil = LocalDate.of(2025, 12, 26);
        assertFalse(atualizaDataBoleto.isFeriadoOuFinalDeSemana(diaUtil, feriados));

        // Teste para um dia que não está na lista de feriados
        LocalDate naoFeriado = LocalDate.of(2025, 12, 29);
        assertFalse(atualizaDataBoleto.isFeriadoOuFinalDeSemana(naoFeriado, feriados));
    }


}
