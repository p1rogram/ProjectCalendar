package com.example.projectcalendar.data.db.Dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.projectcalendar.data.db.Entity.EventEntity
import kotlinx.coroutines.flow.Flow

// 1. АННОТАЦИЯ @Dao
// Роль: Говорит Room: "Этот интерфейс — управляющий пульт для базы данных".
// Room автоматически создаст класс, который реализует эти методы.
@Dao
interface EventDao {

    // ==========================================
    // 2. ОПЕРАЦИИ ЗАПИСИ (INSERT, UPDATE, DELETE)
    // ==========================================

    /**
     * ВСТАВКА (INSERT)
     * Роль: Добавляет новое событие в таблицу.
     *
     * @Insert(onConflict = OnConflictStrategy.REPLACE)
     * - onConflict: Что делать, если событие с таким ID уже есть?
     * - REPLACE: Заменить старое новым (безопаснее, чем ABORT, которое просто выкинет ошибку).
     *
     * suspend: Функция выполняется в фоне, не блокируя экран.
     * Return Long: Возвращает ID вставленного события (чтобы ты знал, под каким номером оно сохранилось).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: EventEntity): Long

    /**
     * ОБНОВЛЕНИЕ (UPDATE)
     * Роль: Изменяет уже существующее событие (например, пользователь поменял время).
     *
     * @Update
     * - Room ищет запись по ID внутри объекта event и обновляет все её поля.
     */
    @Update
    suspend fun updateEvent(event: EventEntity)

    /**
     * УДАЛЕНИЕ (DELETE)
     * Роль: Удаляет событие из календаря.
     *
     * @Delete
     * - Room ищет запись по ID и удаляет её.
     */
    @Delete
    suspend fun deleteEvent(event: EventEntity)


    // ==========================================
    // 3. ОПЕРАЦИИ ЧТЕНИЯ (QUERY)
    // ==========================================

    /**
     * ПОЛУЧЕНИЕ ПО ID
     * Роль: Найти одно конкретное событие, чтобы открыть его детали.
     *
     * @Query: Здесь мы пишем чистый SQL-запрос вручную.
     * ":id" — это аргумент функции, который подставится в запрос.
     *
     * Return EventEntity?: Возвращает объект или null (если ничего не найдено).
     */
    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getEventById(id: Long): EventEntity?

    /**
     * ПОЛУЧЕНИЕ ЗА ПЕРИОД (САМЫЙ ВАЖНЫЙ ЗАПРОС ДЛЯ КАЛЕНДАРЯ)
     * Роль: Получить все события, которые попадают в выбранные даты (например, весь апрель).
     *
     * Flow<List<EventEntity>>:
     * - Это не просто список, а "поток".
     * - Если в БД что-то изменится (добавится событие), этот список обновится САМ, и экран перерисуется.
     * - Тебе не нужно вызывать функцию снова, чтобы обновить данные.
     *
     * ORDER BY startDateTime ASC: Сортировка от старых к новым.
     */
    @Query("SELECT * FROM events WHERE startDateTime BETWEEN :startDate AND :endDate ORDER BY startDateTime ASC")
    fun getEventsForDateRange(startDate: Long, endDate: Long): Flow<List<EventEntity>>

    /**
     * ПОЛУЧЕНИЕ ВАЖНЫХ СОБЫТИЙ
     * Роль: Найти все события, у которых стоит галочка "Важное".
     *
     * isImportant = 1: В SQLite "true" хранится как цифра 1.
     */
    @Query("SELECT * FROM events WHERE isImportant = 1 ORDER BY startDateTime ASC")
    fun getImportantEvents(): Flow<List<EventEntity>>
}