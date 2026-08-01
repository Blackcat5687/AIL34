package com.notekeep.local.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String = "",
    val content: String = "",
    val color: Int = 0,
    val updatedAt: Long = System.currentTimeMillis()
) {
    /** Extracts #hashtags from the title and content, used to build the relationship graph. */
    fun extractTags(): Set<String> {
        val regex = Regex("#[\\p{L}0-9_]+")
        val found = LinkedHashSet<String>()
        regex.findAll(title).forEach { found.add(it.value) }
        regex.findAll(content).forEach { found.add(it.value) }
        return found
    }
}
