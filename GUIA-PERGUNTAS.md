# Preparação para as perguntas do professor

Leia o código antes de apresentar. Cada integrante precisa entender o caminho entre botão, ViewModel, Repository e SQLite.

**O que é MVVM aqui?**
A View é a MainActivity. O BadgrViewModel expõe o catálogo em LiveData e recebe as ações. EventRepository e BadgrDatabase gerenciam o armazenamento. Domain.kt tem entidades e regras.

**Por que usar ViewModel?**
Para separar estado e ações da interface e manter os dados durante rotação. Ele não deve guardar referências à Activity ou às Views.

**Como a interface recebe os dados?**
A Activity observa `vm.catalog`. Quando o ViewModel publica um novo Catalog, a interface renderiza esse estado. LiveData respeita o ciclo de vida do observador.

**Como evita travar a tela?**
O Repository usa `withContext(Dispatchers.IO)` para consultas e gravações. `viewModelScope.launch` coordena o trabalho e atualiza LiveData na thread principal.

**Por que SQLite em vez de Firebase?**
O requisito pede banco, sem impor serviço remoto. SQLite mantém o projeto independente de credenciais e permite demonstrar persistência offline. O banco fica no armazenamento privado do app.

**Como garante estoque?**
`reserve` inicia transação, consulta capacidade e quantidade reservada, valida e insere antes de confirmar a transação. Isso vale para o banco deste aparelho. Estoque global exigiria servidor e operação atômica no backend.

**Como calcula a taxa?**
Em centavos inteiros. Evento gratuito tem taxa zero. Para os pagos, usa 10%, arredonda e aplica o mínimo de 150 centavos. R$ 35,00 mais R$ 3,50 dá R$ 38,50.

**Qual sensor foi usado?**
O acelerômetro, obtido com `getDefaultSensor(TYPE_ACCELEROMETER)`. O Android envia três componentes em m/s². O código calcula a magnitude em unidades de g.

**Por que o celular parado mede aproximadamente 1 g?**
O acelerômetro inclui a gravidade. A magnitude do vetor parado é aproximadamente 9,80665 m/s², que equivale a 1 g.

**Como evita várias sugestões seguidas?**
O detector exige magnitude de pelo menos 2,5 g e intervalo mínimo de 1.800 ms. A View também evita abrir várias caixas de sugestão simultaneamente.

**Por que remover o listener?**
Para interromper trabalho e consumo desnecessário. O registro acontece apenas na tela Explorar, com a Activity retomada. `onPause` cancela a inscrição.

**O mapa usa GPS?**
Não. Usa coordenadas de referência dos eventos no banco. O requisito de mapa é atendido com MapView, marcadores e interação. GPS seria um recurso adicional.

**Por que não existe uma chave do Google Maps?**
A implementação usa OpenStreetMap/osmdroid. O carregamento dos tiles precisa de rede e os créditos precisam permanecer visíveis.

**O QR Code é real?**
A imagem é um QR Code válido, gerado com ZXing, contendo `BADGR-DEMO:` e um UUID. Serve apenas à demonstração. Não comprova pagamento nem autentica ingresso na Badgr comercial.

**Quais testes são unitários?**
PricingTest, BookingRulesTest, ShakeDetectorTest e EventSearchTest. Validam regras isoladas com JUnit 4, sem depender de interface ou de aparelho.

**Por que testar sensor com números?**
Para validar deterministicamente o algoritmo de detecção. A leitura física continua precisando de demonstração no aparelho ou emulador com controles de sensores.

**Como impede repetir check-in?**
O UPDATE tem condição `checked_in=0`. Depois da primeira atualização, outra chamada retorna zero linhas alteradas. A interface também deixa de exibir o botão de check-in.

**O que falta para virar o aplicativo oficial?**
Autenticação e APIs seguras, sincronização, pagamento real, estoque no servidor, validação de ingressos autenticada, testes instrumentados e publicação. Esses serviços não fazem parte desta demonstração.
