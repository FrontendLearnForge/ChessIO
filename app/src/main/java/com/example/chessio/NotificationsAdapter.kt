package com.example.chessio

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class NotificationsAdapter(
    private val onClick: (Notification, Int) -> Unit // Добавляем position в callback
) : ListAdapter<Notification, NotificationsAdapter.ViewHolder>(DiffCallback()) {

    // Изменяем ViewHolder для работы с MaterialCardView
    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val message: TextView = view.findViewById(R.id.notificationMessage)
        val date: TextView = view.findViewById(R.id.notificationDate)
        val cardView: MaterialCardView = view.findViewById(R.id.notificationCard) // Используем MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = getItem(position)
        holder.message.text = notification.message

        // Парсим дату из строки и форматируем в локальное время
        try {
            // Формат даты, который приходит с сервера (предположим ISO 8601)
            val serverFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            serverFormat.timeZone = TimeZone.getTimeZone("UTC") // Указываем, что дата в UTC

            // Формат для отображения
            val displayFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

            // Парсим дату и форматируем в локальное время
            val date = serverFormat.parse(notification.createdAt)
            val formattedDateTime = date?.let { displayFormat.format(it) } ?: notification.createdAt

            holder.date.text = formattedDateTime
        } catch (e: Exception) {
            // Если не удалось распарсить, показываем как есть
            holder.date.text = notification.createdAt
            e.printStackTrace()
        }
        // Обновляем цвет в зависимости от статуса прочтения
        updateCardAppearance(holder.cardView, notification.isRead)

        holder.itemView.setOnClickListener {
            onClick(notification, position) // Передаем позицию в обработчик
        }
    }

    // Функция для обновления внешнего вида карточки
    private fun updateCardAppearance(card: MaterialCardView, isRead: Boolean) {
        if (!isRead) {
            // Непрочитанное уведомление
            card.setCardBackgroundColor(Color.parseColor("#DFBF81"))
        } else {
            // Прочитанное уведомление
            card.setCardBackgroundColor(Color.parseColor("#907954")) // Полупрозрачный
        }
    }

    // Обновляем уведомление в списке
    fun markAsRead(position: Int) {
        val currentList = currentList.toMutableList()
        if (position in 0 until currentList.size) {
            val notification = currentList[position]
            currentList[position] = notification.copy(isRead = true)
            submitList(currentList)
            notifyItemChanged(position)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Notification>() {
        override fun areItemsTheSame(oldItem: Notification, newItem: Notification) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Notification, newItem: Notification) = oldItem == newItem
    }
}