package com.github.evandromedina.services.atualiza_boleto;

/*
* Classe responsável por atualizar a data de vencimento do boleto vencido com base em tabela de feriados no Banco de Dados
*/

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class AtualizaDataBoleto {

    @Autowired
    private FeriadosRepository feriadosRepository;

    public LocalDateTime ajustarParaProximoDiaUtil(LocalDateTime dataHoje, Integer codigoCalendario) {
        List<LocalDate> feriados = feriadosRepository.buscarFeriadosPorCodigoCalendario(codigoCalendario)
                .orElse(Collections.emptyList())
                .stream()
                .map(FeriadosDTO::getDatData)
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toList());

        System.out.println("Feriados encontrados: ");
        feriados.forEach(System.out::println);
        LocalDate vencimento = dataHoje.toLocalDate().plusDays(1);

        while (isFeriadoOuFinalDeSemana(vencimento, feriados)) {
            vencimento = vencimento.plusDays(1);
        }

        return vencimento.atTime(23, 59);
    }

    public boolean isFeriadoOuFinalDeSemana(LocalDate data, List<LocalDate> feriados) {
        DayOfWeek diaSemana = data.getDayOfWeek();
        return feriados.contains(data)
                || diaSemana == DayOfWeek.SATURDAY
                || diaSemana == DayOfWeek.SUNDAY;
    }
}