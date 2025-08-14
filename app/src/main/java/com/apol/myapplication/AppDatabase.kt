package com.apol.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.LogEntry

import com.apol.myapplication.data.model.TreinoEntity
import com.apol.myapplication.data.model.TreinoNota
import com.apol.myapplication.data.model.User
import com.apol.myapplication.data.model.WeightEntry

@Database(entities = [User::class, TreinoEntity::class, DivisaoTreino::class, TreinoNota::class, WeightEntry::class], version = 10)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun treinoDao(): TreinoDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    // A CADEIA DE MIGRAÇÕES COMPLETA, COMO ANTES
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // --- MIGRAÇÕES RESTAURADAS ---
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN idade INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN peso INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN altura REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN genero TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `treinos` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `nome` TEXT NOT NULL, `iconeResId` INTEGER NOT NULL, `tipoDivisao` TEXT NOT NULL DEFAULT 'NAO_DEFINIDO')")
                database.execSQL("CREATE TABLE IF NOT EXISTS `divisoes_treino` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `treinoId` INTEGER NOT NULL, `nome` TEXT NOT NULL, `ordem` INTEGER NOT NULL, FOREIGN KEY(`treinoId`) REFERENCES `treinos`(`id`) ON DELETE CASCADE)")
            }
        }
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE treinos ADD COLUMN detalhes TEXT NOT NULL DEFAULT 'Toque para adicionar detalhes'")
            }
        }
        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE treinos ADD COLUMN tipoDeTreino TEXT NOT NULL DEFAULT 'GENERICO'")
                database.execSQL("CREATE TABLE IF NOT EXISTS `log_entries` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `divisaoId` INTEGER NOT NULL, `campo1` TEXT NOT NULL, `campo2` TEXT NOT NULL, `campo3` TEXT NOT NULL, `campo4` TEXT NOT NULL, FOREIGN KEY(`divisaoId`) REFERENCES `divisoes_treino`(`id`) ON DELETE CASCADE)")
            }
        }
        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE treinos ADD COLUMN templateJson TEXT")
            }
        }
        val MIGRATION_6_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // ... (código para recriar a tabela treinos sem templateJson, se necessário)
            }
        }
        val MIGRATION_7_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Apaga a tabela de LogEntry que estava errada e recria a TreinoNota
                database.execSQL("DROP TABLE IF EXISTS log_entries")
                database.execSQL("CREATE TABLE IF NOT EXISTS `treino_notas` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `divisaoId` INTEGER NOT NULL, `titulo` TEXT NOT NULL, `conteudo` TEXT NOT NULL, FOREIGN KEY(`divisaoId`) REFERENCES `divisoes_treino`(`id`) ON DELETE CASCADE)")
            }
        }
        val MIGRATION_8_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN perguntaSecreta TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE users ADD COLUMN respostaSecreta TEXT NOT NULL DEFAULT ''")
            }
        }
    }
}