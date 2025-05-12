package com.github.evandromedina.services.boleto;


import br.com.caelum.stella.boleto.*;
import br.com.caelum.stella.boleto.bancos.BancoDoBrasil;
import br.com.caelum.stella.boleto.bancos.Santander;
import br.com.caelum.stella.boleto.transformer.GeradorDeBoleto;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class BoletoService {

    @Autowired
    private FaturaRepository faturaRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private AgenciaRepository agenciaRepository;

    @Autowired
    private DadosRegistroBoleto dadosRegistroBoleto;

    @Autowired
    private DebitosRepository debitosRepository;

    @Autowired
    private GeradorQrCodeService geradorQrCodeService;

    @Autowired
    private AtualizaValorBoleto atualizaValorBoleto;

    @Autowired
    private AtualizaDataBoleto atualizaDataBoleto;

    @Autowired
    private FeriadosRepository feriadosRepository;

    @Autowired
    private ConsultaRegistroBoleto consultaRegistroBoleto;

    @Autowired
    private HistoricoRegistros historicoRegistrosistoricoRegistros;

    public byte[] gerarBoletoSantander(String calCodigo, Long codigoDebito) {

        /* BLOCO 1 - OBTENÇÃO DAS VARIÁVEIS */
        byte[] bPDF = null;
        Long idDebito = codigoDebito;
        short idDadosBanco = 5;

        Optional<EmpresaDTO> empresa = empresaRepository.buscarEmpresaBoletoSantander();
        Optional<AgenciaDTO> agencia = agenciaRepository.buscarAgenciaBoletoSantander(idDadosBanco);
        Optional<DadosBancoDTO> dadosBanco = DadosRegistroBoleto.buscarLocalDePagamentoEmdadosBancoAssociandoComAgencia(idDadosBanco);
        List<DebitosDTO> DebitosDTO = DebitosRepository.buscarDebitos(Long.parseLong(calCodigo));

        DebitosDTO registro = new DebitosDTO();

        for (DebitosDTO dadosDebito : DebitosDTO) {
            if (dadosDebito.getcodigoDebito().equals(idDebito)) {
                registro = dadosDebito;
                System.out.println(registro);
                break;
            }
        }

        // Dados Endereço Beneficiário
        String logradouroEmpresa = empresa.get().getEmpRua() + ", " + empresa.get().getEmpNumero();
        String bairroEmpresa = empresa.get().getEmpBairro();
        String cepEmpresa = empresa.get().getEmpCep().toString();
        String cidadeEmpresa = empresa.get().getEmpCidade();
        String ufEmpresa = empresa.get().getEmpUf();

        //Dados Beneficiário
        String nomeEmpresa = empresa.get().getFunNome();
        String cgcEmpresa = empresa.get().getEmpCgc();
        String agenciaSantander = String.valueOf(agencia.get().getAgeNumeroAgencia());
        String convenioSantander = registro.getCodigoConvenio();
        String carteira = "102";

        //Dados Endereço Pagador
        String enderecoPagador = registro.getRuaPagador() + " , " + registro.getNumeroPagador() + " , " + registro.getComplementoPagador();
        String bairroPagador = registro.getBairroPagador();
        String cepPagador = registro.getCepPagador();
        String cidadePagador = registro.getCidadePagador();
        String ufPagador = registro.getUfPagador();

        //Dados pessoais Pagador
        String nomePagador = registro.getNomePagador();
        String documentoPagador = registro.getCpfPagador();

        //Formatação do valor do boleto
        DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        decimalFormat.applyPattern("#,##0.00");
        String valorDoBoleto = decimalFormat.format(registro.getValor());

        String numeroDocumento = registro.getCodigoTitulo();
        String instrucao1 = "Msg 1";
        String instrucao2 = "Msg 2";
        String instrucao3 = "Msg 3";
        String locaisDePagamento = dadosBanco.get().getLocalPagamento();
        String nossoNumero = registro.getNossoNumero();
        String digitoNossoNumero = "";

        String nossoNumeroComDigito = nossoNumero;

        //Separa o último dígito do nosso número
        if (nossoNumero.length() == 13) {
            digitoNossoNumero = nossoNumero.substring(12, 13);
            nossoNumero = nossoNumero.substring(0, 12);
        }

        /* BLOCO 2 - INSERÇÃO DOS DADOS PARA MONTAGEM DOS BOLETOS */
        LocalDateTime dataVencimento = registro.getDatData();
        LocalDateTime dataVencimentoComHoraZerada = dataVencimento.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime hoje = LocalDateTime.now();
        LocalDateTime dataEmissao = registro.getDataEmissao();
        BigDecimal valorOriginal = registro.getValor();

        //Utiliza métodos da lib stella-boleto para montagem do boleto
        Endereco enderecoBeneficiario = Endereco.novoEndereco()
                .comLogradouro(logradouroEmpresa)
                .comBairro(bairroEmpresa)
                .comCep(cepEmpresa)
                .comCidade(cidadeEmpresa)
                .comUf(ufEmpresa);

        Beneficiario beneficiario = Beneficiario.novoBeneficiario()
                .comNomeBeneficiario(nomeEmpresa)
                .comDocumento(cgcEmpresa)
                .comEndereco(enderecoBeneficiario)
                .comAgencia(agenciaSantander)
                .comCodigoBeneficiario(convenioSantander)
                .comCarteira(carteira)
                .comNossoNumero(nossoNumero)
                .comDigitoNossoNumero(digitoNossoNumero);

        Endereco enderecoCompletoPagador = Endereco.novoEndereco()
                .comLogradouro(enderecoPagador)
                .comBairro(bairroPagador)
                .comCep(cepPagador)
                .comCidade(cidadePagador)
                .comUf(ufPagador);

        Pagador pagador = Pagador.novoPagador()
                .comNome(nomePagador)
                .comDocumento(documentoPagador)
                .comEndereco(enderecoCompletoPagador);

        Banco banco = new Santander();

        /* SE BOLETO VENCIDO*/
        if (hoje.isAfter(dataVencimentoComHoraZerada)) {
            BigDecimal valorCorrigido = atualizaValorBoleto.calcularAcerto(valorOriginal, dataVencimentoComHoraZerada);

            LocalDateTime dataVencimentoAjustada = obterNovaDataVencimento(dataVencimentoComHoraZerada, atualizaDataBoleto);

            Datas datasBoletoVencido = Datas.novasDatas()
                    .comDocumento(convertToCalendar(dataEmissao))
                    .comProcessamento(Calendar.getInstance())
                    .comVencimento(convertToCalendar(dataVencimentoAjustada));

            // Atualiza o valor do boleto com juros, multa e correção
            Boleto boletoAtualizado = Boleto.novoBoleto()
                    .comBanco(banco)
                    .comDatas(datasBoletoVencido)
                    .comBeneficiario(beneficiario)
                    .comPagador(pagador)
                    .comValorBoleto(valorCorrigido)
                    .comNumeroDoDocumento(numeroDocumento)
                    .comInstrucoes(instrucao1, instrucao2, instrucao3)
                    .comLocaisDePagamento(locaisDePagamento);

            try {

                String qrCodePix = String.valueOf(registro.getQrCodePix());
                byte[] qrCodeImage = geradorQrCodeService.gerar(qrCodePix).readAllBytes();

                GeradorDeBoleto gerador = new GeradorDeBoleto(boletoAtualizado);
                gerador.geraPDF("Santander.pdf");
                bPDF = gerador.geraPDF();

                //Utilização da Lib Apache PDFBox para inserir o QRCode no boleto pdf
                try (PDDocument document = PDDocument.load(bPDF)) {
                    PDPage page = document.getPage(1);
                    PDImageXObject pdImage = PDImageXObject.createFromByteArray(document, qrCodeImage, "QRCode");
                    PDPageContentStream contentStream = new PDPageContentStream(document, page, PDPageContentStream.AppendMode.APPEND, true, true);

                    //Posição e tamanho do QRCode
                    float largura = 115;
                    float altura = 115;
                    float posicaoX = 280;
                    float posicaoY = 280;

                    contentStream.drawImage(pdImage, posicaoX, posicaoY, largura, altura);
                    contentStream.close();

                    //Salvando o PDF
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    document.save(outputStream);
                    bPDF = outputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // Remover a primeira página em branco, se necessário
                try (PDDocument document = PDDocument.load(bPDF)) {
                    if (document.getNumberOfPages() > 1) {
                        document.removePage(0);
                    }
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    document.save(outputStream);
                    bPDF = outputStream.toByteArray();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Erro na geração do boleto Santander");
                return null;
            }
        } else {
            /*BOLETO A VENCER*/

            // Implementar a mesma lógica sem aplicar correcão do valor e data de vencimento...
        }
        return bPDF;
    }

    public LocalDateTime obterNovaDataVencimento(LocalDateTime dataOriginal, AtualizaDataBoleto atualizaDataBoleto) {
        int ano = dataOriginal.getYear();
        Optional<Integer> optCodigo = feriadosRepository.buscarCodigoCalendarioPorAno(ano);

        if (optCodigo.isPresent() && dataOriginal.isBefore(LocalDateTime.now())) {
            return atualizaDataBoleto.ajustarParaProximoDiaUtil(LocalDateTime.now(), optCodigo.get());
        }

        if (optCodigo.isPresent()) {
            return atualizaDataBoleto.ajustarParaProximoDiaUtil(dataOriginal, optCodigo.get());
        }

        throw new IllegalStateException("Código de calendário de feriados não encontrado para o ano: " + ano);
    }

    private Calendar convertToCalendar(LocalDateTime localDateTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(localDateTime.getYear(), localDateTime.getMonthValue() - 1, localDateTime.getDayOfMonth(),
                localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
        calendar.set(Calendar.MILLISECOND, localDateTime.getNano() / 1_000_000);
        return calendar;
    }

}
