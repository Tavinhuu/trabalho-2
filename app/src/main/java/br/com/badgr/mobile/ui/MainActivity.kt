package br.com.badgr.mobile.ui

import br.com.badgr.mobile.viewmodel.BadgrViewModel
import br.com.badgr.mobile.domain.model.Event
import br.com.badgr.mobile.domain.rules.BookingRules
import br.com.badgr.mobile.domain.rules.Pricing
import br.com.badgr.mobile.domain.rules.ShakeDetector


import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.math.sqrt
class MainActivity : ComponentActivity(), SensorEventListener {
    private val vm: BadgrViewModel by viewModels()
    private val bg = Color.rgb(24,25,27)
    private val panel = Color.rgb(38,39,42)
    private val muted = Color.rgb(169,169,181)
    private val pink = Color.rgb(240,106,190)
    private lateinit var root: LinearLayout
    private lateinit var body: LinearLayout
    private lateinit var sensors: SensorManager
    private var accelerometer: Sensor? = null
    private val shake = ShakeDetector()
    private var sensorLabel: TextView? = null
    private var lastSensorUi = 0L
    private var map: MapView? = null
    private var reserveButton: Button? = null
    private var resumed = false
    private var sensorRegistered = false
    private var suggestionDialog: AlertDialog? = null
    private var resultsContainer: LinearLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensors = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensors.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        Configuration.getInstance().userAgentValue = packageName
        if(savedInstanceState != null) {
            vm.tab = savedInstanceState.getString("tab",vm.tab)
            vm.selectedEvent = savedInstanceState.getLong("event",-1).takeIf { it != -1L }
            vm.query = savedInstanceState.getString("query",vm.query)
            vm.category = savedInstanceState.getString("category",vm.category)
            vm.onlyFavorites = savedInstanceState.getBoolean("favorites",vm.onlyFavorites)
        }
        vm.catalog.observe(this) { render() }
        vm.error.observe(this) { message -> if(message != null) { toast(message); vm.error.value = null } }
        vm.busy.observe(this) { busy -> reserveButton?.isEnabled = !busy; reserveButton?.text = if(busy) "Reservando…" else "Reservar ingresso de demonstração" }
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if(vm.selectedEvent != null) { vm.selectedEvent = null;render() }
                else if(vm.tab != "Explorar") { vm.tab = "Explorar";render() }
                else { isEnabled = false;onBackPressedDispatcher.onBackPressed() }
            }
        })
    }
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("tab",vm.tab);outState.putLong("event",vm.selectedEvent ?: -1)
        outState.putString("query",vm.query);outState.putString("category",vm.category);outState.putBoolean("favorites",vm.onlyFavorites)
    }
    private fun dp(n: Int) = (n * resources.displayMetrics.density).toInt()
    private fun shape(color: Int, radius: Int = 20) = GradientDrawable().apply { setColor(color);cornerRadius=dp(radius).toFloat() }
    private fun gradient(a: Int = pink, b: Int = Color.rgb(144,90,234)) = GradientDrawable(GradientDrawable.Orientation.TL_BR, intArrayOf(a,b)).apply { cornerRadius=dp(18).toFloat() }
    private fun col(): LinearLayout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
    private fun row(): LinearLayout = LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL }
    private fun text(value: String, size: Int = 16, color: Int = Color.WHITE, bold: Boolean = false): TextView = TextView(this).apply {
        text=value;textSize=size.toFloat();setTextColor(color);if(bold) setTypeface(typeface,Typeface.BOLD)
        setLineSpacing(dp(3).toFloat(),1f)
    }
    private fun gap(parent: LinearLayout, h: Int = 16) { parent.addView(View(this),LinearLayout.LayoutParams(1,dp(h))) }
    private fun label(parent: LinearLayout, value: String, size: Int=16, color: Int=Color.WHITE, bold: Boolean=false): TextView = text(value,size,color,bold).also { parent.addView(it) }
    private fun button(value: String, primary: Boolean = false, action: () -> Unit): Button = Button(this).apply {
        text=value;textSize=14f;isAllCaps=false;setTextColor(Color.WHITE)
        background=if(primary) gradient() else shape(panel,14)
        minimumHeight=dp(52);setPadding(dp(14),dp(8),dp(14),dp(8));setOnClickListener { action() }
        layoutParams=LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(8) }
    }
    private fun input(hintText: String, value: String=""): EditText = EditText(this).apply {
        hint=hintText;setText(value);setSingleLine(true);setTextColor(Color.WHITE);setHintTextColor(muted)
        textSize=16f;setPadding(dp(16),dp(10),dp(16),dp(10));background=shape(panel,16)
        layoutParams=LinearLayout.LayoutParams(-1,dp(54));inputType=android.text.InputType.TYPE_CLASS_TEXT
    }
    private fun card(parent: LinearLayout): LinearLayout = col().apply {
        setPadding(dp(20),dp(20),dp(20),dp(20));background=shape(panel)
        parent.addView(this,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(16) })
    }
    private fun render() {
        map?.onPause();map?.onDetach();map=null;sensorLabel=null;resultsContainer=null;reserveButton=null
        root=col().apply { setBackgroundColor(bg) }
        setContentView(root)
        val header=row().apply { setPadding(dp(24),dp(16),dp(24),dp(10)) }
        header.addView(text("badgr",34,Color.WHITE,true),LinearLayout.LayoutParams(0,-2,1f))
        header.addView(text("DEMO ACADÊMICA",10,pink,true))
        root.addView(header)
        val scroll=ScrollView(this).apply { isFillViewport=true;clipToPadding=false }
        body=col().apply { setPadding(dp(24),dp(8),dp(24),dp(28)) }
        scroll.addView(body)
        root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f))
        val event=vm.catalog.value?.events?.find { it.id == vm.selectedEvent }
        if(event != null) details(event) else when(vm.tab) {
            "Mapa" -> mapScreen()
            "Ingressos" -> ticketsScreen()
            "Perfil" -> profileScreen()
            else -> explore()
        }
        val nav=row().apply { setPadding(dp(8),dp(8),dp(8),dp(12));background=shape(bg,0) }
        for((title,symbol) in listOf("Explorar" to "⌕","Mapa" to "◎","Ingressos" to "▣","Perfil" to "◉")) {
            val tab=text("$symbol\n$title",12,if(vm.tab == title) pink else muted,true).apply {
                gravity=Gravity.CENTER;setPadding(0,dp(8),0,dp(8));contentDescription=title
                setOnClickListener { vm.tab=title;vm.selectedEvent=null;render() }
            }
            nav.addView(tab,LinearLayout.LayoutParams(0,dp(58),1f))
        }
        root.addView(nav)
        syncSensor()
    }
    private fun explore() {
        label(body,"SÃO PAULO, SP   ⌄",12,pink,true);gap(body,12)
        label(body,"Seu próximo rolê\ncomeça aqui.",32,Color.WHITE,true);gap(body,10)
        label(body,"Música independente. Encontros de verdade.",14,muted);gap(body,22)
        val search=input("Buscar festa, espaço ou cidade",vm.query)
        body.addView(search);gap(body,14)
        val chips=HorizontalScrollView(this).apply { isHorizontalScrollBarEnabled=false }
        val cats=row()
        for(c in listOf("Todos","Techno","House","Bass","Experimental")) {
            val chip=text(c,13,if(vm.category==c) bg else Color.WHITE,true).apply {
                setPadding(dp(16),dp(10),dp(16),dp(10));background=shape(if(vm.category==c) pink else panel,24)
                setOnClickListener { vm.category=c;render() }
            }
            cats.addView(chip,LinearLayout.LayoutParams(-2,-2).apply { rightMargin=dp(8) })
        }
        chips.addView(cats);body.addView(chips);gap(body,16)
        body.addView(button(if(vm.onlyFavorites) "♥ Mostrando favoritos · ver todos" else "♡ Ver meus favoritos") { vm.onlyFavorites=!vm.onlyFavorites;render() })
        gap(body,18)
        label(body,"Na sua frequência",21,Color.WHITE,true);gap(body,6)
        sensorLabel=label(body,if(accelerometer!=null) "Agite o celular e descubra um rolê surpresa." else "Acelerômetro indisponível neste dispositivo.",12,muted)
        body.addView(button("Me surpreenda") { surprise() });gap(body,20)
        resultsContainer=col().also { body.addView(it) }
        renderResults()
        search.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int) {}
            override fun onTextChanged(s: CharSequence?,start: Int,before: Int,count: Int) { vm.query=s.toString();renderResults() }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
    private fun renderResults() {
        val container=resultsContainer ?: return
        container.removeAllViews()
        val events=vm.visibleEvents()
        label(container,"${events.size} EVENTOS · AGENDA FICTÍCIA",11,muted,true);gap(container,14)
        if(events.isEmpty()) label(container,"Nenhum rolê por aqui ainda. Tente outra busca ou salve seus favoritos.",16,muted)
        events.forEach { eventCard(container,it) }
    }
    private fun poster(e: Event, height: Int=158): View {
        val box=col().apply { background=gradient(e.color,Color.rgb(28,27,42));gravity=Gravity.BOTTOM;setPadding(dp(20),dp(20),dp(20),dp(20)) }
        label(box,"BADGR PRESENTS / DEMO",10,Color.WHITE,true)
        val title=label(box,e.title,30,Color.WHITE,true)
        title.letterSpacing=0.04f
        gap(box,5);label(box,"${e.category.uppercase()}   /   2026",11,Color.WHITE,true)
        box.layoutParams=LinearLayout.LayoutParams(-1,dp(height))
        return box
    }
    private fun eventCard(parent: LinearLayout,e: Event) {
        val c=col().apply { background=shape(panel);setPadding(dp(2),dp(2),dp(2),dp(16)) }
        parent.addView(c,LinearLayout.LayoutParams(-1,-2).apply { bottomMargin=dp(20) })
        c.addView(poster(e))
        val info=col().apply { setPadding(dp(16),dp(14),dp(16),0) };c.addView(info)
        label(info,e.date,11,pink,true);gap(info,6)
        val r=row();r.addView(text(e.title,20,Color.WHITE,true),LinearLayout.LayoutParams(0,-2,1f))
        r.addView(text(if(e.id in vm.catalog.value!!.favorites) "♥" else "♡",29,pink).apply {
            setPadding(dp(12),0,dp(4),0);contentDescription="Favoritar ${e.title}"
            setOnClickListener { vm.favorite(e.id) }
        });info.addView(r)
        label(info,"${e.venue} · ${e.city}",12,muted);gap(info,12)
        label(info,if(e.priceCents==0L) "Entrada gratuita" else "${Pricing.money(e.priceCents)} + taxa",15,Color.WHITE,true)
        info.addView(button("Ver evento  ↗") { openEvent(e.id) })
    }
    private fun openEvent(id: Long) {
        (getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(root.windowToken,0)
        vm.selectedEvent=id;render()
    }
    private fun details(e: Event) {
        body.addView(button("‹ Voltar para ${vm.tab.lowercase()}") { vm.selectedEvent=null;render() });gap(body,18)
        body.addView(poster(e,200));gap(body,22)
        label(body,e.date,12,pink,true);gap(body,8)
        label(body,e.title,30,Color.WHITE,true);gap(body,10)
        label(body,"${e.venue}\n${e.city}",16,muted);gap(body,18)
        body.addView(button(if(e.id in vm.catalog.value!!.favorites) "♥ Salvo nos favoritos" else "♡ Salvar evento") { vm.favorite(e.id) })
        body.addView(button("◎ Ver localização no mapa") { vm.tab="Mapa";vm.selectedEvent=null;render();map?.controller?.animateTo(GeoPoint(e.latitude,e.longitude)) })
        gap(body,18);label(body,"Sobre o rolê",20,Color.WHITE,true);gap(body,8);label(body,e.description,15,muted);gap(body,22)
        val summary=card(body)
        label(summary,"INGRESSO INDIVIDUAL",11,pink,true);gap(summary,12)
        label(summary,"Ingresso: ${Pricing.money(e.priceCents)}",16)
        label(summary,"Taxa de serviço: ${Pricing.money(Pricing.fee(e.priceCents))}",14,muted)
        gap(summary,10);label(summary,"Total: ${Pricing.money(Pricing.total(e.priceCents))}",24,Color.WHITE,true)
        val reserved=vm.catalog.value!!.tickets.count { it.eventId==e.id }
        label(summary,"${e.capacity-reserved} disponíveis neste dispositivo",12,muted);gap(summary,16)
        val name=input("Nome de quem vai ao rolê")
        name.inputType=android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_CAP_WORDS
        name.filters=arrayOf(android.text.InputFilter.LengthFilter(80));summary.addView(name)
        val draftKey="holder-${e.id}"
        name.setText(getPreferences(MODE_PRIVATE).getString(draftKey,""))
        name.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(s: CharSequence?,start: Int,count: Int,after: Int) {}
            override fun onTextChanged(s: CharSequence?,start: Int,before: Int,count: Int) { getPreferences(MODE_PRIVATE).edit().putString(draftKey,s.toString()).apply() }
            override fun afterTextChanged(s: Editable?) {}
        })
        reserveButton=button("Reservar ingresso de demonstração",true) {
            if(vm.busy.value != true) {
                BookingRules.validate(name.text.toString(),reserved,e.capacity)?.let { toast(it);return@button }
                AlertDialog.Builder(this).setTitle("Confirmar reserva local?")
                    .setMessage("${e.title}\n${name.text}\nTotal demonstrativo: ${Pricing.money(Pricing.total(e.priceCents))}\n\nNenhum pagamento será cobrado. O ingresso não vale em eventos reais.")
                    .setNegativeButton("Voltar",null).setPositiveButton("Reservar") { _,_ -> vm.reserve(e.id,name.text.toString()) }.show()
            }
        }.also { summary.addView(it);it.isEnabled=vm.busy.value != true }
        gap(summary,10);label(summary,"DEMONSTRAÇÃO · Sem cobrança e sem validade de entrada em eventos reais.",11,muted)
    }
    private fun mapScreen() {
        label(body,"O rolê está por perto.",28,Color.WHITE,true);gap(body,8)
        label(body,"Locais de referência em São Paulo. Toque em um marcador para abrir o evento.",14,muted);gap(body,18)
        val m=MapView(this).apply {
            setTileSource(TileSourceFactory.MAPNIK);setMultiTouchControls(true)
            controller.setZoom(13.5);controller.setCenter(GeoPoint(-23.552,-46.644))
        }
        map=m
        for(e in vm.catalog.value!!.events) {
            m.overlays.add(Marker(m).apply {
                position=GeoPoint(e.latitude,e.longitude);title=e.title;snippet=e.venue
                setAnchor(Marker.ANCHOR_CENTER,Marker.ANCHOR_BOTTOM)
                setOnMarkerClickListener { _,_ -> openEvent(e.id);true }
            })
        }
        body.addView(m,LinearLayout.LayoutParams(-1,dp(320)))
        if(resumed) m.onResume()
        gap(body,8)
        val credits=label(body,"© OpenStreetMap contributors · Mapa requer internet",11,muted)
        credits.setOnClickListener { safeOpen("https://www.openstreetmap.org/copyright") }
        gap(body,18)
        vm.catalog.value!!.events.forEach { e ->
            val c=card(body);label(c,e.title,18,Color.WHITE,true);label(c,e.venue,13,muted)
            c.addView(button("Ver evento") { openEvent(e.id) })
            c.addView(button("Abrir rota no app de mapas") { safeOpen("geo:0,0?q=${e.latitude},${e.longitude}(${Uri.encode(e.venue)})") })
        }
    }
    private fun ticketsScreen() {
        label(body,"Seus ingressos",28,Color.WHITE,true);gap(body,8)
        label(body,"Reservas salvas neste celular, mesmo sem internet.",14,muted);gap(body,22)
        val tickets=vm.catalog.value!!.tickets
        if(tickets.isEmpty()) {
            val c=card(body);label(c,"A próxima história\nainda vai começar.",23,Color.WHITE,true);gap(c,10)
            label(c,"Explore a agenda e reserve seu primeiro ingresso de demonstração.",15,muted)
            c.addView(button("Encontrar meu rolê",true) { vm.tab="Explorar";render() })
        }
        for(t in tickets) {
            val e=vm.catalog.value!!.events.find { it.id==t.eventId } ?: continue
            val c=card(body)
            label(c,if(t.checkedIn) "ENTRADA REGISTRADA · DEMO" else "RESERVADO · DEMONSTRAÇÃO",10,pink,true);gap(c,10)
            label(c,e.title,24,Color.WHITE,true);label(c,e.date,12,muted);gap(c,10)
            label(c,t.holder,17,Color.WHITE,true);label(c,"Total demonstrativo: ${Pricing.money(t.totalCents)}",13,muted);gap(c,16)
            val qr=ImageView(this).apply {
                setImageBitmap(qrBitmap("BADGR-DEMO:${t.id}"));setBackgroundColor(Color.WHITE);setPadding(dp(12),dp(12),dp(12),dp(12))
                contentDescription="QR Code de demonstração do ingresso ${t.id}"
            }
            c.addView(qr,LinearLayout.LayoutParams(dp(190),dp(190)).apply { gravity=Gravity.CENTER_HORIZONTAL })
            gap(c,12);label(c,t.id,10,muted)
            label(c,"Sem validade em eventos reais",12,pink,true)
            c.addView(button("Compartilhar reserva") {
                startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type="text/plain";putExtra(Intent.EXTRA_TEXT,"BADGR DEMO • ${e.title}\n${e.date}\n${t.holder}\nCódigo: ${t.id}\nReserva acadêmica, sem validade em eventos reais.")
                },"Compartilhar reserva"))
            })
            if(!t.checkedIn) c.addView(button("Simular check-in local") {
                AlertDialog.Builder(this).setTitle("Simular entrada?").setMessage("Esta ação marca apenas esta reserva local como utilizada.")
                    .setNegativeButton("Voltar",null).setPositiveButton("Registrar") { _,_ -> vm.checkIn(t.id) }.show()
            })
        }
    }
    private fun profileScreen() {
        label(body,"Feito pra sua cena.",28,Color.WHITE,true);gap(body,10)
        label(body,"Badgr Mobile · versão acadêmica",15,muted);gap(body,24)
        val c=card(body)
        label(c,"Seu espaço neste celular",20,Color.WHITE,true);gap(c,12)
        label(c,"${vm.catalog.value!!.favorites.size} eventos favoritos\n${vm.catalog.value!!.tickets.size} reservas locais",16,muted)
        gap(c,12);label(c,"Nenhum login ou dado da Badgr em produção é utilizado.",13,muted)
        val sensor=card(body)
        label(sensor,"Descoberta por movimento",20,Color.WHITE,true);gap(sensor,10)
        label(sensor,accelerometer?.let { "Acelerômetro disponível: ${it.name}" } ?: "Este dispositivo não tem acelerômetro.",13,muted)
        label(sensor,"Na aba Explorar, agite o celular para receber uma sugestão. O botão Me surpreenda funciona como alternativa manual.",14,muted)
        val tech=card(body)
        label(tech,"Por dentro do aplicativo",20,Color.WHITE,true);gap(tech,10)
        label(tech,"Kotlin nativo\nMVVM com ViewModel e LiveData\nRoom + SQLite com entidades e DAOs\nOpenStreetMap com osmdroid\nSensorManager e acelerômetro\nTestes JUnit das regras de negócio",14,muted)
        body.addView(button("Limpar favoritos e reservas") {
            AlertDialog.Builder(this).setTitle("Reiniciar demonstração?").setMessage("Apaga apenas os favoritos e ingressos deste aplicativo. A agenda continua disponível.")
                .setNegativeButton("Cancelar",null).setPositiveButton("Limpar") { _,_ -> vm.reset() }.show()
        })
        gap(body,16);label(body,"Eventos fictícios. A agenda e os locais não representam uma programação confirmada.",12,muted)
    }
    private fun qrBitmap(payload: String): Bitmap {
        val matrix=MultiFormatWriter().encode(payload,BarcodeFormat.QR_CODE,384,384)
        return Bitmap.createBitmap(384,384,Bitmap.Config.RGB_565).apply {
            val pixels=IntArray(384*384) { i -> if(matrix[i%384,i/384]) Color.BLACK else Color.WHITE }
            setPixels(pixels,0,384,0,0,384,384)
        }
    }
    private fun safeOpen(uri: String) {
        try { startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(uri))) }
        catch(_: android.content.ActivityNotFoundException) { toast("Nenhum aplicativo disponível para abrir este link.") }
    }
    private fun toast(s: String) { Toast.makeText(this,s,Toast.LENGTH_LONG).show() }
    private fun surprise() {
        if(suggestionDialog?.isShowing == true) return
        val e=vm.discover() ?: return
        suggestionDialog=AlertDialog.Builder(this).setTitle("Seu próximo rolê?").setMessage("${e.title}\n${e.date}\n${e.venue}")
            .setNegativeButton("Depois",null).setPositiveButton("Ver evento") { _,_ -> openEvent(e.id) }.show()
    }
    private fun syncSensor() {
        if(!::sensors.isInitialized) return
        val should=resumed && vm.tab=="Explorar" && vm.selectedEvent==null
        if(should && !sensorRegistered) accelerometer?.let { sensorRegistered=sensors.registerListener(this,it,SensorManager.SENSOR_DELAY_UI) }
        else if(!should && sensorRegistered) { sensors.unregisterListener(this);sensorRegistered=false }
    }
    override fun onResume() { super.onResume();resumed=true;map?.onResume();syncSensor() }
    override fun onPause() { resumed=false;sensors.unregisterListener(this);sensorRegistered=false;map?.onPause();super.onPause() }
    override fun onDestroy() { suggestionDialog?.dismiss();map?.onDetach();map=null;super.onDestroy() }
    override fun onAccuracyChanged(sensor: Sensor?,accuracy: Int) {}
    override fun onSensorChanged(event: SensorEvent) {
        if(event.sensor.type!=Sensor.TYPE_ACCELEROMETER) return
        val now=SystemClock.elapsedRealtime()
        if(now-lastSensorUi>400) {
            val g=sqrt(event.values[0].toDouble()*event.values[0]+event.values[1].toDouble()*event.values[1]+event.values[2].toDouble()*event.values[2])/SensorManager.GRAVITY_EARTH
            sensorLabel?.text="Acelerômetro ativo · %.1f g · Agite para descobrir".format(g)
            lastSensorUi=now
        }
        if(shake.accept(event.values[0],event.values[1],event.values[2],now)) surprise()
    }
}
