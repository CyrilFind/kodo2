package io.cyrilfind.kodo2

import retrofit2.Response
import retrofit2.http.*

interface TasksWebService {
    @GET("tasks")
    suspend fun getTasks(): Response<List<Task>>
    
    @POST("tasks")
    suspend fun createTask(@Body task: Task): Response<Task>

    @PATCH("tasks/{id}")
    suspend fun updateTask(@Body task: Task, @Path("id") id: String? = task.id): Response<Task>

}