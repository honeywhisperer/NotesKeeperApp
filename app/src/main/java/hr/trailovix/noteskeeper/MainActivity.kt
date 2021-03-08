package hr.trailovix.noteskeeper

import android.content.ContentValues
import android.database.Cursor
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hr.trailovix.noteskeeper.database.DbContract
import hr.trailovix.noteskeeper.databinding.ActivityMainBinding
import hr.trailovix.noteskeeper.databinding.MenuTextBinding
import java.lang.Exception
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter

    private var _menuTextBinding: MenuTextBinding? = null /*for edit task and add new task dialogs*/
    private val menuTextBinding get() = _menuTextBinding!!

    private val tasksRV = mutableListOf<Task>() //data to be shown in RecyclerView
    private val _tasksDB = mutableListOf<Task>() //data taken from database, used for reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupTasksRV()
        setListeners()
    }

    private fun setListeners() {
        binding.fabAddNew.setOnClickListener {
            addNewTask()
        }
    }

    private fun setupTasksRV() {
        taskAdapter = TaskAdapter(object : OnTaskItemInteraction {
            override fun onMenuClick(task: Task) {
                val options = arrayOf(
                    "Change Color",
                    "Edit Task",
                    if (task.isDone) "Mark as Undone" else "Mark as Done",
                    "Remove Task"
                )
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Task Options")
                    .setItems(options) { _, choice ->
                        when (choice) {
                            0 -> changeColor(task)
                            1 -> editTask(task)
                            2 -> invertDoneFlag(task)
                            3 -> removeTask(task)
                        }
                    }
                    .show()
            }

            override fun onCheckboxClick(task: Task) {
                invertDoneFlag(task)
            }

            override fun onTextLongClick(task: Task) {
                //todo: select + show options to change color, delete selected item(s)
            }

        })
        binding.rvToDo.layoutManager = LinearLayoutManager(this)
        binding.rvToDo.adapter = taskAdapter

        refreshData()
    }

    private fun addNewTask() {
        fun createTask(taskDescription: String, taskDetails: String) {
            val newTask = Task(taskDescription, taskDetails)
            insertTaskInDB(newTask)
            refreshData()
        }

        taskTextShowDialog(Task(""), ::createTask, "Create New Task")
    }

    private fun editTask(task: Task) {
        fun updateTask(taskDescription: String, taskDetails: String) {
            val newTask = task.copy(taskDescription = taskDescription, taskDetails = taskDetails)
            updatedTaskInDB(newTask)
            refreshData()
        }

        taskTextShowDialog(task, ::updateTask, "Updating Task Details")
    }

    private fun changeColor(task: Task) {
        val newTask = task.copy()
        val options = arrayOf("Indigo", "Blue", "Green", "Yellow", "Orange", "Red", "Transparent")
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Color")
            .setItems(options) { _, selection ->
                when (selection) {
                    0 -> newTask.color = Colors.INDIGO
                    1 -> newTask.color = Colors.BLUE
                    2 -> newTask.color = Colors.GREEN
                    3 -> newTask.color = Colors.YELLOW
                    4 -> newTask.color = Colors.ORANGE
                    5 -> newTask.color = Colors.RED
                    else -> newTask.color = Colors.TRANSPARENT
                }
                //database update + RV update + notify
                updatedTaskInDB(newTask)
                refreshData()
            }
            .show()
    }

    private fun taskTextShowDialog(
        task: Task,
        predicate: (String, String) -> Unit,
        dialogTitle: String
    ) {
        _menuTextBinding = MenuTextBinding.inflate(layoutInflater)
        menuTextBinding.tilTaskDescription.editText?.setText(task.taskDescription)
        menuTextBinding.tilTaskDetails.editText?.setText(task.taskDetails)
        menuTextBinding.tilTaskDescription.editText?.selectAll()

        MaterialAlertDialogBuilder(this)
            .setView(menuTextBinding.root)
            .setTitle(dialogTitle)
            .setNegativeButton("Cancel") { _1, _2 ->
                _menuTextBinding = null
            }
            .setPositiveButton("Save") { _1, _2 ->
                val newDescription =
                    menuTextBinding.tilTaskDescription.editText?.text.toString().trim()
                val newDetails = menuTextBinding.tilTaskDetails.editText?.text.toString().trim()

                predicate(newDescription, newDetails)

                _menuTextBinding = null
            }
            .show()
    }

    private fun invertDoneFlag(task: Task) {
        val oldValue = task.isDone
        val newTask = task.copy(isDone = oldValue.not())
        updatedTaskInDB(newTask)
        refreshData()
    }

    private fun removeTask(task: Task) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Remove: ${task.taskDescription} ?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Remove") { _1, _2 ->
                removeTaskFromDB(task)
                refreshData()
            }
            .show()
    }

    private fun getTaskFromCursor(cursor: Cursor): Task {
        cursor.let {
            val taskDescription =
                it.getString(it.getColumnIndex(DbContract.Columns.TASK_DESCRIPTION))
            val taskDetails = it.getString(it.getColumnIndex(DbContract.Columns.TASK_DETAILS))
            val taskColor = Colors.values()
                .first { appColors ->
                    appColors.value == cursor.getInt(cursor.getColumnIndex(DbContract.Columns.TASK_COLOR))
                }
            val taskIsDone = it.getInt(it.getColumnIndex(DbContract.Columns.TASK_DONE)) != 0
            val taskUuid =
                UUID.fromString(it.getString(it.getColumnIndex(DbContract.Columns.TASK_UUID)))

            return Task(taskDescription, taskDetails, taskColor, taskIsDone, taskUuid)

        }
    }

    private fun getContentValues(task: Task): ContentValues {
        return ContentValues().apply {
            put(DbContract.Columns.TASK_DESCRIPTION, task.taskDescription)
            put(DbContract.Columns.TASK_DETAILS, task.taskDetails)
            put(DbContract.Columns.TASK_COLOR, task.color.value)
            put(DbContract.Columns.TASK_DONE, task.isDone)
            put(DbContract.Columns.TASK_UUID, task.uuid.toString())
        }
    }

    /**
     * read data from the DB and store it in _taskDB; compare with RV, update RV and notify RV
     */
    private fun refreshData() {
        val cursor = contentResolver.query(DbContract.CONTENT_URI, null, null, null, null)
        _tasksDB.clear()
        try {
            cursor?.let {
                it.moveToFirst()
                _tasksDB.add(getTaskFromCursor(it))
                while (it.moveToNext()) {
                    _tasksDB.add(getTaskFromCursor(it))
                }
            }
            taskAdapter.updateTasksList(_tasksDB)
            cursor?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updatedTaskInDB(task: Task) {
        val contentValues = getContentValues(task)
        val selection = DbContract.Columns.TASK_UUID + " = ?"
        val arg = arrayOf(task.uuid.toString())
        contentResolver.update(DbContract.CONTENT_URI, contentValues, selection, arg)
    }

    private fun insertTaskInDB(task: Task) {
        val contentValues = getContentValues(task)
        contentResolver.insert(DbContract.CONTENT_URI, contentValues)
    }

    private fun removeTaskFromDB(task: Task) {
        val selection = DbContract.Columns.TASK_UUID + " = ?"
        val arg = arrayOf(task.uuid.toString())
        contentResolver.delete(DbContract.CONTENT_URI, selection, arg)
    }
}