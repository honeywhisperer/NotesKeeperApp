package hr.trailovix.noteskeeper

import java.util.*

data class Task(
    var taskDescription: String,
    var taskDetails: String = "",
    var color: Colors = Colors.TRANSPARENT,
    var isDone: Boolean = false,
    var lastEdit: Long = Date().time,
    val created: Long = Date().time,
    val uuid: UUID = UUID.randomUUID()
)
