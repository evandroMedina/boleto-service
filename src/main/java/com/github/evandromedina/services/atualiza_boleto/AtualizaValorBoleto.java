package com.github.evandromedina.services.atualiza_boleto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/*
 * Classe responsável por atualizar o valor a pagar do boleto vencido
 */

@Service
public class AtualizaValorBoleto {

    @Autowired
    private IndicesAcertosRepository indicesAcertosRepository;

    public BigDecimal calcularAcerto(BigDecimal valorOriginal, LocalDateTime vencimento) {
        LocalDateTime hoje = LocalDateTime.now();
        long diasAtraso = ChronoUnit.DAYS.between(vencimento, hoje);
        long mesesAtraso = ChronoUnit.MONTHS.between(vencimento.withDayOfMonth(1), hoje.withDayOfMonth(1));

        BigDecimal indiceMulta = BigDecimal.valueOf(2); // 2% ao mês
        BigDecimal indiceJuros = BigDecimal.valueOf(1); // 1% ao mês
        BigDecimal inpc = BigDecimal.ONE;

        if (diasAtraso > 30) {
            inpc = BigDecimal.valueOf(getInpc((short) vencimento.getYear(), (short) vencimento.getMonthValue()));
        }

        BigDecimal fatorMulta = BigDecimal.valueOf(diasAtraso)
                .multiply(indiceMulta)
                .divide(BigDecimal.valueOf(30), 10, RoundingMode.HALF_UP);
        if (fatorMulta.compareTo(indiceMulta) > 0) {
            fatorMulta = indiceMulta;
        }

        BigDecimal valorCorrigido = valorOriginal.multiply(inpc);
        BigDecimal juros = valorCorrigido
                .multiply(indiceJuros)
                .multiply(BigDecimal.valueOf(mesesAtraso))
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal multa = valorCorrigido
                .multiply(fatorMulta)
                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal correcao = valorCorrigido.subtract(valorOriginal);

        return valorOriginal.add(juros).add(multa).add(correcao).setScale(2, RoundingMode.HALF_UP);
    }

    public Double getInpc(Short ano, Short mes) {
        return indicesAcertosRepository.buscarIndicesAcertos(ano, mes)
                .map(IndicesAcertosDTO::getIndIndice)
                .orElse(1.0);
    }
}


