package io.cyrilfind.kodo2

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import io.cyrilfind.kodo2.databinding.FragmentListBinding
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class ListFragment : Fragment() {
    private val tasksRepository = ServiceLocator.tasksRepository

    private lateinit var binding: FragmentListBinding

    private val adapter = TaskListAdapter(object : TaskListListener {
        override fun onClickEdit(task: Task) {
            findNavController().currentBackStackEntry?.savedStateHandle?.set(TASK_KEY, task)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        override fun onClickRemove(task: Task) {
            removeTask(task)
        }
    })

    private fun removeTask(task: Task) {
        adapter.submitList(adapter.currentList.filter { it.id != task.id })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tasksRecyclerView.adapter = adapter

        lifecycleScope.launch {
            tasksRepository.taskList.collect {
                adapter.submitList(it)
            }
        }

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }
        lifecycleScope.launch { tasksRepository.refresh() }
    }

}