package br.com.badgr.mobile

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/** Todas as consultas são chamadas pelo Repository em Dispatchers.IO. */
class BadgrDatabase(context: Context) : SQLiteOpenHelper(context, "badgr-demo.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE events(id INTEGER PRIMARY KEY, title TEXT NOT NULL, date TEXT NOT NULL, venue TEXT NOT NULL, city TEXT NOT NULL, category TEXT NOT NULL, price INTEGER NOT NULL CHECK(price>=0), capacity INTEGER NOT NULL CHECK(capacity>0), lat REAL NOT NULL, lon REAL NOT NULL, description TEXT NOT NULL, color INTEGER NOT NULL)")
        db.execSQL("CREATE TABLE favorites(event_id INTEGER PRIMARY KEY REFERENCES events(id) ON DELETE CASCADE)")
        db.execSQL("CREATE TABLE tickets(id TEXT PRIMARY KEY, event_id INTEGER NOT NULL REFERENCES events(id), holder TEXT NOT NULL, total INTEGER NOT NULL, created_at INTEGER NOT NULL, checked_in INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE INDEX tickets_event ON tickets(event_id)")
        for (e in DemoEvents.all) db.execSQL("INSERT INTO events VALUES(?,?,?,?,?,?,?,?,?,?,?,?)", arrayOf(e.id,e.title,e.date,e.venue,e.city,e.category,e.priceCents,e.capacity,e.latitude,e.longitude,e.description,e.color))
    }
    override fun onConfigure(db: SQLiteDatabase) { db.setForeignKeyConstraintsEnabled(true) }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) { error("Migração não definida") }
}

/** Agenda fictícia. Coordenadas de referência, sem parceria ou evento confirmado. */
object DemoEvents {
    val all = listOf(
        Event(1,"UMBRA","21 NOV · SÁB, 22H","Espaço Aurora","São Paulo, SP","Techno",3500,100,-23.5505,-46.6333,"Uma noite de pistas escuras, encontros e música eletrônica independente. Line-up de demonstração: Luma, Nox e Vértice. Classificação 18 anos. Evento fictício para o trabalho acadêmico.",0xFF8655E9.toInt()),
        Event(2,"CORPO LIVRE","28 NOV · SÁB, 23H","Casa Prisma","São Paulo, SP","House",2500,80,-23.5575,-46.6559,"House, expressão e liberdade na pista. Uma experiência de demonstração inspirada na cena independente LGBTQIA+. Classificação 18 anos. Evento fictício.",0xFFE955AA.toInt()),
        Event(3,"GRAVE ABERTO","05 DEZ · SÁB, 18H","Galpão Frequência","São Paulo, SP","Bass",0,60,-23.5417,-46.6400,"Cultura de sound system, graves e encontro de coletivos. Entrada gratuita com reserva local. Classificação 18 anos. Evento fictício para demonstração.",0xFF55B7A8.toInt()),
        Event(4,"ENTRE FREQUÊNCIAS","12 DEZ · SÁB, 21H","Estúdio Subsolo","São Paulo, SP","Experimental",1000,50,-23.5680,-46.6430,"Escuta, performance e experimentação sonora em uma noite dedicada a artistas independentes. Classificação 18 anos. Evento fictício.",0xFFD58C4E.toInt())
    )
}
