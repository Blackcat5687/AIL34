package com.notekeep.local.ui

import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.notekeep.local.R
import com.notekeep.local.data.AppDatabase
import com.notekeep.local.data.Note
import com.notekeep.local.databinding.ActivityNoteEditBinding
import kotlinx.coroutines.launch

class NoteEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNoteEditBinding
    private var noteId: Long = -1
    private var currentNote: Note? = null
    private var selectedColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNoteEditBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { saveAndFinish() }

        noteId = intent.getLongExtra(EXTRA_NOTE_ID, -1)
        buildColorRow()

        if (noteId != -1L) {
            lifecycleScope.launch {
                val note = AppDatabase.getInstance(applicationContext).noteDao().getById(noteId)
                if (note != null) {
                    currentNote = note
                    selectedColor = note.color
                    binding.editTitle.setText(note.title)
                    binding.editContent.setText(note.content)
                    highlightSelectedColor()
                    invalidateOptionsMenu()
                }
            }
        }
    }

    private fun buildColorRow() {
        binding.colorRow.removeAllViews()
        val size = (34 * resources.displayMetrics.density).toInt()
        val margin = (8 * resources.displayMetrics.density).toInt()

        NoteColors.palette.forEachIndexed { index, colorRes ->
            val circle = View(this)
            val params = android.widget.LinearLayout.LayoutParams(size, size)
            params.marginEnd = margin
            circle.layoutParams = params
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(ContextCompat.getColor(this, colorRes))
            if (index == selectedColor) {
                drawable.setStroke((2 * resources.displayMetrics.density).toInt(), ContextCompat.getColor(this, R.color.white))
            }
            circle.background = drawable
            circle.tag = index
            circle.setOnClickListener {
                selectedColor = index
                highlightSelectedColor()
            }
            binding.colorRow.addView(circle)
        }
    }

    private fun highlightSelectedColor() {
        for (i in 0 until binding.colorRow.childCount) {
            val child = binding.colorRow.getChildAt(i)
            val index = child.tag as Int
            val drawable = GradientDrawable()
            drawable.shape = GradientDrawable.OVAL
            drawable.setColor(ContextCompat.getColor(this, NoteColors.palette[index]))
            if (index == selectedColor) {
                drawable.setStroke((2 * resources.displayMetrics.density).toInt(), ContextCompat.getColor(this, R.color.white))
            }
            child.background = drawable
        }
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu): Boolean {
        if (noteId != -1L) {
            menuInflater.inflate(R.menu.menu_note_edit, menu)
        }
        return true
    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        if (item.itemId == R.id.action_delete) {
            deleteAndFinish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        saveAndFinish()
    }

    private fun saveAndFinish() {
        val title = binding.editTitle.text.toString().trim()
        val content = binding.editContent.text.toString().trim()

        if (title.isEmpty() && content.isEmpty()) {
            finish()
            return
        }

        lifecycleScope.launch {
            val dao = AppDatabase.getInstance(applicationContext).noteDao()
            val existing = currentNote
            if (existing != null) {
                dao.update(existing.copy(title = title, content = content, color = selectedColor, updatedAt = System.currentTimeMillis()))
            } else {
                dao.insert(Note(title = title, content = content, color = selectedColor))
            }
            finish()
        }
    }

    private fun deleteAndFinish() {
        val existing = currentNote ?: run { finish(); return }
        lifecycleScope.launch {
            AppDatabase.getInstance(applicationContext).noteDao().delete(existing)
            finish()
        }
    }

    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
    }
}
