package hr.trailovix.noteskeeper.database

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.SQLException
import android.database.sqlite.SQLiteQueryBuilder
import android.net.Uri
import android.util.Log
import java.lang.IllegalArgumentException

private const val TAG = "TasksProvider.kt"

const val CONTENT_AUTHORITY = "hr.trailovix.tasks.provider"
val CONTENT_AUTHORITY_URI: Uri = Uri.parse("content://$CONTENT_AUTHORITY")

private const val TASKS = 100
private const val TASKS_ID = 101

class TasksProvider : ContentProvider() {

    private val uriMatcher by lazy { buildUriMatcher() }
    private fun buildUriMatcher(): UriMatcher {
        Log.d(TAG, "buildUriMatcher: starts")
        val matcher = UriMatcher(UriMatcher.NO_MATCH)

        //e.g. content://hr.trailovix.tasks.provider/Tasks
        matcher.addURI(CONTENT_AUTHORITY, DbContract.TABLE_NAME, TASKS)
        //e.g. content://hr.trailovix.tasks.provider/Tasks/8
        matcher.addURI(CONTENT_AUTHORITY, "${DbContract.TABLE_NAME}/#", TASKS_ID)
        return matcher
    }

    override fun onCreate(): Boolean {
        Log.d(TAG, "onCreate: starts")
        return true
    }

    override fun query(
        uri: Uri, projection: Array<out String>?, selection: String?,
        selectionArgs: Array<out String>?, sortOrder: String?
    ): Cursor? {
        Log.d(TAG, "query: called with $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "query: match is $match")

        val queryBuilder = SQLiteQueryBuilder()
        when (match) {
            TASKS -> queryBuilder.tables = DbContract.TABLE_NAME
            TASKS_ID -> {
                queryBuilder.tables = DbContract.TABLE_NAME
                val taskId = DbContract.getId(uri)
                queryBuilder.appendWhere("${DbContract.Columns.ID} = ")
                queryBuilder.appendWhereEscapeString("$taskId")
            }
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
        val db = context?.let { TasksDatabase.getInstance(it).readableDatabase }
        val cursor =
            queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder)
        Log.d(TAG, "query: rows in returned cursor = ${cursor.count}")
        return cursor
    }

    override fun getType(uri: Uri): String? {
        Log.d(TAG, "getType: called with $uri")
        val match = uriMatcher.match(uri)
        return when (match) {
            TASKS -> DbContract.CONTENT_TYPE
            TASKS_ID -> DbContract.CONTENT_ITEM_TYPE
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.d(TAG, "insert: called with $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "insert: match is $match")

        val recordId: Long
        val returnUri: Uri

        when (match) {
            TASKS -> {
                val db = context?.let { TasksDatabase.getInstance(it).writableDatabase }
                recordId = db?.insert(DbContract.TABLE_NAME, null, values) ?: 0L
                if (recordId != -1L) {
                    returnUri = DbContract.buildUriFromId(recordId)
                }else{
                    throw SQLException("Failed to insert, uri was $uri")
                }
            }
            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }
        Log.d(TAG, "insert: exiting, returning: $returnUri")
        return returnUri
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        Log.d(TAG, "delete: called with $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "delete: match is $match")

        val count: Int
        var selectionCriteria: String

        when(match){
            TASKS->{
                val db = context?.let { TasksDatabase.getInstance(it).writableDatabase }
                count = db?.delete(DbContract.TABLE_NAME, selection, selectionArgs) ?: 0
            }
            TASKS_ID->{
                val db = context?.let { TasksDatabase.getInstance(it).writableDatabase }
                val id = DbContract.getId(uri)
                selectionCriteria = "${DbContract.Columns.ID} = $id"
                if (selection.isNullOrEmpty().not()){
                    selectionCriteria += " AND ($selection)"
                }
                count = db?.delete(DbContract.TABLE_NAME, selectionCriteria, selectionArgs) ?: 0
            }
            else-> throw IllegalArgumentException("Unknown uri: $uri")
        }
        Log.d(TAG, "delete: exiting, returning $count")
        return count
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        Log.d(TAG, "update: called with $uri")
        val match = uriMatcher.match(uri)
        Log.d(TAG, "update: match is $match")

        val count : Int
        var selectionCriteria: String

        when(match){
            TASKS->{
                val db = context?.let { TasksDatabase.getInstance(it).writableDatabase }
                count = db?.update(DbContract.TABLE_NAME, values, selection, selectionArgs) ?: 0
            }
            TASKS_ID->{
                val db = context?.let { TasksDatabase.getInstance(it).writableDatabase }
                val id = DbContract.getId(uri)
                selectionCriteria = "${DbContract.Columns.ID} = $id"

                if (selectionCriteria.isNullOrEmpty().not()){
                    selectionCriteria += " AND ($selection)"
                }
                count = db?.update(DbContract.TABLE_NAME, values, selectionCriteria, selectionArgs) ?: 0
            }
            else -> throw IllegalArgumentException("Unknown uri: $uri")
        }
        Log.d(TAG, "update: exiting, returning $count")
        return count
    }
}