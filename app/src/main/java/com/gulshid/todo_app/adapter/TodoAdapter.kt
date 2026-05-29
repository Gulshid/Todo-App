package com.gulshid.todo_app.adapter

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gulshid.todo_app.R
import com.gulshid.todo_app.data.Todo

/**
 * RecyclerView Adapter using ListAdapter + DiffUtil for smooth animations.
 * Handles: check/uncheck, edit click, delete click, priority color.
 */
class TodoAdapter(
    private val onToggle: (Todo) -> Unit,
    private val onEdit: (Todo) -> Unit,
    private val onDelete: (Todo) -> Unit
) : ListAdapter<Todo, TodoAdapter.TodoViewHolder>(DiffCallback()) {

    private var lastPosition = -1

    inner class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: CardView       = view.findViewById(R.id.todoCard)
        val checkbox: CheckBox   = view.findViewById(R.id.cbComplete)
        val tvTitle: TextView    = view.findViewById(R.id.tvTodoTitle)
        val tvDesc: TextView     = view.findViewById(R.id.tvTodoDesc)
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val priorityBar: View    = view.findViewById(R.id.priorityBar)
        val btnEdit: ImageButton = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val todo = getItem(position)
        val ctx = holder.itemView.context

        // Title + strikethrough if completed
        holder.tvTitle.text = todo.title
        if (todo.isCompleted) {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.tvTitle.alpha = 0.5f
        } else {
            holder.tvTitle.paintFlags = holder.tvTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.tvTitle.alpha = 1f
        }

        // Description
        if (todo.description.isNotEmpty()) {
            holder.tvDesc.visibility = View.VISIBLE
            holder.tvDesc.text = todo.description
        } else {
            holder.tvDesc.visibility = View.GONE
        }

        // Category chip
        holder.tvCategory.text = todo.category

        // Priority color bar
        val priorityColor = when (todo.priority) {
            2    -> ContextCompat.getColor(ctx, R.color.priority_high)
            1    -> ContextCompat.getColor(ctx, R.color.priority_medium)
            else -> ContextCompat.getColor(ctx, R.color.priority_low)
        }
        holder.priorityBar.setBackgroundColor(priorityColor)

        // Card alpha when completed
        holder.card.alpha = if (todo.isCompleted) 0.7f else 1f

        // Checkbox state (no infinite loop — set listener after state)
        holder.checkbox.setOnCheckedChangeListener(null)
        holder.checkbox.isChecked = todo.isCompleted
        holder.checkbox.setOnCheckedChangeListener { _, _ -> onToggle(todo) }

        // Buttons
        holder.btnEdit.setOnClickListener { onEdit(todo) }
        holder.btnDelete.setOnClickListener {
            // Delete animation
            holder.itemView.animate()
                .translationX(holder.itemView.width.toFloat())
                .alpha(0f)
                .setDuration(250)
                .withEndAction { onDelete(todo) }
                .start()
        }

        // Staggered slide-in animation
        setAnimation(holder.itemView, position)
    }

    private fun setAnimation(view: View, position: Int) {
        if (position > lastPosition) {
            val anim = AnimationUtils.loadAnimation(view.context, R.anim.slide_in_up)
            anim.startOffset = (position * 50L).coerceAtMost(300L)
            view.startAnimation(anim)
            lastPosition = position
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Todo>() {
        override fun areItemsTheSame(a: Todo, b: Todo) = a.id == b.id
        override fun areContentsTheSame(a: Todo, b: Todo) = a == b
    }
}