package io.cyrilfind.kodo2

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TasksRepository {
    private val tasksWebService = ServiceLocator.tasksWebService

    private val _taskList = MutableStateFlow<List<Task>>(value = emptyList())
    public val taskList: StateFlow<List<Task>> = _taskList.asStateFlow()

    suspend fun refresh() {
        // Call HTTP (opération longue):
        val tasksResponse = tasksWebService.getTasks()
        if (!tasksResponse.isSuccessful) {
            Log.e("TasksRepository", "Error while fetching tasks: ${tasksResponse.message()}")
            return
        }
        val fetchedTasks = tasksResponse.body()
        // on modifie la valeur encapsulée, ce qui va notifier ses Observers et donc déclencher leur callback
        if (fetchedTasks != null) _taskList.value = fetchedTasks
    }

    suspend fun createOrUpdateTask(task: Task) {
        val oldTask = taskList.value.firstOrNull { it.id == task.id }
        val response = when {
            oldTask != null -> tasksWebService.updateTask(task)
            else -> tasksWebService.createTask(task)
        }
        if (response.isSuccessful) {
            val updatedTask = response.body()!!
            if (oldTask != null) _taskList.value = taskList.value - oldTask
            _taskList.value = taskList.value + updatedTask
        } else {
            Log.e("TasksRepository", "Error while creating or updating task: ${response.message()}")
        }
    }
}

