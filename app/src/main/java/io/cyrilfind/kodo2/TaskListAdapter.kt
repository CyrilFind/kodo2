package io.cyrilfind.kodo2

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.cyrilfind.kodo2.databinding.TaskItemBinding

interface TaskListListener {
    fun onClickEdit(task: Task)
    fun onClickRemove(task: Task)
}
    
class TaskListAdapter(val listener: TaskListListener) :
    ListAdapter<Task, TaskListAdapter.TaskViewHolder>(object : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) =
            oldItem == newItem
    }) {
    
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): TaskViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context))
        return TaskViewHolder(binding)
    }
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(val binding: TaskItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Task) {
            binding.taskItemTitle.text = item.title
            binding.taskItemDescription.text = item.description
            binding.root.setOnClickListener { listener.onClickEdit(item) }
            binding.root.setOnLongClickListener { 
                listener.onClickRemove(item) 
                true
            }
        }
    }

}