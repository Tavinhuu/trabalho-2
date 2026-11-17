package br.com.badgr.mobile.domain.rules

import java.util.Locale
import kotlin.math.sqrt
import br.com.badgr.mobile.domain.model.Event

object Pricing {
    fun fee(priceCents: Long): Long {
        require(priceCents >= 0) { "Preço inválido" }
        return if (priceCents == 0L) 0 else maxOf(150L, (priceCents + 5) / 10)
    }
    fun total(priceCents: Long): Long = Math.addExact(priceCents, fee(priceCents))
    fun money(cents: Long): String = java.text.NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR")).format(cents / 100.0)
}
object BookingRules {
    fun validate(holder: String, reserved: Int, capacity: Int): String? = when {
        holder.trim().length < 3 -> "Informe seu nome com pelo menos 3 caracteres."
        holder.trim().length > 80 -> "O nome deve ter até 80 caracteres."
        reserved >= capacity -> "Este lote está esgotado."
        else -> null
    }
}
object EventSearch {
    fun filter(events: List<Event>, query: String, category: String = "Todos"): List<Event> {
        fun normalized(s: String) = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "").lowercase(Locale.ROOT)
        val q = normalized(query.trim())
        return events.filter { (category == "Todos" || it.category == category) &&
            normalized("${it.title} ${it.venue} ${it.city}").contains(q) }
    }
}
/** Usa a magnitude da aceleração, incluindo gravidade. Limita eventos repetidos. */
class ShakeDetector(private val thresholdG: Double = 2.5, private val cooldownMs: Long = 1800) {
    private var lastShake: Long? = null
    fun accept(x: Float, y: Float, z: Float, timeMs: Long): Boolean {
        val g = sqrt(x.toDouble()*x + y.toDouble()*y + z.toDouble()*z) / 9.80665
        val previous = lastShake
        if (g < thresholdG || (previous != null && timeMs - previous < cooldownMs)) return false
        lastShake = timeMs
        return true
    }
}
