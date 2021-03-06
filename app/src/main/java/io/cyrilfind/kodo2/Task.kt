package io.cyrilfind.kodo2

import java.io.Serializable

@kotlinx.serialization.Serializable
data class Task(val id: String, val title: String, val description: String) : Serializable