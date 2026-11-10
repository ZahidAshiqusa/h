package com.zaidashiq.ultra.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        AutomationRuleEntity::class,
        CommandEntity::class,
        PluginEntity::class,
        ScheduledTaskEntity::class,
        SessionContactEntity::class,
        SessionChatEntity::class,
        StatusPostEntity::class,
        ActivityLogEntity::class,
        MessageTemplateEntity::class,
        WhatsAppSessionMetadataEntity::class,
        CommandUsageLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun ultraDao(): UltraDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zaid_ultra.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
