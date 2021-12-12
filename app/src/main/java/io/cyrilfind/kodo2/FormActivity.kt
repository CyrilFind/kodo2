package io.cyrilfind.kodo2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.cyrilfind.kodo2.databinding.ActivityFormBinding

class FormActivity : AppCompatActivity() {
    lateinit var binding: ActivityFormBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val task = intent.getSerializableExtra(TASK_KEY) as? Task
        binding.editTitle.setText(task?.title ?: "title")
        binding.editDescription.setText(task?.description ?: "description")
        
        val id = task?.id ?: newUuid()
        binding.validateButton.setOnClickListener {
            val newTitle = binding.editTitle.text.toString()
            val newDescription = binding.editDescription.text.toString()
            val newTask = Task(id = id, title = newTitle, description = newDescription)

            intent.putExtra(TASK_KEY, newTask)
            setResult(RESULT_OK, intent)
            finish()
        }
    }
}