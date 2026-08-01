package com.notekeep.local.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.notekeep.local.R
import com.notekeep.local.data.Note
import com.notekeep.local.databinding.ItemNoteBinding

class NoteAdapter(
    private val onClick: (Note) -> Unit
) : ListAdapter<Note, NoteAdapter.NoteViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(private val binding: ItemNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.textTitle.text = note.title
            binding.textContent.text = note.content
            binding.textTitle.visibility = if (note.title.isBlank()) android.view.View.GONE else android.view.View.VISIBLE

            val colorRes = NoteColors.palette.getOrElse(note.color) { R.color.note_0 }
            binding.cardRoot.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(binding.root.context, colorRes)
            )

            binding.root.setOnClickListener { onClick(note) }
        }
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Note>() {
            override fun areItemsTheSame(oldItem: Note, newItem: Note) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: Note, newItem: Note) = oldItem == newItem
        }
    }
}
