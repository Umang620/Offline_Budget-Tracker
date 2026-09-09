package com.example.magtipidka.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.magtipidka.data.local.dao.BudgetDao
import com.example.magtipidka.data.local.dao.CategoryDao
import com.example.magtipidka.data.local.dao.DebtDao
import com.example.magtipidka.data.local.dao.RecurringBillDao
import com.example.magtipidka.data.local.dao.SavingsGoalDao
import com.example.magtipidka.data.local.dao.TransactionDao
import com.example.magtipidka.data.local.entity.BudgetEntity
import com.example.magtipidka.data.local.entity.CategoryEntity
import com.example.magtipidka.data.local.entity.DebtEntity
import com.example.magtipidka.data.local.entity.RecurringBillEntity
import com.example.magtipidka.data.local.entity.SavingsGoalEntity
import com.example.magtipidka.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        DebtEntity::class,
        RecurringBillEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun debtDao(): DebtDao
    abstract fun recurringBillDao(): RecurringBillDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_categories_name_type` ON `categories` (`name`, `type`)")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE categories ADD COLUMN colorHex TEXT NOT NULL DEFAULT '#00523B'")
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `debts` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `personName` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `paidAmount` REAL NOT NULL DEFAULT 0.0,
                        `type` TEXT NOT NULL,
                        `dueDate` INTEGER NOT NULL,
                        `isSettled` INTEGER NOT NULL DEFAULT 0,
                        `note` TEXT NOT NULL DEFAULT '',
                        `createdAt` INTEGER NOT NULL
                    )
                """)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `recurring_bills` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `title` TEXT NOT NULL,
                        `amount` REAL NOT NULL,
                        `categoryId` INTEGER NOT NULL,
                        `frequency` TEXT NOT NULL,
                        `nextDueDate` INTEGER NOT NULL,
                        `isAutoAdd` INTEGER NOT NULL DEFAULT 1,
                        `createdAt` INTEGER NOT NULL
                    )
                """)
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE transactions ADD COLUMN receiptImageUri TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE recurring_bills ADD COLUMN lastProcessedDate INTEGER NOT NULL DEFAULT 0")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mag_tipid_ka_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                getDatabase(context).categoryDao().insertCategories(DefaultCategories.list)
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
