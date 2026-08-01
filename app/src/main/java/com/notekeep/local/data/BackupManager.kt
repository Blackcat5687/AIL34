package com.notekeep.local.data

import org.json.JSONArray
import org.json.JSONObject

object BackupManager {

    fun toJson(notes: List<Note>): String {
        val array = JSONArray()
        for (note in notes) {
            val obj = JSONObject()
            obj.put("title", note.title)
            obj.put("content", note.content)
            obj.put("color", note.color)
            obj.put("updatedAt", note.updatedAt)
            array.put(obj)
        }
        val root = JSONObject()
        root.put("app", "NotesLink")
        root.put("version", 1)
        root.put("notes", array)
        return root.toString()
    }

    /** Parses a backup file. Ignores original ids so imported notes get fresh ones. */
    fun fromJson(json: String): List<Note> {
        val root = JSONObject(json)
        val array = root.optJSONArray("notes") ?: JSONArray(json)
        val notes = ArrayList<Note>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            notes.add(
                Note(
                    title = obj.optString("title", ""),
                    content = obj.optString("content", ""),
                    color = obj.optInt("color", 0),
                    updatedAt = obj.optLong("updatedAt", System.currentTimeMillis())
                )
            )
        }
        return notes
    }
}
