package com.gulshid.todo_app.repository

import androidx.lifecycle.LiveData
import com.gulshid.todo_app.data.Todo
import com.gulshid.todo_app.data.TodoDao

/**
 * Repository — single source of truth between ViewModel and Room.
 * Abstracts data source from the ViewModel.
 */
class TodoRepository(private val dao: TodoDao) {

    val allTodos: LiveData<List<Todo>> = dao.getAllTodos()
    val activeTodos: LiveData<List<Todo>> = dao.getActiveTodos()
    val completedTodos: LiveData<List<Todo>> = dao.getCompletedTodos()
    val activeCount: LiveData<Int> = dao.getActiveCount()

    suspend fun insert(todo: Todo) = dao.insert(todo)
    suspend fun update(todo: Todo) = dao.update(todo)
    suspend fun delete(todo: Todo) = dao.delete(todo)
    suspend fun deleteAllCompleted() = dao.deleteAllCompleted()
}