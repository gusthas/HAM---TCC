package com.apol.myapplication
import kotlinx.serialization.Serializable
@Serializable
data class Note(
    val id: Long = System.currentTimeMillis(),
    val text: String,
    val lastModified: Long = System.currentTimeMillis(),
    var isSelected: Boolean = false
)
