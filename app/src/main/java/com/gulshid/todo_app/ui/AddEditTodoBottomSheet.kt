package com.gulshid.todo_app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.gulshid.todo_app.R
import com.gulshid.todo_app.data.Todo

/**
 * Beautiful bottom sheet for Add / Edit todo.
 * Supports: title, description, priority selection, category chips.
 */
class AddEditTodoBottomSheet : BottomSheetDialogFragment() {

    private var existingTodo: Todo? = null
    private var onSave: ((Todo) -> Unit)? = null

    private lateinit var etTitle: TextInputEditText
    private lateinit var etDesc: TextInputEditText
    private lateinit var tilTitle: TextInputLayout
    private lateinit var rgPriority: RadioGroup
    private lateinit var cgCategory: ChipGroup
    private lateinit var btnSave: Button
    private lateinit var tvSheetTitle: TextView

    companion object {
        fun newInstance(todo: Todo? = null, onSave: (Todo) -> Unit): AddEditTodoBottomSheet {
            return AddEditTodoBottomSheet().apply {
                this.existingTodo = todo
                this.onSave = onSave
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.bottom_sheet_add_todo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        etTitle     = view.findViewById(R.id.etTodoTitle)
        etDesc      = view.findViewById(R.id.etTodoDesc)
        tilTitle    = view.findViewById(R.id.tilTodoTitle)
        rgPriority  = view.findViewById(R.id.rgPriority)
        cgCategory  = view.findViewById(R.id.cgCategory)
        btnSave     = view.findViewById(R.id.btnSaveTodo)
        tvSheetTitle = view.findViewById(R.id.tvSheetTitle)

        // Pre-fill if editing
        existingTodo?.let { todo ->
            tvSheetTitle.text = "Edit Task"
            etTitle.setText(todo.title)
            etDesc.setText(todo.description)

            // Priority
            val radioId = when (todo.priority) {
                2    -> R.id.rbHigh
                0    -> R.id.rbLow
                else -> R.id.rbMedium
            }
            rgPriority.check(radioId)

            // Category chip
            for (i in 0 until cgCategory.childCount) {
                val chip = cgCategory.getChildAt(i) as? Chip
                if (chip?.text == todo.category) chip.isChecked = true
            }
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString().trim()
            if (title.isEmpty()) {
                tilTitle.error = "Title can't be empty"
                return@setOnClickListener
            }
            tilTitle.error = null

            val priority = when (rgPriority.checkedRadioButtonId) {
                R.id.rbHigh -> 2
                R.id.rbLow  -> 0
                else        -> 1
            }

            val selectedChip = cgCategory.findViewById<Chip>(cgCategory.checkedChipId)
            val category = selectedChip?.text?.toString() ?: "Personal"

            val todo = existingTodo?.copy(
                title = title,
                description = etDesc.text.toString().trim(),
                priority = priority,
                category = category
            ) ?: Todo(
                title = title,
                description = etDesc.text.toString().trim(),
                priority = priority,
                category = category
            )

            onSave?.invoke(todo)
            dismiss()
        }
    }

    override fun getTheme() = R.style.BottomSheetTheme
}