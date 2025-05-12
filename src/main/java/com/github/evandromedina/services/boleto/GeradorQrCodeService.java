package com.github.evandromedina.services.boleto;

import java.io.InputStream;

public interface GeradorQrCodeService {

    InputStream gerar(String codigo); //codigo emv (ccrQrCodePix) do QrCode que estáno banco
}
