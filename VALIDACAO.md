# Validação

Data: 17/11/2026. Banco Room versão 1, arquivo `badgr.db`.

- `assembleDebug`: compilação concluída com sucesso.
- `testDebugUnitTest`: 22 testes unitários com assertEquals, zero falhas ou erros.
- Room gerou as implementações dos três DAOs e do banco e verificou as consultas na compilação.
- Esquema exportado em `app/schemas/`.

## Verificação no emulador

A interface, o mapa, o acelerômetro e a abertura do banco pelo Room ainda precisam de conferência no Android. Os testes JUnit são de regras de negócio, não de UI, hardware ou persistência instrumentada.

No Android Studio: Git > Pull, Gradle Sync, selecionar emulador e Run. O arquivo de banco é `badgr.db`, independente do banco utilizado nas compilações anteriores. As reservas do outro arquivo não são importadas.

Agenda fictícia, reservas locais, QR de demonstração e check-in manual. Sem pagamentos ou sincronização com a Badgr em produção.
