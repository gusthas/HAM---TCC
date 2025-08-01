// Substitua o conteúdo completo de AppDatabase.kt
package com.apol.myapplication

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.apol.myapplication.data.model.DivisaoTreino
import com.apol.myapplication.data.model.Exercicio
import com.apol.myapplication.data.model.TreinoEntity


// 1. ADICIONE AS NOVAS ENTIDADES E MUDE A VERSÃO PARA 3
@Database(entities = [User::class, TreinoEntity::class, DivisaoTreino::class, Exercicio::class], version = 4)
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
                    "usuario_database"
                )
                    // 3. ADICIONE A NOVA MIGRAÇÃO AQUI
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Migração de 1 para 2 (você já tinha)
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN idade INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN peso INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN altura REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE users ADD COLUMN genero TEXT NOT NULL DEFAULT ''")
            }
        }
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE treinos ADD COLUMN detalhes TEXT NOT NULL DEFAULT 'Toque para adicionar detalhes'")
            }
        }

        // 3. CRIE A NOVA MIGRAÇÃO DE 2 PARA 3
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Cria a tabela de treinos
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `treinos` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `nome` TEXT NOT NULL, 
                        `iconeResId` INTEGER NOT NULL, 
                        `tipoDivisao` TEXT NOT NULL DEFAULT 'NAO_DEFINIDO'
                    )
                """)

                // Cria a tabela de divisões de treino
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `divisoes_treino` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `treinoId` INTEGER NOT NULL, 
                        `nome` TEXT NOT NULL, 
                        `ordem` INTEGER NOT NULL,
                        FOREIGN KEY(`treinoId`) REFERENCES `treinos`(`id`) ON DELETE CASCADE
                    )
                """)

                // Cria a tabela de exercícios
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS `exercicios` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, 
                        `divisaoId` INTEGER NOT NULL, 
                        `nome` TEXT NOT NULL, 
                        `carga` TEXT NOT NULL, 
                        `series` TEXT NOT NULL, 
                        `repeticoes` TEXT NOT NULL,
                        FOREIGN KEY(`divisaoId`) REFERENCES `divisoes_treino`(`id`) ON DELETE CASCADE
                    )
                """)
            }
        }
    }
}