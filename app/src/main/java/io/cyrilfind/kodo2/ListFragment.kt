package io.cyrilfind.kodo2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import io.cyrilfind.kodo2.databinding.FragmentListBinding
import java.io.Serializable

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListFragment : Fragment() {
    private var tasks: List<Task> = listOf(Task(newUuid(), "Task 1", "Description 1"))

    private lateinit var binding: FragmentListBinding

    private val adapter = TaskListAdapter(object : TaskListListener {
        override fun onClickEdit(task: Task) {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(TASK_KEY, task)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        override fun onClickRemove(task: Task) {
            TODO("Not yet implemented")
        }

    })

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val previousList = savedInstanceState?.getSerializable("tasks") as? List<Task>
        if (previousList != null) tasks = previousList

        binding.tasksRecyclerView.adapter = adapter
        adapter.submitList(tasks)
        findNavController().currentBackStackEntry?.savedStateHandle
            ?.getLiveData<Task>(TASK_KEY)
            ?.observe(viewLifecycleOwner) { newTask ->
                val oldTask = tasks.firstOrNull { it.id == newTask.id }
                if (oldTask != null) tasks = tasks - oldTask
                tasks = tasks + newTask
                adapter.submitList(tasks)
                findNavController().currentBackStackEntry?.savedStateHandle?.remove<Task>(TASK_KEY)
            }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable("tasks", tasks as Serializable)
    }

}