package hr.trailovix.noteskeeper

import android.content.res.ColorStateList
import android.graphics.Paint
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.alpha
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import hr.trailovix.noteskeeper.databinding.ItemTaskBinding
import java.util.*

class TaskAdapter(private val listener: OnTaskItemInteraction) :
    RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private val listRV = mutableListOf<Task>() //data to be shown in RecyclerView

    fun updateTasksList(updatedList: List<Task>) {
        val diffCallback = TasksDiffCallback(listRV, updatedList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)

        listRV.clear()
        listRV.addAll(updatedList)
        diffResult.dispatchUpdatesTo(this)
    }

    fun getVisibleElements(): List<Task> = listRV

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(listRV[position])
    }

    override fun getItemCount(): Int {
        return listRV.size
    }

    inner class TaskViewHolder(private val itemTaskBinding: ItemTaskBinding) :
        RecyclerView.ViewHolder(itemTaskBinding.root) {
        init {
            itemTaskBinding.ibMore.setOnClickListener {
                listener.onMenuClick(listRV[layoutPosition])
            }
            itemTaskBinding.ivDone.setOnClickListener {
                listener.onCheckboxClick(listRV[layoutPosition])
            }
            itemTaskBinding.tvTaskDescription.setOnLongClickListener {
                listener.onTextLongClick(listRV[layoutPosition])
                true
            }
        }

        fun bind(task: Task) {
            itemTaskBinding.tvTaskDescription.text = task.taskDescription
            if (task.isDone) {
                itemTaskBinding.ivDone.setImageResource(R.drawable.ic_done)
                val paintFlagsPrevious = itemTaskBinding.tvTaskDescription.paintFlags
                itemTaskBinding.tvTaskDescription.paintFlags =
                    paintFlagsPrevious or Paint.STRIKE_THRU_TEXT_FLAG
                itemTaskBinding.cardView.setCardBackgroundColor(ColorStateList.valueOf(task.color.value or 0x77000000))
            } else {
                itemTaskBinding.ivDone.setImageResource(R.drawable.ic_undone)
                val paintFlagsPrevious = itemTaskBinding.tvTaskDescription.paintFlags
                itemTaskBinding.tvTaskDescription.paintFlags =
                    paintFlagsPrevious and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                itemTaskBinding.cardView.setCardBackgroundColor(ColorStateList.valueOf(task.color.value))
            }
            if (task.taskDetails.isNotBlank()) {
                itemTaskBinding.tvTaskDetails.visibility = View.VISIBLE
                itemTaskBinding.tvTaskDetails.text = task.taskDetails
            } else {
                itemTaskBinding.tvTaskDetails.visibility = View.GONE
            }
            val now = Date().time
            itemTaskBinding.tvTimeCreated.text = "Created: ${DateUtils.getRelativeTimeSpanString(task.created, now, 1L)}"
            itemTaskBinding.tvTimeEdited.text = "Last Change: ${DateUtils.getRelativeTimeSpanString(task.lastEdit, now, 1L)}"
        }
    }
}