# Validação da entrega

Data: 17/11/2026.

- Compilação Android: `assembleDebug`, concluída com sucesso.
- Testes: `testDebugUnitTest`, 22 executados, zero falhas, erros ou ignorados.
- JUnit: PricingTest (6), BookingRulesTest (6), ShakeDetectorTest (5) e EventSearchTest (5).
- APK de desenvolvimento assinado e assinatura verificada com apksigner. Android mínimo 8.0/API 26.
- Slides PPTX: 6 páginas com notas de apresentação, exportação e validação estrutural concluídas.

## Verificação ainda necessária no aparelho

A interface, os tiles do mapa e a leitura física do sensor não foram executados em aparelho ou emulador neste ambiente. Abra o APK e confira: busca, favorito, reserva, reabertura, QR Code, check-in, mapa com internet e movimento na aba Explorar. Os testes unitários validam as regras, sem comprovar integração de hardware.

## Limites

Agenda fictícia. Reserva e estoque apenas locais. Sem pagamento, login remoto ou validade dos QR Codes em eventos reais. O mapa exige rede para tiles novos. Acelerômetro depende do hardware. Não é um aplicativo conectado à produção da Badgr.
