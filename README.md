# Badgr Mobile — Trabalho 2

Aplicativo Android nativo em Kotlin com **MVVM + Room + DAOs + Repository**, acelerômetro, mapa e testes unitários. Agenda fictícia e reservas locais, sem pagamentos ou integração com a Badgr em produção.

## Rodar no Android Studio com emulador

1. Clone `https://github.com/Tavinhuu/trabalho-2.git` ou atualize com **Git > Pull** se já clonou.
2. Abra a raiz `trabalho-2`, que contém `settings.gradle.kts`. Aguarde **Gradle Sync**. Instale SDK 35 quando solicitado. Use JDK 17 ou 21.
3. Abra **Tools > Device Manager > Create Virtual Device**, escolha um Pixel e uma imagem Android 14/API 34. Baixe a imagem se necessário.
4. Inicie o emulador, selecione a configuração **app** e clique em **Run**.
5. Após atualizar este projeto, aceite o Gradle Sync: foram adicionadas dependências Room e o plugin KAPT.

## Estrutura

| Pasta em `app/src/main/java/br/com/badgr/mobile/` | Responsabilidade |
|---|---|
| `ui/` | Activity, Views nativas e integração com sensor/mapa |
| `viewmodel/` | Estado observável LiveData e ações da tela |
| `domain/model/` | Modelos Event, Ticket e Catalog |
| `domain/rules/` | Preços, validação, busca e detecção de movimento |
| `data/repository/` | Coordenação de DAOs e transações |
| `data/local/dao/` | Interfaces `@Dao` com consultas, inserções, exclusões e check-in |
| `data/local/entity/` | Classes `@Entity`, chaves primárias e estrangeiras |
| `data/local/` | `@Database`, singleton Room, migração e agenda inicial |
| `data/mapper/` | Conversão entre entidades persistidas e modelos de domínio |

Fluxo: a View chama o ViewModel, que chama o Repository. O Repository usa os DAOs. Room gera as implementações dos DAOs e persiste no SQLite.

## Onde está a persistência?

Em `data/local/BadgrDatabase.kt`: `Room.databaseBuilder` abre o arquivo privado `badgr-demo.db`.

- `EventEntity`, `FavoriteEntity`, `TicketEntity`: definem as tabelas.
- `EventDao`, `FavoriteDao`, `TicketDao`: definem o acesso aos dados.
- `TicketDao.insert`: grava o ingresso.
- `TicketDao.getAll`: recupera os ingressos ao reabrir.
- `EventRepository.reserve`: valida estoque e insere dentro de `withTransaction`.
- `DatabaseMigrations.FROM_1_TO_2`: adapta a versão anterior, preservando dados.

Room é uma camada sobre SQLite, não um banco remoto. Funções `suspend` dos DAOs usam os executores do Room. Não usamos `allowMainThreadQueries`. `withTransaction` mantém o conjunto de operações atômico. O banco singleton usa contexto de Application.

## Apresentação e perguntas

Leia [ROTEIRO-APRESENTACAO-CODIGO.md](ROTEIRO-APRESENTACAO-CODIGO.md) e [GUIA-PERGUNTAS.md](GUIA-PERGUNTAS.md). O roteiro descreve cada arquivo e os métodos principais.

Demonstre busca, favorito, reserva, QR, reabertura, mapa e leitura do acelerômetro. O mapa usa OpenStreetMap/osmdroid e precisa de rede para tiles novos. Na aba Explorar, movimento de pelo menos 2,5 g sugere um evento. O botão “Me surpreenda” é alternativa manual, não leitura de sensor. No emulador, use controles de sensores; rotação lenta pode não superar o limiar.

## Testes e compilação

```sh
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Windows: `gradlew.bat`. APK: `app/build/outputs/apk/debug/app-debug.apk`.

Os 22 testes JUnit validam regras de preço, reserva, busca e movimento. O compilador Room valida as consultas dos DAOs. Isso não substitui testes de UI, banco real e hardware no emulador. Consulte [VALIDACAO.md](VALIDACAO.md).

Kotlin 2.0.21, Android Gradle Plugin 8.7.3, Gradle 8.9, Room 2.7.2 e KAPT. API mínima 26, compile SDK 35. Os esquemas Room exportados ficam em `app/schemas/` e devem ser versionados. Classes geradas pelo KAPT ficam em `app/build/` e não devem ser editadas.

## Limites

Reservas e estoque apenas neste aparelho. QR de demonstração sem validade em eventos reais. Check-in por botão, sem scanner de câmera. Sem GPS, autenticação remota, cobrança ou sincronização com Firebase. SharedPreferences guarda apenas o rascunho do nome digitado.

## Referências

- Room: https://developer.android.com/training/data-storage/room
- DAOs: https://developer.android.com/training/data-storage/room/accessing-data
- ViewModel: https://developer.android.com/topic/libraries/architecture/viewmodel
- SensorManager: https://developer.android.com/reference/kotlin/android/hardware/SensorManager
- osmdroid: https://github.com/osmdroid/osmdroid
- OpenStreetMap: https://www.openstreetmap.org/copyright
