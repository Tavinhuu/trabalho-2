# Roteiro do código — Room + DAO + MVVM

## Resposta de persistência para o professor

> “A persistência usa Room sobre SQLite. `BadgrDatabase` abre o arquivo local `badgr-demo.db`. `EventEntity`, `FavoriteEntity` e `TicketEntity` definem as tabelas. As interfaces `EventDao`, `FavoriteDao` e `TicketDao` definem as consultas e gravações. O Repository coordena esses DAOs e as regras, e o ViewModel publica o estado para a Activity.”

**Para mostrar rapidamente:** abra `data/local/BadgrDatabase.kt`, depois `data/local/entity/TicketEntity.kt`, `data/local/dao/TicketDao.kt` e `data/repository/EventRepository.kt`.

**Fluxo da reserva:** botão na Activity chama ViewModel.reserve; o ViewModel chama Repository.reserve; o Repository usa EventDao.getById, TicketDao.countForEvent, valida as regras e chama TicketDao.insert dentro de withTransaction; recarrega o Catalog; LiveData notifica a Activity.

## Organização dos arquivos Kotlin

Os caminhos abaixo são relativos a `app/src/main/java/br/com/badgr/mobile/`.

| Arquivo | O que explicar |
|---|---|
| `data/local/BadgrDatabase.kt` | Classe @Database. Declara entidades, expõe os três DAOs e abre badgr-demo.db como singleton com Room.databaseBuilder. |
| `data/local/DatabaseMigrations.kt` | Migração 1 para 2. Adapta tabelas antigas para o esquema Room e preserva registros. SQL manual existe somente nesta conversão de esquema. |
| `data/local/DemoEvents.kt` | Quatro eventos fictícios para inserir na primeira carga se events estiver vazia. |
| `data/local/dao/EventDao.kt` | @Dao: getAll, getById, count e insertAll. Consultas de eventos e inserção da agenda. |
| `data/local/dao/FavoriteDao.kt` | @Dao: getEventIds, insert, delete e deleteAll. Favoritos persistidos, sem duplicatas. |
| `data/local/dao/TicketDao.kt` | @Dao: getAll, countForEvent, insert, checkIn e deleteAll. É aqui que o acesso aos ingressos está definido. |
| `data/local/entity/EventEntity.kt` | @Entity events: chave id e campos do evento. @ColumnInfo mapeia price, lat e lon. |
| `data/local/entity/FavoriteEntity.kt` | @Entity favorites: event_id é chave primária e estrangeira. CASCADE remove favorito quando evento é removido. |
| `data/local/entity/TicketEntity.kt` | @Entity tickets: UUID, titular, total, data e check-in. Chave estrangeira de evento e índice tickets_event. |
| `data/mapper/EntityMappers.kt` | Converte entidades Room em modelos de domínio e evento de domínio em entidade. Mantém anotações de banco fora do domínio. |
| `data/repository/EventRepository.kt` | Coordena DAOs, regras e transações. Não contém rawQuery, ContentValues ou comandos SQL de CRUD. |
| `domain/model/Models.kt` | Event, Ticket e Catalog. Estruturas Kotlin utilizadas pela View e pelo ViewModel. |
| `domain/rules/Rules.kt` | Pricing, BookingRules, EventSearch e ShakeDetector. Regras puras testadas com JUnit. |
| `ui/MainActivity.kt` | View: cria Views nativas, observa LiveData, navega e integra MapView, QR e acelerômetro. |
| `viewmodel/BadgrViewModel.kt` | Estado e ações. Obtém Room singleton, cria Repository e publica Catalog, erro e busy via LiveData. |

## Métodos e linhas para localizar no Android Studio

As linhas correspondem a esta versão. Se você editar, procure pelo nome do método.

| Arquivo | Método ou declaração | Linha |
|---|---|---:|
| `data/local/BadgrDatabase.kt` | `abstract fun eventDao(): EventDao` | 16 |
| `data/local/BadgrDatabase.kt` | `abstract fun favoriteDao(): FavoriteDao` | 17 |
| `data/local/BadgrDatabase.kt` | `abstract fun ticketDao(): TicketDao` | 18 |
| `data/local/BadgrDatabase.kt` | `fun getInstance(context: Context): BadgrDatabase = instance ?: synchronized(this)` | 23 |
| `data/local/DatabaseMigrations.kt` | `override fun migrate(db: SupportSQLiteDatabase)` | 9 |
| `data/local/dao/EventDao.kt` | `suspend fun getAll(): List<EventEntity>` | 13 |
| `data/local/dao/EventDao.kt` | `suspend fun getById(id: Long): EventEntity?` | 16 |
| `data/local/dao/EventDao.kt` | `suspend fun count(): Int` | 19 |
| `data/local/dao/EventDao.kt` | `suspend fun insertAll(events: List<EventEntity>)` | 22 |
| `data/local/dao/FavoriteDao.kt` | `suspend fun getEventIds(): List<Long>` | 12 |
| `data/local/dao/FavoriteDao.kt` | `suspend fun insert(favorite: FavoriteEntity)` | 15 |
| `data/local/dao/FavoriteDao.kt` | `suspend fun delete(eventId: Long): Int` | 18 |
| `data/local/dao/FavoriteDao.kt` | `suspend fun deleteAll()` | 21 |
| `data/local/dao/TicketDao.kt` | `suspend fun getAll(): List<TicketEntity>` | 11 |
| `data/local/dao/TicketDao.kt` | `suspend fun countForEvent(eventId: Long): Int` | 14 |
| `data/local/dao/TicketDao.kt` | `suspend fun insert(ticket: TicketEntity)` | 17 |
| `data/local/dao/TicketDao.kt` | `suspend fun checkIn(id: String): Int` | 20 |
| `data/local/dao/TicketDao.kt` | `suspend fun deleteAll()` | 23 |
| `data/mapper/EntityMappers.kt` | `fun EventEntity.toDomain() = Event(id, title, date, venue, city, category, priceCents, capacity, latitude, longitude, description, color)` | 8 |
| `data/mapper/EntityMappers.kt` | `fun Event.toEntity() = EventEntity(id, title, date, venue, city, category, priceCents, capacity, latitude, longitude, description, color)` | 9 |
| `data/mapper/EntityMappers.kt` | `fun TicketEntity.toDomain() = Ticket(id, eventId, holder, totalCents, createdAt, checkedIn)` | 10 |
| `data/repository/EventRepository.kt` | `suspend fun load(): Catalog = database.withTransaction` | 21 |
| `data/repository/EventRepository.kt` | `suspend fun toggleFavorite(id: Long) = database.withTransaction` | 26 |
| `data/repository/EventRepository.kt` | `suspend fun reserve(eventId: Long, holder: String): String = database.withTransaction` | 31 |
| `data/repository/EventRepository.kt` | `suspend fun checkIn(id: String): Boolean = tickets.checkIn(id) == 1` | 41 |
| `data/repository/EventRepository.kt` | `suspend fun reset() = database.withTransaction` | 43 |
| `domain/rules/Rules.kt` | `fun fee(priceCents: Long): Long` | 8 |
| `domain/rules/Rules.kt` | `fun total(priceCents: Long): Long = Math.addExact(priceCents, fee(priceCents))` | 12 |
| `domain/rules/Rules.kt` | `fun money(cents: Long): String = java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(cents / 100.0)` | 13 |
| `domain/rules/Rules.kt` | `fun validate(holder: String, reserved: Int, capacity: Int): String? = when` | 16 |
| `domain/rules/Rules.kt` | `fun filter(events: List<Event>, query: String, category: String = "Todos"): List<Event>` | 24 |
| `domain/rules/Rules.kt` | `fun normalized(s: String) = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)` | 25 |
| `domain/rules/Rules.kt` | `fun accept(x: Float, y: Float, z: Float, timeMs: Long): Boolean` | 35 |
| `ui/MainActivity.kt` | `override fun onCreate(savedInstanceState: Bundle?)` | 63 |
| `ui/MainActivity.kt` | `override fun handleOnBackPressed()` | 79 |
| `ui/MainActivity.kt` | `override fun onSaveInstanceState(outState: Bundle)` | 86 |
| `ui/MainActivity.kt` | `private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()` | 91 |
| `ui/MainActivity.kt` | `private fun shape(color: Int, radius: Int = 20) = GradientDrawable().apply` | 92 |
| `ui/MainActivity.kt` | `private fun gradient(a: Int = pink, b: Int = Color.rgb(144,90,234)) = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(a,b)).apply` | 93 |
| `ui/MainActivity.kt` | `private fun col(): LinearLayout = LinearLayout(this).apply` | 94 |
| `ui/MainActivity.kt` | `private fun row(): LinearLayout = LinearLayout(this).apply` | 95 |
| `ui/MainActivity.kt` | `private fun text(value: String, size: Int = 16, color: Int = Color.WHITE, bold: Boolean = false): TextView = TextView(this).apply` | 96 |
| `ui/MainActivity.kt` | `private fun gap(parent: LinearLayout, h: Int = 16)` | 100 |
| `ui/MainActivity.kt` | `private fun label(parent: LinearLayout, value: String, size: Int=16, color: Int=Color.WHITE, bold: Boolean=false): TextView = text(value,size,color,bold).also` | 101 |
| `ui/MainActivity.kt` | `private fun button(value: String, primary: Boolean = false, action: () -> Unit): Button = Button(this).apply` | 102 |
| `ui/MainActivity.kt` | `private fun input(hintText: String, value: String=""): EditText = EditText(this).apply` | 108 |
| `ui/MainActivity.kt` | `private fun card(parent: LinearLayout): LinearLayout = col().apply` | 113 |
| `ui/MainActivity.kt` | `private fun render()` | 117 |
| `ui/MainActivity.kt` | `private fun explore()` | 147 |
| `ui/MainActivity.kt` | `override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int)` | 171 |
| `ui/MainActivity.kt` | `override fun onTextChanged(s: CharSequence?,start: Int,before: Int,count: Int)` | 172 |
| `ui/MainActivity.kt` | `override fun afterTextChanged(s: Editable?)` | 173 |
| `ui/MainActivity.kt` | `private fun renderResults()` | 176 |
| `ui/MainActivity.kt` | `private fun poster(e: Event, height: Int=158): View` | 184 |
| `ui/MainActivity.kt` | `private fun eventCard(parent: LinearLayout,e: Event)` | 193 |
| `ui/MainActivity.kt` | `private fun openEvent(id: Long)` | 208 |
| `ui/MainActivity.kt` | `private fun details(e: Event)` | 212 |
| `ui/MainActivity.kt` | `override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int)` | 234 |
| `ui/MainActivity.kt` | `override fun onTextChanged(s: CharSequence?,start: Int,before: Int,count: Int)` | 235 |
| `ui/MainActivity.kt` | `override fun afterTextChanged(s: Editable?)` | 236 |
| `ui/MainActivity.kt` | `private fun mapScreen()` | 248 |
| `ui/MainActivity.kt` | `private fun ticketsScreen()` | 275 |
| `ui/MainActivity.kt` | `private fun profileScreen()` | 308 |
| `ui/MainActivity.kt` | `private fun qrBitmap(payload: String): Bitmap` | 328 |
| `ui/MainActivity.kt` | `private fun safeOpen(uri: String)` | 335 |
| `ui/MainActivity.kt` | `private fun toast(s: String)` | 339 |
| `ui/MainActivity.kt` | `private fun surprise()` | 340 |
| `ui/MainActivity.kt` | `private fun syncSensor()` | 346 |
| `ui/MainActivity.kt` | `override fun onResume()` | 352 |
| `ui/MainActivity.kt` | `override fun onPause()` | 353 |
| `ui/MainActivity.kt` | `override fun onDestroy()` | 354 |
| `ui/MainActivity.kt` | `override fun onAccuracyChanged(sensor: Sensor?,accuracy: Int)` | 355 |
| `ui/MainActivity.kt` | `override fun onSensorChanged(event: SensorEvent)` | 356 |
| `viewmodel/BadgrViewModel.kt` | `private fun refresh() = viewModelScope.launch` | 30 |
| `viewmodel/BadgrViewModel.kt` | `fun favorite(id: Long) = viewModelScope.launch` | 33 |
| `viewmodel/BadgrViewModel.kt` | `fun reserve(id: Long, holder: String)` | 36 |
| `viewmodel/BadgrViewModel.kt` | `fun checkIn(id: String) = viewModelScope.launch` | 48 |
| `viewmodel/BadgrViewModel.kt` | `fun reset() = viewModelScope.launch` | 54 |
| `viewmodel/BadgrViewModel.kt` | `fun visibleEvents(): List<Event> = EventSearch.filter(_catalog.value?.events.orEmpty(), query, category)` | 57 |
| `viewmodel/BadgrViewModel.kt` | `fun discover(): Event? = _catalog.value?.events?.randomOrNull()` | 59 |

## DAO: o que é e como explicar

DAO significa Data Access Object. É uma interface que centraliza o acesso ao banco. Room lê as anotações e gera a implementação durante a compilação.

```kotlin
@Dao
interface TicketDao {
    @Query("SELECT * FROM tickets ORDER BY created_at DESC")
    suspend fun getAll(): List<TicketEntity>

    @Insert
    suspend fun insert(ticket: TicketEntity)
}
```

`@Dao` identifica o componente. `@Query` define uma consulta SQL. `@Insert` manda gerar uma inserção para a entidade recebida. Os parâmetros nomeados como `:eventId` são vinculados de forma separada do SQL. `suspend` integra a chamada com corrotinas e com os executores do Room.

**Não escrevemos TicketDaoImpl manualmente.** O compilador Room gera as implementações dentro de app/build/. Não editar arquivos gerados.

## Entidades: onde estão as tabelas?

- EventEntity representa events.
- FavoriteEntity representa favorites.
- TicketEntity representa tickets.
- @PrimaryKey define o identificador único.
- @ColumnInfo permite ter um nome Kotlin e outro na coluna.
- @ForeignKey mantém o relacionamento do ingresso/favorito com evento.
- @Index em event_id ajuda a buscar ou contar ingressos de um evento.

Entidade de banco não é a mesma coisa que modelo de domínio: as entidades têm anotações Room e ficam em data/local/entity. Os modelos sem anotações ficam em domain/model. EntityMappers converte entre os dois.

## Banco e migração

BadgrDatabase é uma classe abstrata que herda RoomDatabase. @Database lista as entidades e define versão 2. Os getters abstratos retornam os DAOs. getInstance usa singleton com @Volatile e synchronized e mantém contexto da aplicação.

Room.databaseBuilder indica classe do banco e nome do arquivo. Room continua usando SQLite local. Não existe Firebase neste projeto. O esquema exportado para app/schemas documenta as tabelas e pode apoiar futuras migrações.

DatabaseMigrations.FROM_1_TO_2 preserva o banco da primeira versão, que usava SQLiteOpenHelper diretamente. A migração reconstrói as tabelas para os nomes e constraints esperados pelo Room, copiando registros. Não há fallbackToDestructiveMigration.

**Onde o arquivo fica?** No armazenamento interno privado do app, normalmente em /data/user/0/br.com.badgr.mobile.demo/databases/badgr-demo.db. Em um emulador, abra App Inspection > Database Inspector com o app em execução e veja as tabelas.

**Como comprovar:** favorite e reserve, encerre o app e abra de novo. Depois mostre as linhas em favorites e tickets no Database Inspector. Desinstalar ou limpar dados pode apagar esse banco.

## Repository: por que existe se já tem DAO?

O DAO define a operação de banco. O Repository coordena regras e várias operações. reserve precisa buscar o evento, contar reservas, validar nome/estoque e inserir ingresso. Isso deve ocorrer na mesma transação.

`database.withTransaction` confirma tudo quando o bloco termina com sucesso e desfaz a operação se houver exceção. O estoque é local ao aparelho. Não existe coordenação de reservas entre aparelhos diferentes.

load também roda em transação para devolver um retrato consistente de eventos, favoritos e ingressos. Se a tabela events estiver vazia, insere DemoEvents usando EventDao.insertAll.

checkIn chama TicketDao.checkIn. O UPDATE tem condição checked_in=0. A primeira entrada altera uma linha, outra chamada não altera nenhuma. A View também deixa de mostrar o botão após o check-in.

Não usamos allowMainThreadQueries. As funções suspend dos DAOs executam por mecanismos do Room, e withTransaction usa o executor transacional. O ViewModel coordena as chamadas com viewModelScope. Suspend por si só não significa criar uma nova thread.

## ViewModel e interface

BadgrViewModel mantém LiveData de Catalog, erro e busy. O catálogo mutável é privado e exposto como LiveData de leitura. busy evita enviar a mesma reserva durante uma operação em andamento. O ViewModel não guarda Views nem Activity.

MainActivity observa vm.catalog e chama render quando o estado muda. Ela cria Views nativas em Kotlin, sem XML de layout e sem Compose. O Manifest aponta para .ui.MainActivity após a organização em pacotes.

**ViewModel não é banco.** Mantém estado durante rotação, mas não garante os dados depois do encerramento do processo. Quem garante a persistência é Room/SQLite.

onSaveInstanceState salva navegação e filtros para restauração pelo Android. SharedPreferences guarda o rascunho do nome digitado em cada evento. Essas preferências não substituem as tabelas de ingressos.

## Domínio e testes

Models.kt tem Event, Ticket e Catalog. Rules.kt contém:

| Regra | Explicação |
|---|---|
| Pricing | Centavos inteiros. Taxa 10% com mínimo R$ 1,50 em pagos e zero em gratuitos. R$ 35,00 vira 3500 e total 3850 |
| BookingRules | Nome de 3 a 80 caracteres e capacidade disponível |
| EventSearch | Remove acentos e diferenciação de caixa e filtra categoria |
| ShakeDetector | Magnitude dos três eixos em g e intervalo entre detecções |

DomainTest.kt em app/src/test/java/br/com/badgr/mobile/ contém 22 testes: PricingTest (6), BookingRulesTest (6), ShakeDetectorTest (5) e EventSearchTest (5). Execute pela seta ao lado das classes ou com ./gradlew testDebugUnitTest.

@Test identifica o caso. assertEquals compara resultado esperado e obtido. assertTrue/assertFalse verificam condições. assertNull confirma ausência de mensagem de validação.

Exemplo: Pricing.total(3500) deve resultar em 3850. No sensor, gravidade de 9,80665 em um eixo deve resultar em false, pois o aparelho parado mede aproximadamente 1 g.

Os testes unitários não testam UI, hardware ou SQLite Android. O processador Room verifica SQL dos DAOs ao compilar. Isso também não substitui uma demonstração no emulador.

## Sensor, mapa e QR

**Sensor:** MainActivity obtém TYPE_ACCELEROMETER. syncSensor registra o listener somente em Explorar, com Activity retomada e sem detalhe aberto. onSensorChanged recebe x, y e z. ShakeDetector calcula sqrt(x²+y²+z²)/9,80665. O limiar é 2,5 g, com intervalo de 1800 ms. onPause remove o listener. O botão Me surpreenda é alternativa manual, não comprova leitura do sensor.

**Emulador:** controles de sensores permitem variar a pose/aceleração. Rotação lenta pode manter magnitude próxima de 1 g e não disparar. Não diga que precisa de GPS ou permissão de localização para o recurso implementado.

**Mapa:** mapScreen cria MapView com osmdroid, OpenStreetMap, GeoPoint e Marker. Coordenadas vêm do catálogo persistido. Toque abre detalhes. Tiles novos precisam de rede. O botão de rota abre outro aplicativo.

**QR:** ZXing transforma BADGR-DEMO: mais UUID em uma imagem QR válida. Não autentica pagamentos nem vale no produto real. O check-in é manual e local, sem câmera.

## Arquivos de configuração e recursos

| Arquivo | Função |
|---|---|
| settings.gradle.kts | Nome do projeto, módulo app e repositórios de bibliotecas |
| build.gradle.kts da raiz | Versões dos plugins Android, Kotlin e KAPT |
| app/build.gradle.kts | SDK, versão do app, dependências Room, compiler via kapt e schemaLocation |
| gradle.properties | Memória Gradle e AndroidX |
| gradlew / gradlew.bat | Execução padronizada do Gradle em Unix / Windows |
| gradle/wrapper/gradle-wrapper.properties | Distribuição Gradle 8.9 |
| gradle/wrapper/gradle-wrapper.jar | Binário padrão que inicializa o Gradle |
| app/src/main/AndroidManifest.xml | Activity inicial ui.MainActivity, permissões de rede e sensor opcional |
| app/src/main/res/values/styles.xml | Tema escuro e cores do sistema |
| app/src/main/res/drawable/ic_badgr.xml | Ícone vetorial |
| app/schemas/ | JSON exportado com estrutura das tabelas Room |
| .gitignore | Exclui cache, build, IDE e local.properties |
| README.md | Execução e arquitetura |
| GUIA-PERGUNTAS.md | Perguntas rápidas atualizadas para DAO |
| VALIDACAO.md | Resultado de build, testes e limites da validação |

KAPT processa as anotações de Room. Adicionar @Dao e @Entity exige gerar código no build; as interfaces sozinhas não implementam a persistência. A dependência room-compiler é quem gera esse código.

## Roteiro de cinco minutos

1. **0:00–0:30:** “É uma versão acadêmica da Badgr em Kotlin, com MVVM e persistência Room.”
2. **0:30–1:30:** buscar evento, favoritar, reservar e mostrar QR.
3. **1:30–2:00:** fechar/reabrir e mostrar persistência.
4. **2:00–2:40:** mapa e marcador.
5. **2:40–3:10:** sensor na aba Explorar.
6. **3:10–4:30:** abrir Entity, DAO, Database, Repository e ViewModel, explicando o fluxo.
7. **4:30–5:00:** mostrar um teste de preço e um de sensor.

## Respostas rápidas para não confundir

**Room é DAO?** Room é a biblioteca. DAO é o componente de acesso, marcado com @Dao.

**Repository grava direto com SQL?** Não. Chama os DAOs. SQL manual fora das anotações aparece somente na migração do esquema anterior.

**Entity é ViewModel?** Não. Entity define uma tabela e seus campos. ViewModel mantém estado e ações da interface.

**Tem sincronização remota?** Não. Banco, estoque e check-in são locais.

**O que falta para produção?** APIs seguras, pagamento, autenticação, sincronização e validação de ingressos em servidor. Nada disso deve ser apresentado como implementado.
