package com.example.projectcalendar.data.db.Entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.projectcalendar.data.db.Converter.Converters

@Entity(
    tableName = "notes",  // Во множественном числе, как "events", "tasks"
    indices = [
        Index(value = ["date"])  // Поиск заметок по дате
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    @ColumnInfo(name = "title")
    val title : String,

    @ColumnInfo(name = "date")
    val date: Long,  // Дата заметки (начало дня в миллисекундах)

    @ColumnInfo(name = "content")  // Не "container", а "content"
    val content: String,  // Текст заметки (обязательный)

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long? = null
)