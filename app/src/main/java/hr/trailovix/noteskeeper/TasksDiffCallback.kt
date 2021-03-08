package hr.trailovix.noteskeeper

import androidx.recyclerview.widget.DiffUtil

class TasksDiffCallback(private val oldListRV: List<Task>, private val newListDB: List<Task>) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldListRV.size
    }

    override fun getNewListSize(): Int {
        return newListDB.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldListRV[oldItemPosition].uuid == newListDB[newItemPosition].uuid
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldListRV[oldItemPosition] == newListDB[newItemPosition]
    }
}