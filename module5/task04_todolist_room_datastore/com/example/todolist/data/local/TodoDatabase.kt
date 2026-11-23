package com.example.todolist.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "todos")
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val isCompleted: Boolean
)

@Dao
interface TodoDao {
    @Query("SELECT * FROM todos ORDER BY id DESC")
    fun observeAll(): Flow<List<TodoEntity>>

    @Query("SELECT COUNT(*) FROM todos")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TodoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TodoEntity)

    @Update
    suspend fun update(item: TodoEntity)

    @Delete
    suspend fun delete(item: TodoEntity)
}

@Database(entities = [TodoEntity::class], version = 1, exportSchema = false)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun todoDao(): TodoDao
}
