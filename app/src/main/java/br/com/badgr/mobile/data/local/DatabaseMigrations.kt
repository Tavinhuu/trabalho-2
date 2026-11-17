package br.com.badgr.mobile.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Preserva os dados da versão SQLiteOpenHelper (v1) ao adotar Room (v2). */
object DatabaseMigrations {
    val FROM_1_TO_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Cópias temporárias permitem reconstruir constraints sem perder reservas.
            db.execSQL("CREATE TABLE events_backup AS SELECT * FROM events")
            db.execSQL("CREATE TABLE favorites_backup AS SELECT * FROM favorites")
            db.execSQL("CREATE TABLE tickets_backup AS SELECT * FROM tickets")
            db.execSQL("DROP TABLE favorites")
            db.execSQL("DROP TABLE tickets")
            db.execSQL("DROP TABLE events")
            db.execSQL("CREATE TABLE events(id INTEGER NOT NULL PRIMARY KEY, title TEXT NOT NULL, date TEXT NOT NULL, venue TEXT NOT NULL, city TEXT NOT NULL, category TEXT NOT NULL, price INTEGER NOT NULL, capacity INTEGER NOT NULL, lat REAL NOT NULL, lon REAL NOT NULL, description TEXT NOT NULL, color INTEGER NOT NULL)")
            db.execSQL("CREATE TABLE favorites(event_id INTEGER NOT NULL PRIMARY KEY, FOREIGN KEY(event_id) REFERENCES events(id) ON UPDATE NO ACTION ON DELETE CASCADE)")
            db.execSQL("CREATE TABLE tickets(id TEXT NOT NULL PRIMARY KEY, event_id INTEGER NOT NULL, holder TEXT NOT NULL, total INTEGER NOT NULL, created_at INTEGER NOT NULL, checked_in INTEGER NOT NULL DEFAULT 0, FOREIGN KEY(event_id) REFERENCES events(id) ON UPDATE NO ACTION ON DELETE NO ACTION)")
            db.execSQL("CREATE INDEX tickets_event ON tickets(event_id)")
            db.execSQL("INSERT INTO events SELECT * FROM events_backup")
            db.execSQL("INSERT INTO favorites SELECT * FROM favorites_backup")
            db.execSQL("INSERT INTO tickets SELECT * FROM tickets_backup")
            db.execSQL("DROP TABLE events_backup")
            db.execSQL("DROP TABLE favorites_backup")
            db.execSQL("DROP TABLE tickets_backup")
        }
    }
}
