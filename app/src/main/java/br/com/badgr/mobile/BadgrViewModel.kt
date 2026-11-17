package br.com.badgr.mobile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

/** Estado da tela e ações ficam fora da Activity. Não guarda referências a Views. */
class BadgrViewModel(app: Application) : AndroidViewModel(app) {
    private val repository = EventRepository(BadgrDatabase(app))
    private val _catalog = MutableLiveData(Catalog())
    val catalog: LiveData<Catalog> = _catalog
    val error = MutableLiveData<String?>()
    val busy = MutableLiveData(false)
    var tab = "Explorar"
    var selectedEvent: Long? = null
    var query = ""
    var category = "Todos"
    var onlyFavorites = false
    init { refresh() }
    private fun refresh() = viewModelScope.launch {
        try { _catalog.value = repository.load() } catch (e: Exception) { error.value = e.message ?: "Falha ao carregar agenda" }
    }
    fun favorite(id: Long) = viewModelScope.launch {
        try { repository.toggleFavorite(id); _catalog.value = repository.load() } catch(e: Exception) { error.value = e.message }
    }
    fun reserve(id: Long, holder: String) {
        if (busy.value == true) return
        busy.value = true
        viewModelScope.launch {
            try {
                repository.reserve(id,holder)
                selectedEvent = null;tab = "Ingressos"
                _catalog.value = repository.load()
            } catch(e: Exception) { error.value = e.message ?: "Falha ao reservar" }
            finally { busy.value = false }
        }
    }
    fun checkIn(id: String) = viewModelScope.launch {
        try {
            if (!repository.checkIn(id)) error.value = "Este ingresso já teve a entrada registrada."
            _catalog.value = repository.load()
        } catch(e: Exception) { error.value = e.message }
    }
    fun reset() = viewModelScope.launch {
        try { repository.reset(); _catalog.value = repository.load() } catch(e: Exception) { error.value = e.message }
    }
    fun visibleEvents(): List<Event> = EventSearch.filter(_catalog.value?.events.orEmpty(), query, category)
        .filter { !onlyFavorites || it.id in _catalog.value!!.favorites }
    fun discover(): Event? = _catalog.value?.events?.randomOrNull()
    override fun onCleared() { repository.close() }
}
