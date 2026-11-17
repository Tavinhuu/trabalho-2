# Perguntas rápidas — versão Room + DAO

**Onde está a persistência?**
Em `data/local/BadgrDatabase.kt`. Room abre o arquivo SQLite `badgr-demo.db`. As tabelas são as classes `@Entity`, e as operações são definidas nas interfaces `@Dao`.

**O que é DAO?**
Data Access Object: componente que define como acessar os dados. `EventDao`, `FavoriteDao` e `TicketDao` têm `@Query` e `@Insert`. Room gera as classes que executam essas operações.

**O que é Entity?**
Uma classe Kotlin marcada com `@Entity` que representa uma tabela. `@PrimaryKey` identifica a chave primária, `@ColumnInfo` mapeia nomes de colunas e `@ForeignKey` define relações.

**Onde grava ingresso?**
`EventRepository.reserve` valida e chama `TicketDao.insert`. A inserção é persistida pelo Room. A Activity não contém SQL.

**Qual a diferença entre Room e SQLite?**
SQLite é o banco local. Room é a biblioteca de persistência que organiza entidades, DAOs e banco e verifica as consultas durante a compilação.

**Qual a diferença entre Repository e DAO?**
DAO define operações de persistência. Repository coordena essas operações com regras de negócio, como validar capacidade e gravar uma reserva na mesma transação.

**Onde está o MVVM?**
`ui/MainActivity` é View, `viewmodel/BadgrViewModel` é ViewModel, e o Model inclui domínio e camada de dados. A View observa LiveData.

**O ViewModel é persistência?**
Não. Ele mantém estado em memória durante mudanças de configuração. O banco em disco mantém os dados após encerrar o processo.

**Como evita travar a tela?**
Os DAOs têm funções `suspend`, e Room executa o trabalho nos próprios executores. O ViewModel usa `viewModelScope`. `suspend` sozinho não cria uma thread.

**Como garante estoque?**
`database.withTransaction` engloba consulta do evento, contagem de ingressos, validação e inserção. A garantia vale para o banco local, sem estoque compartilhado entre aparelhos.

**Como atualiza o banco antigo?**
O banco agora tem versão 2. `DatabaseMigrations.FROM_1_TO_2` adapta a estrutura anterior e copia eventos, favoritos e ingressos. Não usa migração destrutiva.

**Para que serve o singleton?**
Para compartilhar uma instância do RoomDatabase no processo, com contexto da aplicação. Evita criar uma conexão independente para cada ação da tela.

**Qual sensor?**
Acelerômetro. `onSensorChanged` recebe os eixos; `ShakeDetector` calcula a magnitude. Repouso é aproximadamente 1 g. O limiar é 2,5 g com intervalo de 1,8 segundo.

**Como economiza sensor?**
Registra apenas em Explorar, com a Activity retomada e sem detalhes abertos. Remove o listener em `onPause`.

**O mapa usa GPS?**
Não. Os marcadores usam as coordenadas dos eventos no banco. MapView usa OpenStreetMap/osmdroid, e novos tiles precisam de internet.

**O QR comprova pagamento?**
Não. Contém `BADGR-DEMO:` e UUID. É apenas uma demonstração. O check-in é manual e local, sem câmera.

**Quais testes?**
22 testes JUnit de preço, nome/capacidade, busca e detecção de movimento. São testes de regras, não de hardware ou UI. Room também verifica as consultas na compilação.

**Onde fica o layout XML?**
A interface é criada com Views nativas em Kotlin na MainActivity. `styles.xml` define o tema, e não o layout das telas.
