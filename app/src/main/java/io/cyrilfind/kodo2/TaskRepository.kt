package io.cyrilfind.kodo2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TasksRepository {
    private val tasksWebService = Api.tasksWebService

    private val _taskList = MutableStateFlow<List<Task>>(value = emptyList())
    public val taskList: StateFlow<List<Task>> = _taskList.asStateFlow()

    suspend fun refresh() {
        // Call HTTP (opération longue):
        val tasksResponse = tasksWebService.getTasks()
        // À la ligne suivante, on a reçu la réponse de l'API:
        if (tasksResponse.isSuccessful) {
            val fetchedTasks = tasksResponse.body()
            // on modifie la valeur encapsulée, ce qui va notifier ses Observers et donc déclencher leur callback
            if (fetchedTasks != null) _taskList.value = fetchedTasks
        }
    }

    suspend fun updateTask(task: Task) {
        val response = tasksWebService.updateTask(task)
        if (response.isSuccessful) {
            val updatedTask: Task = response.body()!!
            val oldTask = taskList.value.firstOrNull { it.id == updatedTask.id }
            if (oldTask != null) _taskList.value = taskList.value - oldTask + updatedTask
        }
    }
    
    suspend fun createTask(task: Task) {
        val response = tasksWebService.createTask(task)
        if (response.isSuccessful) {
            val newTask: Task = response.body()!!
            _taskList.value = taskList.value + newTask
        }
    }
}

