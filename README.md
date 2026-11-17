# Badgr Mobile — N2 de Desenvolvimento Mobile II

Aplicativo Android nativo em Kotlin inspirado na Badgr. Agenda fictícia, dados locais e nenhuma integração com o ambiente real. Não cobra pagamentos. QR Codes não valem em eventos reais.

## Instalar o APK que acompanha a entrega

No Android, abra `Badgr-Mobile.apk` e instale. Se o sistema pedir, autorize a instalação pelo aplicativo usado para abrir o arquivo. Requer Android 8.0 ou superior. Para recompilar ou estudar o código, siga as etapas abaixo.

## Abrir e executar

1. Extraia o projeto e abra a pasta `badgr-mobile` no Android Studio.
2. Use o JDK embarcado no Android Studio (17 ou 21). Instale o SDK Android 35 quando solicitado.
3. Aguarde a sincronização do Gradle. A primeira compilação precisa de internet.
4. Selecione um Android 8.0/API 26 ou superior e clique em Run.
5. Para testar o movimento, prefira um celular físico com acelerômetro. No emulador, use os controles de sensores.

Comandos no terminal do projeto:

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

No Windows use `gradlew.bat`. APK gerado: `app/build/outputs/apk/debug/app-debug.apk`.

## Roteiro do código

Leia [ROTEIRO-APRESENTACAO-CODIGO.md](ROTEIRO-APRESENTACAO-CODIGO.md) para localizar cada arquivo, estudar o fluxo da reserva e responder às perguntas sobre persistência, MVVM, sensor, mapas e testes.

## O que demonstrar em 5 minutos

1. Abra Explorar. Mostre busca por `umbra`, filtros e favorito.
2. Abra o evento, confira ingresso, taxa e total. Informe um nome e reserve.
3. Em Ingressos, mostre QR Code, compartilhamento e simulação de check-in.
4. Feche e reabra o aplicativo. Mostre a reserva e o favorito persistidos.
5. Abra Mapa. Toque em um marcador para abrir o evento. Use internet para os tiles.
6. Volte a Explorar e agite o aparelho. A leitura em g fica visível. Mostre o evento sugerido.
7. Explique MVVM e abra os testes.

## Requisitos e código

| Requisito do PDF | Implementação | Arquivo |
|---|---|---|
| Android/Kotlin | App nativo, API mínima 26 | `MainActivity.kt`, `app/build.gradle.kts` |
| MVVM | View observa LiveData e envia ações ao ViewModel | `MainActivity.kt`, `BadgrViewModel.kt` |
| Banco | SQLite, 3 tabelas, FK, índice e transações | `BadgrDatabase.kt`, `EventRepository.kt` |
| Sensor | Acelerômetro pelo SensorManager, sugestão por movimento | `MainActivity.kt`, `ShakeDetector` em `Domain.kt` |
| Mapas | MapView osmdroid e marcadores OpenStreetMap | `mapScreen` em `MainActivity.kt` |
| Testes unitários | JUnit 4, regras sem dependência de Activity | `DomainTest.kt` |
| Fonte completo | Projeto Gradle e wrapper | esta pasta |
| Apresentação | Slides com notas de fala | arquivo PPTX que acompanha o projeto |

## Arquitetura

- **View:** `MainActivity` cria Views nativas, observa estado e trata o ciclo de vida. Não executa SQL.
- **ViewModel:** `BadgrViewModel` concentra as ações, estado, carregamento e tratamento de erros. Sobrevive à rotação e não mantém referências a Views.
- **Model:** modelos e regras em `Domain.kt`, repositório em `EventRepository.kt` e banco em `BadgrDatabase.kt`.
- Consultas e escritas usam `Dispatchers.IO`. O resultado volta para a thread principal pelo `viewModelScope`.
- `busy` impede duplo clique durante reserva. Uma transação verifica disponibilidade e insere o ingresso de forma atômica no banco local.
- O ViewModel retém a navegação em rotações. `onSaveInstanceState` preserva tela e filtros após recriação pelo Android. Rascunhos de nome usam preferências locais.

## Banco

`events` guarda a agenda fictícia pré-carregada na criação. `favorites` usa `event_id` como chave primária para evitar duplicatas. `tickets` guarda UUID, evento, titular, total em centavos, timestamp e status de check-in. Chaves estrangeiras ficam habilitadas. O índice em `tickets.event_id` atende a contagem por evento.

O check-in atualiza apenas registros com `checked_in=0`. Esta é uma simulação manual local, não uma leitura de câmera nem uma validação de ingressos reais. A reserva reduz a disponibilidade pela contagem de tickets do evento. O estoque não é compartilhado entre aparelhos.

## Sensor

A magnitude é `sqrt(x²+y²+z²)/9,80665`. O limiar de 2,5 g distingue o movimento forte do repouso, que mede aproximadamente 1 g. Há intervalo mínimo de 1.800 ms entre detecções. O sensor registra somente na aba Explorar, sem um detalhe aberto, e é removido em `onPause`. O Android pode não oferecer acelerômetro: o aplicativo informa isso e mantém uma sugestão manual. O botão manual não comprova o requisito físico de sensor.

## Mapas

OpenStreetMap via osmdroid, sem chave nem cadastro. Coordenadas fictícias de referência em São Paulo. Os marcadores abrem os eventos e há abertura opcional de rota em aplicativo externo. Não usa GPS nem solicita permissão de localização. Tiles novos precisam de internet. Os créditos do OpenStreetMap ficam visíveis.

## Regras e testes

Valores monetários em centavos (`Long`), taxa demonstrativa de 10% com mínimo R$ 1,50 em ingressos pagos e zero em gratuitos. Arredondamento para o centavo mais próximo. `Math.addExact` detecta overflow ao somar. `BookingRules` valida nome e capacidade. `EventSearch` normaliza acentos e caixa. `ShakeDetector` é puro e testável com leituras numéricas.

Os testes unitários não substituem verificação física de sensor, renderização da UI ou testes instrumentados do banco.

## Limites da versão acadêmica

Sem login remoto, Firebase, Mercado Pago, carteira financeira, painel de produção ou sincronização. Não é um porte completo do produto comercial. É um app independente com um fluxo completo de demonstração para os requisitos do trabalho. A versão de desenvolvimento do APK não é uma publicação na Play Store.

## Referências

- SensorManager: https://developer.android.com/reference/kotlin/android/hardware/SensorManager
- ViewModel: https://developer.android.com/topic/libraries/architecture/viewmodel
- SQLiteOpenHelper: https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper
- osmdroid: https://github.com/osmdroid/osmdroid
- OpenStreetMap: https://www.openstreetmap.org/copyright
- ZXing: https://github.com/zxing/zxing
