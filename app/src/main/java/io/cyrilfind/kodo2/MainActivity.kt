package io.cyrilfind.kodo2

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.cyrilfind.kodo2.databinding.ActivityMainBinding
import io.cyrilfind.kodo2.databinding.TaskItemBinding
import java.io.Serializable
import java.util.*

fun newUuid() = UUID.randomUUID().toString()

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
        }
    }

}


data class Task(val id: String, val title: String, val description: String) : Serializable

const val TASK_KEY = "task"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

}
