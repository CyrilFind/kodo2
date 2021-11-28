package io.cyrilfind.kodo2

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import io.cyrilfind.kodo2.databinding.FragmentFormBinding

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FormFragment : Fragment() {

    private lateinit var binding: FragmentFormBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFormBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val task = findNavController().previousBackStackEntry?.savedStateHandle?.get<Task>(TASK_KEY)
        
        binding.editTitle.setText(task?.title ?: "title")
        binding.editDescription.setText(task?.description ?: "description")

        val id = task?.id ?: newUuid()
        binding.validateButton.setOnClickListener {
            val newTitle = binding.editTitle.text.toString()
            val newDescription = binding.editDescription.text.toString()
            val newTask = Task(id = id, title = newTitle, description = newDescription)

            findNavController().previousBackStackEntry?.savedStateHandle?.set(TASK_KEY, newTask)
            findNavController().popBackStack()
        }
    }
}