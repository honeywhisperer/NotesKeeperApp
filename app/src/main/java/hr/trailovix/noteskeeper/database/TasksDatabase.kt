package hr.trailovix.noteskeeper.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.lang.IllegalStateException

private const val DATABASE_NAME = "TasksTable.db"
private const val DATABASE_VERSION = 1
private const val TAG = "TasksDatabase.kt"

class TasksDatabase private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object : SingletonHolder<TasksDatabase, Context>(::TasksDatabase)

    init {
        Log.d(TAG, "TasksDatabase: init")
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d(TAG, "TasksDatabase: onCreate starts")
        val sqlStatement = """CREATE TABLE ${DbContract.TABLE_NAME} (
            ${DbContract.Columns.ID} INTEGER PRIMARY KEY NOT NULL,
            ${DbContract.Columns.TASK_DESCRIPTION} TEXT NOT NULL,
            ${DbContract.Columns.TASK_DETAILS} TEXT,
            ${DbContract.Columns.TASK_COLOR} INTEGER NOT NULL,
            ${DbContract.Columns.TASK_DONE} INTEGER NOT NULL,
            ${DbContract.Columns.TASK_CREATED} LONG NOT NULL,
            ${DbContract.Columns.TASK_LAST_EDIT} LONG NOT NULL,
            ${DbContract.Columns.TASK_UUID} TEXT NOT NULL);""".replaceIndent(" ")
        Log.d(TAG, "TasksDatabase: onCreate SQL Statement: $sqlStatement")
        db.execSQL(sqlStatement)
        Log.d(TAG, "TasksDatabase: onCreate ends")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "TasksDatabase: onUpgrade starts")
        when (oldVersion) {
            1 -> {
            }
            else -> throw IllegalStateException("Unknown DB version onUpgrade: $oldVersion > $newVersion")
        }
    }
}