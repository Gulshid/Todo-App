package com.gulshid.todo_app.ui

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.gulshid.todo_app.R
import com.gulshid.todo_app.adapter.TodoAdapter
import com.gulshid.todo_app.data.Todo
import com.gulshid.todo_app.data.TodoDatabase
import com.gulshid.todo_app.repository.TodoRepository
import com.gulshid.todo_app.viewModel.TodoViewModel
import com.gulshid.todo_app.viewModel.TodoViewModelFactory

class MainActivity : AppCompatActivity() {

    private val viewModel: TodoViewModel by viewModels {
        val db = TodoDatabase.getDatabase(this)
        TodoViewModelFactory(TodoRepository(db.todoDao()))
    }

    private lateinit var adapter: TodoAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAdd: ExtendedFloatingActionButton
    private lateinit var tvEmptyState: LinearLayout   // ← LinearLayout not TextView
    private lateinit var tvTaskCount: TextView
    private lateinit var cgFilter: ChipGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
        setupRecyclerView()
        setupFab()
        setupFilterChips()
        observeViewModel()
    }

    private fun setupViews() {
        recyclerView = findViewById(R.id.recyclerView)
        fabAdd       = findViewById(R.id.fabAddTodo)
        tvEmptyState = findViewById(R.id.tvEmptyState)
        tvTaskCount  = findViewById(R.id.tvTaskCount)
        cgFilter     = findViewById(R.id.cgFilter)
    }

    private fun setupRecyclerView() {
        adapter = TodoAdapter(
            onToggle = { viewModel.toggleComplete(it) },
            onEdit   = { showAddEditSheet(it) },
            onDelete = { deleteTodoWithUndo(it) }
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(rv: RecyclerView, dx: Int, dy: Int) {
                if (dy > 10 && fabAdd.isExtended) fabAdd.shrink()
                if (dy < -10 && !fabAdd.isExtended) fabAdd.extend()
            }
        })

        val swipeCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder) = false
            override fun onSwiped(vh: RecyclerView.ViewHolder, direction: Int) {
                val todo = adapter.currentList[vh.adapterPosition]
                deleteTodoWithUndo(todo)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView)
    }

    private fun setupFab() {
        fabAdd.setOnClickListener { showAddEditSheet(null) }
    }

    private fun setupFilterChips() {
        cgFilter.setOnCheckedStateChangeListener { group, _ ->
            val filter = when (group.checkedChipId) {
                R.id.chipActive    -> "active"
                R.id.chipCompleted -> "completed"
                else               -> "all"
            }
            viewModel.setFilter(filter)
        }
    }

    private fun observeViewModel() {
        viewModel.displayedTodos.observe(this) { todos ->
            adapter.submitList(todos)
            tvEmptyState.visibility = if (todos.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.activeCount.observe(this) { count ->
            tvTaskCount.text = "$count task${if (count != 1) "s" else ""} remaining"
        }
    }

    private fun showAddEditSheet(todo: Todo?) {
        AddEditTodoBottomSheet.newInstance(todo) { savedTodo ->
            if (todo == null) viewModel.insert(savedTodo)
            else viewModel.update(savedTodo)
        }.show(supportFragmentManager, "AddEditTodo")
    }

    private fun deleteTodoWithUndo(todo: Todo) {
        viewModel.delete(todo)
        Snackbar.make(recyclerView, "\"${todo.title}\" deleted", Snackbar.LENGTH_LONG)
            .setAction("UNDO") { viewModel.insert(todo) }
            .setBackgroundTint(getColor(R.color.snackbar_bg))
            .setTextColor(getColor(R.color.white))
            .setActionTextColor(getColor(R.color.accent_teal))
            .show()
    }
}