# Validação — Room + DAO

Data: 17/11/2026. App 2.0-room-demo, versionCode 2.

- `assembleDebug`: compilação concluída com sucesso.
- `testDebugUnitTest`: 22 testes, zero falhas ou erros.
- Room 2.7.2/KAPT gerou EventDao_Impl, FavoriteDao_Impl, TicketDao_Impl e BadgrDatabase_Impl. Consultas dos DAOs verificadas na compilação.
- Esquema Room versão 2 exportado em app/schemas/.
- Migração 1 para 2 exercitada com SQLite em memória no host: dados de evento, favorito e ingresso preservados, chaves estrangeiras sem violações e colunas/FKs comparadas com o esquema gerado pelo Room. Isso não é teste instrumentado Android.

## Limites da verificação

A interface, o mapa, o acelerômetro e a abertura/migração pelo Room no Android não foram executados em um emulador neste ambiente. Conferir no emulador antes de apresentar. Os testes JUnit são de regras de negócio. A conferência no host não substitui teste instrumentado de persistência Android.

## Execução

No Android Studio: Git > Pull, Gradle Sync, selecionar o emulador e Run. Se abrir pela primeira vez, clone o repositório e abra sua raiz.

## Limites funcionais

Agenda fictícia, reserva e estoque locais, sem pagamento ou sincronização. QR de demonstração. Check-in manual, sem câmera. Mapa usa rede para tiles novos.
