package hr.trailovix.noteskeeper

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import hr.trailovix.noteskeeper.databinding.ActivityMainBinding
import hr.trailovix.noteskeeper.databinding.MenuTextBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var taskAdapter: TaskAdapter

    private var _menuTextBinding: MenuTextBinding? = null /*for edit task and add new task dialogs*/
    private val menuTextBinding get() = _menuTextBinding!!

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
        taskAdapter.setTasks(DummyDataHolder.tasks)
    }

    private fun addNewTask() {
        fun createTask(taskDescription: String, taskDetails: String) {
            val newTask = Task(taskDescription, taskDetails)
            Toast.makeText(
                this,
                "$taskDescription : $taskDetails  ADD NEW TASK PLACEHOLDER",
                Toast.LENGTH_SHORT
            ).show()
            //todo: create new task, add it to database + add it to RV + notify
        }
        taskTextShowDialog(Task(""), ::createTask, "Create New Task")
    }

    private fun editTask(task: Task) {
        fun updateTask(taskDescription: String, taskDetails: String) {
            Toast.makeText(
                this,
                "$taskDescription : $taskDetails  EDIT TEXT PLACEHOLDER",
                Toast.LENGTH_SHORT
            ).show()
            //todo: database update + RV update + notify
        }
        taskTextShowDialog(task, ::updateTask, "Updating Task Details")
    }

    private fun changeColor(task: Task) {
        val options = arrayOf("Indigo", "Blue", "Green", "Yellow", "Orange", "Red", "Transparent")
        MaterialAlertDialogBuilder(this)
            .setTitle("Select Color")
            .setItems(options) { _, selection ->
                when (selection) {
                    0 -> task.color = Colors.INDIGO
                    1 -> task.color = Colors.BLUE
                    2 -> task.color = Colors.GREEN
                    3 -> task.color = Colors.YELLOW
                    4 -> task.color = Colors.ORANGE
                    5 -> task.color = Colors.RED
                    else -> task.color = Colors.TRANSPARENT
                }
            }
            .show()
        //todo: database update + RV update + notify
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
            .setNegativeButton("Cancel", ){_1, _2->
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
        task.isDone = oldValue.not()
        //todo: database update + RV update + notify
    }

    private fun removeTask(task: Task) {
        //todo: database update + RV update + notify
    }
}