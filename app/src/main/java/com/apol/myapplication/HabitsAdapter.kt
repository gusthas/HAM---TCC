package com.apol.myapplication

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

data class Habit(
    val id: String,
    val name: String,
    val streakDays: Int,
    val message: String,
    val count: Int,
    var isSelected: Boolean = false,
    var isFavorited: Boolean = false
)

class HabitsAdapter(
    private val onItemClick: (Habit) -> Unit,
    private val onMarkDone: (Habit) -> Unit,
    private val onUndoDone: (Habit) -> Unit,
    private val onToggleFavorite: (Habit) -> Unit // <<< PARÂMETRO ADICIONADO DE VOLTA
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private var habitList: MutableList<Habit> = mutableListOf()
    var modoExclusaoAtivo: Boolean = false
    var onExclusaoModoVazio: (() -> Unit)? = null

    fun getHabitAt(position: Int): Habit? {
        return habitList.getOrNull(position)
    }

    fun submitList(novaLista: List<Habit>) {
        habitList.clear()
        habitList.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun limparSelecao() {
        habitList.forEach { it.isSelected = false }
    }

    fun getSelecionados(): List<Habit> = habitList.filter { it.isSelected }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icone: ImageView = itemView.findViewById(R.id.icone_habito)
        val nome: TextView = itemView.findViewById(R.id.habit_name)
        val streakDays: TextView = itemView.findViewById(R.id.text_streak_days)
        val message: TextView = itemView.findViewById(R.id.text_streak_message)
        val count: TextView = itemView.findViewById(R.id.habit_count)
        val btnFavorite: ImageButton = itemView.findViewById(R.id.btn_favorite)
        val btnCheck: ImageButton = itemView.findViewById(R.id.btn_check)
        val btnCheckDone: ImageButton = itemView.findViewById(R.id.btn_check_done)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habitList[position]
        val context = holder.itemView.context

        val emoji = (context as? habitos)?.extrairEmoji(habit.name) ?: ""
        val nomeSemEmoji = (context as? habitos)?.removerEmoji(habit.name) ?: ""

        holder.nome.text = nomeSemEmoji
        holder.streakDays.text = "${habit.streakDays} dias seguidos"
        holder.message.text = habit.message
        holder.count.text = habit.count.toString()

        if (emoji.isNotEmpty() && context is habitos) {
            holder.icone.setImageDrawable(context.TextDrawable(context, emoji))
        } else {
            holder.icone.setImageResource(R.drawable.ic_habits)
        }

        val background = if (habit.isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
        holder.itemView.background = ContextCompat.getDrawable(context, background)

        if (habit.count > 0) {
            holder.btnCheck.visibility = View.GONE
            holder.btnCheckDone.visibility = View.VISIBLE
        } else {
            holder.btnCheck.visibility = View.VISIBLE
            holder.btnCheckDone.visibility = View.GONE
        }

        if (habit.isFavorited) {
            holder.btnFavorite.setImageResource(R.drawable.ic_star_outline)
        } else {
            holder.btnFavorite.setImageResource(R.drawable.ic_star_filled)
        }

        holder.itemView.setOnClickListener { onItemClick(habit) }
        holder.btnFavorite.setOnClickListener { if (!modoExclusaoAtivo) onToggleFavorite(habit) }
        holder.btnCheck.setOnClickListener { if (!modoExclusaoAtivo) onMarkDone(habit) }
        holder.btnCheckDone.setOnClickListener { if (!modoExclusaoAtivo) onUndoDone(habit) }
    }

    override fun getItemCount(): Int = habitList.size
}