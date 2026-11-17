# Badgr Mobile — roteiro para apresentar o código

## 1. A resposta mais importante: onde está a persistência?

Se o professor perguntar **“onde está a persistência dos dados?”**, responda:

> A persistência principal está em um banco SQLite local chamado `badgr-demo.db`. A classe `BadgrDatabase` cria as tabelas e o `EventRepository` faz a leitura e gravação. Os eventos ficam em `events`, os favoritos em `favorites` e os ingressos em `tickets`. Por isso, favoritos e reservas continuam disponíveis quando eu fecho e reabro o aplicativo.

Abra estes pontos, nesta ordem:

| O que mostrar | Arquivo e linha | O que explicar |
|---|---|---|
| Nome do banco | `BadgrDatabase.kt:8` | `SQLiteOpenHelper(context, "badgr-demo.db", null, 1)` define arquivo e versão do banco |
| Criação das tabelas | `BadgrDatabase.kt:9` | `onCreate` cria `events`, `favorites` e `tickets` |
| Dados iniciais | `BadgrDatabase.kt:14` | Insere a agenda fictícia apenas quando o banco é criado |
| Leitura | `EventRepository.kt:9` | `load` consulta as tabelas com `SELECT` e monta um `Catalog` |
| Favoritos | `EventRepository.kt:20` | `toggleFavorite` insere ou remove um favorito no banco |
| Gravação do ingresso | `EventRepository.kt:30` | `reserve` valida e insere o ingresso com `insertOrThrow` |
| Check-in | `EventRepository.kt:47` | `UPDATE` altera `checked_in` de 0 para 1 |
| Limpeza dos dados | `EventRepository.kt:50` | Apaga favoritos e reservas, mantendo a agenda |

As classes Kotlin estão em `app/src/main/java/br/com/badgr/mobile/`. As linhas deste roteiro correspondem à versão entregue. No Android Studio, procure também pelo nome do método se as linhas mudarem.

**Como comprovar ao vivo:** favorite um evento, reserve um ingresso, feche o aplicativo e abra novamente. Mostre que os dois continuam lá. Para demonstrar uma nova leitura do banco com mais clareza, remova o app da lista de recentes antes de reabrir. Não desinstale nem limpe os dados, pois essas ações podem apagar o banco local.

**Onde o arquivo fica?** No armazenamento interno privado do app. Neste projeto, normalmente `/data/user/0/br.com.badgr.mobile.demo/databases/badgr-demo.db`. O Android administra esse diretório. Em aparelho/emulador compatível, com o app de desenvolvimento em execução, o Android Studio permite inspecioná-lo em **App Inspection > Database Inspector**. Também é possível usar o Device Explorer, conforme as permissões do aparelho.

**Existe outra persistência?** Sim. Em `MainActivity.kt:225` e `:228`, `getPreferences(MODE_PRIVATE)` usa SharedPreferences para guardar o rascunho do nome digitado em cada evento. Isso é separado do SQLite. `LiveData` e `ViewModel` mantêm estado em memória e não substituem o banco em disco.

## 2. Como abrir e apresentar no Android Studio

1. Clone o repositório ou extraia o projeto.
2. Abra a pasta que contém `settings.gradle.kts`, `build.gradle.kts` e `gradlew`.
3. Aguarde o Gradle Sync. O projeto usa SDK 35, Gradle 8.9 e Java alvo 17. Use o JDK embarcado no Android Studio se for compatível.
4. Execute em Android 8.0/API 26 ou superior. Para a demonstração física do sensor, prefira seu celular.
5. No painel esquerdo, a visualização **Project** ajuda a enxergar todos os arquivos. A visualização **Android** agrupa Manifest, Kotlin e recursos.
6. Use a busca do IDE para abrir rapidamente uma classe ou método. Deixe `BadgrDatabase`, `EventRepository`, `BadgrViewModel`, `MainActivity`, `Domain` e `DomainTest` abertos em abas.

Não precisa explicar cada importação ou cada cor da interface. Saiba localizar todos os arquivos, mas concentre a fala nas responsabilidades e no caminho dos dados.

## 3. Roteiro de fala de 5 minutos

| Tempo | Ação | Fala sugerida |
|---|---|---|
| 0:00–0:30 | Abrir Explorar | “Desenvolvemos uma versão acadêmica da Badgr para descobrir eventos independentes. É um aplicativo Android nativo em Kotlin, com agenda fictícia.” |
| 0:30–1:30 | Buscar, favoritar e reservar | “A busca aceita nome, espaço e cidade. Ao reservar, o aplicativo valida o nome e a disponibilidade e grava um ingresso no SQLite.” |
| 1:30–2:00 | Mostrar QR e reabrir app | “Este QR contém um identificador único de demonstração. O ingresso e o favorito persistem após fechar e reabrir.” |
| 2:00–2:40 | Abrir mapa e marcador | “O mapa usa OpenStreetMap com osmdroid. Cada marcador representa a coordenada de referência de um evento e abre seus detalhes.” |
| 2:40–3:10 | Voltar a Explorar e agitar | “O acelerômetro mede aceleração nos três eixos. Quando a magnitude ultrapassa o limiar, o app sugere um evento.” |
| 3:10–4:15 | Mostrar classes MVVM e banco | “A Activity representa a View. O ViewModel gerencia o estado e as ações. O Repository acessa o SQLite, fora da thread principal.” |
| 4:15–5:00 | Mostrar e executar testes | “Os 22 testes unitários verificam preço, validação de reserva, busca e detecção de movimento. Eles não substituem o teste físico do sensor.” |

O PDF prevê mais 5 minutos de perguntas. A demonstração completa precisa caber nos primeiros 5 minutos.

## 4. Mapa de todos os arquivos escritos no projeto

| Arquivo | Para que serve | O que você precisa saber |
|---|---|---|
| `settings.gradle.kts` | Define nome do projeto, repositórios de dependências e módulo `:app` | `google()` e `mavenCentral()` são fontes de bibliotecas. `include(":app")` inclui o aplicativo |
| `build.gradle.kts` da raiz | Declara versões dos plugins Android e Kotlin | Plugin Android 8.7.3 e Kotlin 2.0.21. `apply false` deixa a aplicação dos plugins para o módulo |
| `gradle.properties` | Configura Gradle, memória da JVM e AndroidX | É configuração da ferramenta de build, sem dados dos eventos |
| `gradlew` | Executa o Gradle Wrapper em Linux/macOS | Use `./gradlew testDebugUnitTest` e `./gradlew assembleDebug` |
| `gradlew.bat` | Mesmo papel no Windows | Use `gradlew.bat` no terminal Windows |
| `gradle/wrapper/gradle-wrapper.properties` | Indica qual distribuição Gradle usar | Fixa Gradle 8.9 para tornar a compilação reproduzível |
| `gradle/wrapper/gradle-wrapper.jar` | Inicializador do Wrapper | Binário padrão gerado pelo Gradle, não é regra de negócio |
| `.gitignore` | Evita versionar arquivos locais ou gerados | Exclui `local.properties`, `.gradle`, `.idea` e diretórios `build` |
| `app/build.gradle.kts` | Configura o aplicativo e suas dependências | Namespace, applicationId, SDK mínimo/alvo, Java 17 e bibliotecas |
| `app/src/main/AndroidManifest.xml` | Declara aplicativo, Activity inicial, permissões e sensor opcional | `MAIN` e `LAUNCHER` identificam a tela inicial. Internet permite carregar tiles |
| `app/src/main/res/values/styles.xml` | Define tema escuro, fonte e cores do sistema | Não armazena dados nem regras de negócio |
| `app/src/main/res/drawable/ic_badgr.xml` | Ícone vetorial do aplicativo | Recurso gráfico usado pelo Manifest |
| `app/src/main/java/br/com/badgr/mobile/Domain.kt` | Entidades e regras puras | Event, Ticket, Catalog, Pricing, BookingRules, EventSearch e ShakeDetector |
| `app/src/main/java/br/com/badgr/mobile/BadgrDatabase.kt` | Criação do banco e dados iniciais | SQLiteOpenHelper, três tabelas, FK, índice, agenda fictícia |
| `app/src/main/java/br/com/badgr/mobile/EventRepository.kt` | Consultas e gravações | SQL, transações, corrotinas em IO, UUID, check-in e limpeza |
| `app/src/main/java/br/com/badgr/mobile/BadgrViewModel.kt` | Estado e ações da aplicação | LiveData, `viewModelScope`, loading/erro, coordenação com Repository |
| `app/src/main/java/br/com/badgr/mobile/MainActivity.kt` | Interface e integração com recursos Android | Views nativas, navegação, observação do estado, mapa, QR e sensor |
| `app/src/test/java/br/com/badgr/mobile/DomainTest.kt` | Os 22 testes unitários | Quatro classes JUnit que verificam regras sem depender da interface |
| `README.md` | Instalação, execução, requisitos e limites | Referência para abrir o projeto e demonstrar |
| `GUIA-PERGUNTAS.md` | Respostas rápidas | Revisão antes da apresentação |
| `VALIDACAO.md` | Registro do que foi verificado | Build e testes passaram. Interface e hardware ainda precisam de teste no aparelho |
| `ROTEIRO-APRESENTACAO-CODIGO.md` | Este guia completo | Consulta por arquivo, fluxo e pergunta |

`local.properties` será criado localmente pelo Android Studio com o caminho do SDK do seu computador. Não deve ir ao GitHub. `build/`, arquivos `.class`, recursos compilados e caches são gerados pelas ferramentas e não são o código-fonte escrito do aplicativo.

### Bibliotecas do app/build.gradle.kts

| Dependência | Uso concreto |
|---|---|
| Activity KTX | `ComponentActivity` e delegado `by viewModels()` |
| Lifecycle ViewModel KTX | ViewModel e `viewModelScope` |
| Lifecycle LiveData KTX | Estado observável e observação pela Activity |
| Coroutines Android | Trabalho assíncrono e `Dispatchers.IO` |
| osmdroid | `MapView`, tiles OpenStreetMap e `Marker` |
| ZXing Core | Geração da matriz de pixels do QR Code |
| JUnit 4 | `@Test` e asserções dos testes locais |

## 5. Domain.kt — modelos e regras

**Event, linhas 6–8:** estrutura de um evento. Guarda identificador, título, data de exibição, local, cidade, categoria, preço em centavos, capacidade, latitude, longitude, descrição e cor.

**Ticket, linhas 9–10:** ingresso/reserva. Guarda UUID, evento, titular, total, data de criação e check-in.

**Catalog, linhas 11–12:** retrato do estado dos dados: lista de eventos, conjunto de favoritos e lista de ingressos. O ViewModel publica esse objeto para a View.

**Pricing, linhas 14–20:** preço e taxa. Para ingressos pagos, aplica 10% com mínimo de 150 centavos. Gratuitos têm taxa zero. `total` soma preço e taxa. `money` só formata para real brasileiro.

> “Uso centavos inteiros para não fazer as contas monetárias com ponto flutuante. Um ingresso de R$ 35,00 é representado por 3500. A taxa é 350, então o total é 3850.”

**BookingRules, linhas 22–29:** valida nome com 3 a 80 caracteres após retirar espaços nas extremidades e compara reservas com capacidade. Devolve uma mensagem quando há erro e `null` quando está válido.

**EventSearch, linhas 30–38:** normaliza acentos e caixa e filtra por texto e categoria. A busca acontece sobre os dados carregados em memória, não é uma nova consulta SQL a cada letra.

**ShakeDetector, linhas 40–49:** regra matemática que decide se uma leitura representa um movimento forte. Usa magnitude em g e intervalo mínimo. Não acessa o sensor diretamente: recebe números. Essa separação torna a regra testável.

## 6. BadgrDatabase.kt — criação e estrutura do SQLite

`SQLiteOpenHelper` administra abertura, criação e versão do banco. O código usa versão 1.

| Tabela | Chave | Dados |
|---|---|---|
| `events` | `id` | Catálogo e coordenadas |
| `favorites` | `event_id` | Um registro por evento salvo |
| `tickets` | `id` em texto/UUID | Evento, titular, total, criação e check-in |

`favorites.event_id` e `tickets.event_id` referenciam `events.id`. `onConfigure` habilita chaves estrangeiras. `favorites.event_id` também é chave primária, impedindo favoritos duplicados. O índice `tickets_event` ajuda a localizar/contar ingressos por evento.

`DemoEvents` é a fonte inicial de quatro eventos fictícios. Eles são inseridos em `onCreate`. Depois o app lê a tabela SQLite, não usa apenas a lista fixa para exibir a agenda.

**Quando onCreate executa?** Quando o arquivo do banco ainda não existe e a conexão é aberta pela primeira vez. Não é o `onCreate` da Activity. A Activity pode ser recriada várias vezes sem recriar o banco.

**O que é onUpgrade?** Ponto de migração quando a versão do banco aumenta. Nesta versão inicial, não há migração implementada. O método falha explicitamente para uma atualização não prevista. Não diga que o app já suporta migrações futuras.

## 7. EventRepository.kt — onde o banco é usado

**load, linha 9:** abre o banco de leitura, executa consultas, percorre os cursores e transforma linhas em objetos Kotlin. `use` fecha cada cursor ao terminar. O resultado é um `Catalog`.

**toggleFavorite, linha 20:** tenta remover o favorito. Se nenhuma linha foi removida, insere. A operação ocorre em transação.

**reserve, linha 30:** este é o melhor método para explicar o fluxo de gravação.

1. Abre o banco de escrita e inicia a transação.
2. Busca preço e capacidade atuais na tabela.
3. Conta quantos ingressos já existem para o evento.
4. Valida nome e disponibilidade.
5. Gera identificador com `UUID.randomUUID()`.
6. Insere os dados com `ContentValues` e `insertOrThrow`.
7. Marca a transação como bem-sucedida.
8. Finaliza a transação no `finally`.

Se houver uma exceção antes de `setTransactionSuccessful`, o encerramento não confirma aquela transação. Assim, a contagem e a gravação fazem parte de uma mesma operação no banco deste aparelho.

**checkIn, linha 47:** atualiza somente o ingresso com aquele id e `checked_in=0`. Devolve `true` quando exatamente uma linha foi atualizada. Uma nova tentativa já não atende à condição.

**reset, linha 50:** remove tickets e favoritos. A agenda permanece.

**Por que usar parâmetros `?`?** Para separar valores do comando SQL, evitando montar consulta por concatenação de entrada do usuário. `ContentValues` organiza os campos inseridos/atualizados.

**Por que `suspend` e `withContext(Dispatchers.IO)`?** `suspend` permite coordenação com corrotinas. O `withContext(IO)` é o ponto que tira o trabalho de banco da thread principal. `suspend` sozinho não significa executar em background.

## 8. BadgrViewModel.kt — estado e coordenação

- Linha 12: cria o Repository com banco usando contexto de Application, sem reter Activity.
- Linhas 13–14: `_catalog` é mutável internamente. `catalog` expõe a leitura como LiveData.
- Linhas 15–16: erro e indicador de reserva em andamento.
- Linhas 17–21: aba, evento selecionado, texto de busca, categoria e filtro de favoritos.
- Linha 22: carrega dados na inicialização.
- Linha 23: `refresh` chama o Repository e publica o catálogo.
- Linha 26: favorito grava no banco e recarrega os dados.
- Linha 29: reserva impede envio duplicado enquanto `busy=true`, chama o Repository e muda para Ingressos após sucesso.
- Linha 41: coordena check-in e recarga.
- Linha 47: coordena limpeza.
- Linha 50: filtra os eventos em memória.
- Linha 52: escolhe uma sugestão aleatória.
- Linha 53: fecha o helper quando o ViewModel é descartado.

> “O ViewModel coordena as ações da tela e publica o estado. Ele não desenha botões nem escreve SQL. A Activity não decide como gravar uma reserva.”

**O ViewModel mantém dados após fechar o processo?** Não. Ele sobrevive a mudanças de configuração, como rotação, mas pode ser destruído quando o processo termina. SQLite é que mantém os dados em disco. Algumas informações de navegação também são guardadas em `onSaveInstanceState` para restauração pelo Android.

## 9. MainActivity.kt — interface, mapa, QR e sensor

A interface foi escrita com Views nativas em Kotlin. Não usa Compose nem arquivos XML de layout. `styles.xml` é o tema, não o layout das telas.

| Método/ponto | Linha | Responsabilidade |
|---|---|---|
| `onCreate` | 56 | Inicializa serviços, restaura navegação e registra observadores |
| `vm.catalog.observe` | 68 | Atualiza a interface quando o catálogo muda |
| `onSaveInstanceState` | 79 | Guarda aba, evento e filtros para restauração |
| Helpers `text`, `button`, `input`, `card` | 89–106 | Criam componentes visuais reutilizados |
| `render` | 110 | Monta cabeçalho, tela selecionada e navegação inferior |
| `explore` | 140 | Busca, categorias, favoritos e sugestão |
| `renderResults` | 169 | Atualiza só o contêiner de resultados durante a digitação |
| `poster` e `eventCard` | 177 e 186 | Desenham identificação visual e cartão do evento |
| `openEvent` | 201 | Fecha teclado e navega para detalhes |
| `details` | 205 | Mostra evento, valores e formulário de reserva |
| SharedPreferences | 225 e 228 | Recupera/grava rascunho do nome |
| `mapScreen` | 241 | Cria mapa e marcadores |
| `ticketsScreen` | 268 | Mostra reservas, QR, compartilhar e check-in |
| `profileScreen` | 301 | Informações locais, sensor e limpeza |
| `qrBitmap` | 321 | Gera o bitmap do QR com ZXing |
| `safeOpen` | 328 | Abre links/rotas e trata ausência de aplicativo |
| `surprise` | 333 | Mostra sugestão e evita caixas de sugestão simultâneas |
| `syncSensor` | 339 | Registra sensor apenas quando necessário |
| `onResume` / `onPause` | 345 e 346 | Retoma/pausa mapa e listener do sensor |
| `onDestroy` | 347 | Libera mapa e fecha caixa de sugestão |
| `onSensorChanged` | 349 | Recebe leituras, exibe magnitude e chama detector |

### Mapa: o que mostrar

Abra `mapScreen`:

```kotlin
setTileSource(TileSourceFactory.MAPNIK)
setMultiTouchControls(true)
controller.setCenter(GeoPoint(-23.552,-46.644))
```

O loop percorre eventos do catálogo, cria `Marker`, usa latitude/longitude e define clique que abre o detalhe.

> “O mapa é integrado à tela. As coordenadas vêm dos eventos persistidos no banco. Não estamos capturando a posição atual do usuário.”

O botão de rota abre outro aplicativo. Isso é adicional ao mapa integrado. Não é preciso chave Google Maps. Tiles novos dependem de internet e os créditos OpenStreetMap ficam visíveis.

### Sensor: o que mostrar

1. `onCreate`: obtém `SensorManager` e `TYPE_ACCELEROMETER`.
2. `syncSensor`: `registerListener` com `SENSOR_DELAY_UI` apenas na aba Explorar, sem detalhe aberto e com a Activity retomada.
3. `onSensorChanged`: recebe os três eixos e chama `ShakeDetector.accept`.
4. `onPause`: remove o listener.
5. `Domain.kt`: mostra fórmula e limiar.

> “O acelerômetro inclui gravidade. Parado, a magnitude fica perto de 1 g. Um movimento de pelo menos 2,5 g pode gerar uma sugestão, respeitando 1,8 segundo de intervalo. O sensor é desligado quando a tela não precisa dele.”

Não diga que o botão “Me surpreenda” é sensor: ele é uma alternativa manual. A leitura física é o listener `onSensorChanged`.

### QR Code: o que mostrar

`ticketsScreen` passa `BADGR-DEMO:` mais o UUID para `qrBitmap`. ZXing gera uma matriz, e o código converte cada célula em pixel preto ou branco de um Bitmap.

O QR não é um leitor de câmera, não comprova pagamento e não vale na produção. O check-in existente é manual e local. UUID torna o identificador difícil de adivinhar, mas não transforma um QR local em credencial autenticada por servidor.

## 10. DomainTest.kt — testes que o professor pode pedir

| Classe | Quantidade | Casos |
|---|---:|---|
| `PricingTest` | 6 | Gratuito, mínimo, 10%, total, arredondamento, preço negativo |
| `BookingRulesTest` | 6 | Nome vazio, curto e longo, esgotado, excesso de reservas e última vaga |
| `ShakeDetectorTest` | 5 | Repouso, aceleração alta, intervalo, liberação após intervalo e direção negativa |
| `EventSearchTest` | 5 | Acentos/caixa, cidade, categoria, espaços e busca sem resultado |
| Total | 22 | Todos passaram na compilação entregue |

**Para executar:** clique na seta ao lado da classe de teste ou rode `./gradlew testDebugUnitTest`.

**@Test:** identifica um caso de teste. `assertEquals` compara resultado esperado e obtido. `assertTrue` e `assertFalse` verificam condições. `assertNull` indica validação sem erro. `expected=IllegalArgumentException::class` espera uma exceção.

Escolha um exemplo simples:

> “Em `totalIncludesFee`, o teste chama `Pricing.total(3500)` e espera 3850. Assim valida que um ingresso de R$ 35 tem R$ 3,50 de taxa.”

E um exemplo do sensor:

> “O teste de repouso passa 0, 0 e 9,80665 m/s² e espera falso. O teste de aceleração alta passa 30 m/s² em um eixo e espera verdadeiro. Isso verifica a regra de detecção sem depender de um movimento aleatório do aparelho.”

**São testes do banco?** Não. São testes unitários das regras puras. Banco, interface, carregamento dos tiles e leitura do hardware não foram cobertos por testes instrumentados nesta entrega.

## 11. Caminho completo de uma reserva

1. A pessoa digita o nome em `MainActivity.details`.
2. A Activity faz uma validação rápida para feedback e mostra a confirmação.
3. O botão chama `vm.reserve(eventId, nome)`.
4. `BadgrViewModel.reserve` marca `busy=true` e inicia uma corrotina.
5. `EventRepository.reserve` muda para IO e abre transação SQLite.
6. O Repository consulta capacidade e quantidade já reservada e revalida a regra.
7. Cria UUID e grava uma linha em `tickets`.
8. O ViewModel chama `repository.load()` para recuperar os dados atualizados.
9. Publica o novo `Catalog` em LiveData e seleciona a aba Ingressos.
10. A Activity observa a alteração, renderiza os ingressos e gera a imagem do QR.

**Por que validar duas vezes?** A View dá resposta rápida, mas a confirmação importante ocorre no Repository usando o banco atual, dentro da transação. A interface pode estar mostrando uma contagem antiga.

## 12. Perguntas rápidas para revisar

**“Onde está o MVVM?”** MainActivity é View, BadgrViewModel é ViewModel e o Model inclui entidades, regras, Repository e SQLite.

**“Onde os dados são persistidos?”** No arquivo SQLite `badgr-demo.db`, criado por BadgrDatabase e acessado por EventRepository. Rascunho do nome usa SharedPreferences.

**“LiveData é banco de dados?”** Não, é um contêiner observável do estado em memória.

**“O banco recria toda vez que abre a tela?”** Não. `SQLiteOpenHelper.onCreate` cria quando o banco não existe. Depois `load` lê as tabelas existentes.

**“O que acontece quando rotaciona?”** A Activity pode ser recriada. ViewModel mantém estado durante essa mudança e a nova Activity observa o mesmo estado. O mapa e o sensor seguem o ciclo de vida.

**“Por que não usar a thread principal para SQL?”** Pode bloquear desenho e interação, causando travamentos. O Repository usa `Dispatchers.IO`.

**“O que garante que não reserva acima da capacidade?”** Consulta e inserção na mesma transação do banco local. Isso não controla estoque entre celulares diferentes.

**“Qual permissão o acelerômetro usa?”** Este uso comum não pede permissão em runtime. O Manifest declara o sensor como opcional. O app confere se o hardware existe.

**“Precisa de permissão GPS?”** Não, pois o projeto usa coordenadas dos eventos, sem acessar localização do usuário.

**“Por que não há XML de telas?”** As telas foram criadas programaticamente com Views Kotlin. Android permite tanto layouts XML quanto criação de Views em código.

**“O check-in usa a câmera?”** Não. A versão demonstra uma atualização local por botão. O QR é gerado, mas não existe scanner de câmera neste app.

**“O aplicativo funciona offline?”** Catálogo já persistido, favoritos, reservas e QR funcionam localmente. O mapa precisa de rede para carregar tiles novos e rotas externas dependem do aplicativo aberto.

**“O que acontece se desinstalar?”** Os dados locais normalmente são removidos. Este app desativa backup e não tem conta remota nem sincronização.

**“Como atualizar a estrutura do banco?”** Precisaria aumentar a versão e implementar migração em `onUpgrade`. A entrega tem apenas a versão inicial 1.

**“Os dados são da Badgr real?”** Não. É um projeto acadêmico independente, com eventos fictícios e sem conexão a pagamentos ou coleções de produção.

## 13. O que revisar primeiro se faltarem poucos minutos

1. Decore a resposta de persistência e abra `BadgrDatabase` + `EventRepository.reserve`.
2. Entenda a divisão entre MainActivity, ViewModel e Repository.
3. Saiba apontar `onSensorChanged`, `registerListener` e `unregisterListener`.
4. Abra `mapScreen` e identifique `MapView`, `GeoPoint` e `Marker`.
5. Abra `DomainTest` e consiga explicar ao menos um teste de taxa e um de movimento.
6. Não prometa Firebase, pagamento real, GPS, scanner ou testes instrumentados: essas funcionalidades não estão nesta versão.
