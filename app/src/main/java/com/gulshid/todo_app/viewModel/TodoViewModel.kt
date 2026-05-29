package com.gulshid.todo_app.viewModel

import androidx.lifecycle.*
import com.gulshid.todo_app.data.Todo
import com.gulshid.todo_app.repository.TodoRepository
import kotlinx.coroutines.launch

class TodoViewModel(private val repository: TodoRepository) : ViewModel() {

    // Filter state: "all", "active", "completed"
    private val _filter = MutableLiveData("all")
    val filter: LiveData<String> = _filter

    val allTodos: LiveData<List<Todo>> = repository.allTodos
    val activeTodos: LiveData<List<Todo>> = repository.activeTodos
    val completedTodos: LiveData<List<Todo>> = repository.completedTodos
    val activeCount: LiveData<Int> = repository.activeCount

    // Currently displayed list based on filter
    val displayedTodos: LiveData<List<Todo>> = _filter.switchMap { f ->
        when (f) {
            "active"    -> repository.activeTodos
            "completed" -> repository.completedTodos
            else        -> repository.allTodos
        }
    }

    fun setFilter(f: String) { _filter.value = f }

    fun insert(todo: Todo) = viewModelScope.launch { repository.insert(todo) }

    fun update(todo: Todo) = viewModelScope.launch { repository.update(todo) }

    fun delete(todo: Todo) = viewModelScope.launch { repository.delete(todo) }

    fun toggleComplete(todo: Todo) = viewModelScope.launch {
        repository.update(todo.copy(isCompleted = !todo.isCompleted))
    }

    fun deleteAllCompleted() = viewModelScope.launch { repository.deleteAllCompleted() }
}