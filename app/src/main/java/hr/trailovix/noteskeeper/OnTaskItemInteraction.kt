package hr.trailovix.noteskeeper

interface OnTaskItemInteraction {
    fun onMenuClick(task: Task)
    fun onCheckboxClick(task: Task)
    fun onTextLongClick(task: Task)
}