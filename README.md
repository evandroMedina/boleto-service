# Boleto Service

O projeto **Boleto Service** é um módulo que permite gerar um boleto bancário — neste caso, para o **Banco Santander** — contendo código de barras e QRCode Pix. O **Boleto Service** utiliza a biblioteca open source [stella-boleto](https://github.com/caelum/stella) da **Caelum-Stella**, que oferece suporte para todo o processo base de construção do boleto.

Embora a biblioteca resolva a maior e mais complexa parte do trabalho, com todos os detalhes e regras específicas de cada banco, implementar esse tipo de serviço ainda é um grande desafio. São inúmeros os fatores que dificultam o processo, e esse é justamente o motivo de eu estar compartilhando o resultado: **ajudar outros desenvolvedores que possam enfrentar os mesmos problemas que enfrentei**.

Além disso, considerando que ainda existem poucas bibliotecas open source no Brasil sendo ativamente mantidas e atualizadas, este projeto é também uma tentativa de **colaborar, compartilhar conhecimento e reduzir o tempo de trabalho**.

Se você precisa emitir boletos de forma gratuita, esse projeto pode ser útil!

## 📌 Objetivos

- Gerar boletos bancários para o **Banco Santander**.
- Incluir **código de barras** e **QRCode Pix** no boleto.
- Utilizar a biblioteca open source **stella-boleto** para facilitar o processo.

## ⚙️ Tecnologias Utilizadas

- **Java**
- **stella-boleto** (biblioteca para geração de boletos bancários)
- **QRCode Pix**


