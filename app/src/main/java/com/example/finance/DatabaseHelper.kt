package com.example.finance

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "finance.db"
        private const val DATABASE_VERSION = 1

        // Таблица пользователей
        const val TABLE_USERS = "users"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_USERNAME = "username"
        const val COLUMN_PASSWORD = "password"

        // Таблица транзакций
        const val TABLE_TRANSACTIONS = "transactions"
        const val COLUMN_TRANSACTION_ID = "transaction_id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_AMOUNT = "amount"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_TYPE = "type"
        const val COLUMN_DATE = "date"


        // Таблица категорий
        const val TABLE_CATEGORIES = "categories"
        const val COLUMN_CATEGORY_ID = "category_id"
        const val COLUMN_CATEGORY_NAME = "name"
        const val COLUMN_CATEGORY_TYPE = "category_type"
        const val COLUMN_ICON_RES_ID = "icon_res_id"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Создаем таблицу пользователей
        val createUserTable = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL
            )
        """.trimIndent()

        // Создаем таблицу категорий
        val createCategoryTable = """
            CREATE TABLE $TABLE_CATEGORIES (
                $COLUMN_CATEGORY_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_CATEGORY_NAME TEXT NOT NULL,
                $COLUMN_CATEGORY_TYPE TEXT NOT NULL,
                $COLUMN_ICON_RES_ID INTEGER NOT NULL
            )
        """.trimIndent()

        // Создаем таблицу транзакций
        val createTransactionTable = """
            CREATE TABLE $TABLE_TRANSACTIONS (
                $COLUMN_TRANSACTION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_AMOUNT REAL NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_ICON_RES_ID INTEGER NOT NULL DEFAULT ${R.drawable.ic_other}
            )
        """.trimIndent()

        db.execSQL(createUserTable)
        db.execSQL(createCategoryTable)
        db.execSQL(createTransactionTable)

        // Добавляем начальные категории
        insertInitialCategories(db)
    }

    private fun insertInitialCategories(db: SQLiteDatabase) {
        val initialCategories = listOf(
            // Доходы
            Category(1, "Зарплата", "income", R.drawable.ic_salary),
            Category(2, "Подарок", "income", R.drawable.ic_gift),

            // Расходы
            Category(3, "Продукты", "expense", R.drawable.ic_food),
            Category(4, "Кафе", "expense", R.drawable.ic_cafe),
            Category(5, "Транспорт", "expense", R.drawable.ic_transport),
            Category(6, "Развлечения", "expense", R.drawable.ic_entertainment)
        )

        initialCategories.forEach { category ->
            val values = ContentValues().apply {
                put(COLUMN_CATEGORY_NAME, category.name)
                put(COLUMN_CATEGORY_TYPE, category.type)
                put(COLUMN_ICON_RES_ID, category.iconResId)
            }
            db.insert(TABLE_CATEGORIES, null, values)
        }
    }

    fun isCategoryExists(name: String, type: String): Boolean {
        val db = readableDatabase
        val query = """
        SELECT * FROM $TABLE_CATEGORIES 
        WHERE $COLUMN_CATEGORY_NAME = ? 
        AND $COLUMN_CATEGORY_TYPE = ?
    """.trimIndent()

        return db.rawQuery(query, arrayOf(name, type)).use { it.count > 0 }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

    // Методы для работы с пользователями
    fun registerUser(username: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_PASSWORD, password.hash()) // Хешируем пароль
        }

        return try {
            db.insert(TABLE_USERS, null, values)
        } catch (e: SQLiteConstraintException) {
            -1 // Логин уже существует
        }
    }

    fun loginUser(username: String, password: String): Boolean {
        val db = readableDatabase
        val selection = "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?"
        val selectionArgs = arrayOf(username, password.hash())

        return db.query(
            TABLE_USERS,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        ).use { cursor ->
            cursor.count > 0
        }
    }

    fun isUsernameTaken(username: String): Boolean {
        val db = readableDatabase
        val query = """
            SELECT * FROM $TABLE_USERS 
            WHERE $COLUMN_USERNAME = ?
        """.trimIndent()

        return db.rawQuery(query, arrayOf(username)).use { cursor ->
            cursor.count > 0
        }
    }

    private fun String.hash(): String {
        return this
    }

    // Методы для работы с транзакциями
    fun addTransaction(transaction: Transaction): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, transaction.title)
            put(COLUMN_AMOUNT, transaction.amount)
            put(COLUMN_CATEGORY, transaction.category)
            put(COLUMN_TYPE, transaction.type)
            put(COLUMN_DATE, transaction.date)
            put(COLUMN_ICON_RES_ID, transaction.iconResId)
        }
        return db.insert(TABLE_TRANSACTIONS, null, values)
    }

    fun getTransactions(limit: Int = 10): List<Transaction> {
        val transactions = mutableListOf<Transaction>()
        val db = this.readableDatabase

        val cursor = db.query(
            TABLE_TRANSACTIONS,
            null,
            null,
            null,
            null,
            null,
            "$COLUMN_DATE DESC",
            limit.toString() // Корректный LIMIT
        )

        with(cursor) {
            while (moveToNext()) {
                val transaction = Transaction(
                    id = getInt(getColumnIndexOrThrow(COLUMN_TRANSACTION_ID)),
                    title = getString(getColumnIndexOrThrow(COLUMN_TITLE)),
                    amount = getDouble(getColumnIndexOrThrow(COLUMN_AMOUNT)),
                    category = getString(getColumnIndexOrThrow(COLUMN_CATEGORY)),
                    type = getString(getColumnIndexOrThrow(COLUMN_TYPE)),
                    date = getString(getColumnIndexOrThrow(COLUMN_DATE)),
                    iconResId = getInt(getColumnIndexOrThrow(COLUMN_ICON_RES_ID)) // Новое поле
                )
                transactions.add(transaction)
            }
            close()
        }
        return transactions
    }

    // Методы для работы с категориями
    fun getCategoriesByType(type: String): List<Category> {
        val categories = mutableListOf<Category>()
        val db = this.readableDatabase
        val selection = "$COLUMN_CATEGORY_TYPE = ?"
        val selectionArgs = arrayOf(type)
        val cursor = db.query(
            TABLE_CATEGORIES,
            null,
            selection,
            selectionArgs,
            null,
            null,
            COLUMN_CATEGORY_NAME
        )

        with(cursor) {
            while (moveToNext()) {
                val category = Category(
                    id = getInt(getColumnIndexOrThrow(COLUMN_CATEGORY_ID)),
                    name = getString(getColumnIndexOrThrow(COLUMN_CATEGORY_NAME)),
                    type = getString(getColumnIndexOrThrow(COLUMN_CATEGORY_TYPE)),
                    iconResId = getInt(getColumnIndexOrThrow(COLUMN_ICON_RES_ID))
                )
                categories.add(category)
            }
        }
        cursor.close()
        return categories
    }

    fun addCategory(category: Category): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_CATEGORY_NAME, category.name)
            put(COLUMN_CATEGORY_TYPE, category.type)
            put(COLUMN_ICON_RES_ID, category.iconResId)
        }
        return db.insert(TABLE_CATEGORIES, null, values)
    }
}