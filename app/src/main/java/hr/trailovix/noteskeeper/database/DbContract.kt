package hr.trailovix.noteskeeper.database

import android.content.ContentUris
import android.net.Uri
import android.provider.BaseColumns

object DbContract {
    internal const val TABLE_NAME = "Tasks"
    /*URI to access the Tasks table*/
    val CONTENT_URI: Uri = Uri.withAppendedPath(CONTENT_AUTHORITY_URI, TABLE_NAME)

    const val CONTENT_TYPE = "vnd.android.cursor.dir/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"
    const val CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.$CONTENT_AUTHORITY.$TABLE_NAME"

    object Columns{
        const val ID = BaseColumns._ID
        const val TASK_DESCRIPTION = "TaskDescription"
        const val TASK_DETAILS = "TaskDetails"
        const val TASK_DONE = "IsDone"
        const val TASK_COLOR = "TaskColor"
        const val TASK_CREATED = "TaskCreated"
        const val TASK_LAST_EDIT = "TaskLastEdit"
        const val TASK_UUID = "TaskUuid"
    }

    fun getId(uri: Uri): Long = ContentUris.parseId(uri)

    fun buildUriFromId(id: Long): Uri = ContentUris.withAppendedId(CONTENT_URI, id)
}