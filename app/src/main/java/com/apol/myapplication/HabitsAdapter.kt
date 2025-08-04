package com.apol.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

data class Habit(
    val id: String,
    val name: String,
    val streakDays: Int,
    val message: String,
    val count: Int,
    var isSelected: Boolean = false
)

class HabitsAdapter(
    private val onItemClick: (Habit) -> Unit,
    private val onItemLongClick: (Habit) -> Unit,
    private val onMarkDone: (Habit) -> Unit,
    private val onUndoDone: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private var habits: MutableList<Habit> = mutableListOf()
    var modoExclusaoAtivo: Boolean = false

    var onExclusaoModoVazio: (() -> Unit)? = null

    fun submitList(novaLista: List<Habit>) {
        habits = novaLista.toMutableList()
        notifyDataSetChanged()
    }

    fun limparSelecao() {
        habits.forEach { it.isSelected = false }
        modoExclusaoAtivo = false
        notifyDataSetChanged()
    }

    fun getSelecionados(): List<Habit> = habits.filter { it.isSelected }

    fun apagarSelecionados() {
        habits.removeAll { it.isSelected }
        modoExclusaoAtivo = false
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.habit_item, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(habits[position])
    }

    override fun getItemCount(): Int = habits.size

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nome: TextView = itemView.findViewById(R.id.habit_name)
        private val streakDays: TextView = itemView.findViewById(R.id.text_streak_days)
        private val message: TextView = itemView.findViewById(R.id.text_streak_message)
        private val count: TextView = itemView.findViewById(R.id.habit_count)
        private val btnCheckDone: ImageButton = itemView.findViewById(R.id.btn_check_done)
        private val btnCheck: ImageButton = itemView.findViewById(R.id.btn_check)

        fun bind(habit: Habit) {
            nome.text = habit.name
            streakDays.text = "${habit.streakDays} dias seguidos"
            message.text = habit.message
            count.text = habit.count.toString()

            val backgroundRes = if (habit.isSelected) R.drawable.bg_selected_note else R.drawable.rounded_semi_transparent
            itemView.background = ContextCompat.getDrawable(itemView.context, backgroundRes)

            itemView.setOnClickListener {
                if (modoExclusaoAtivo) {
                    habit.isSelected = !habit.isSelected
                    notifyItemChanged(adapterPosition)

                    if (getSelecionados().isEmpty()) {
                        onExclusaoModoVazio?.invoke()
                    }
                } else {
                    onItemClick(habit)
                }
            }

            itemView.setOnLongClickListener {
                if (!modoExclusaoAtivo) {
                    modoExclusaoAtivo = true
                    habit.isSelected = true

                    notifyItemChanged(adapterPosition)
                    onItemLongClick(habit)
                }
                true
            }

            btnCheck.setOnClickListener {
                if (!modoExclusaoAtivo) {
                    onMarkDone(habit)
                }
            }

            btnCheckDone.setOnClickListener {
                if (!modoExclusaoAtivo) {
                    onUndoDone(habit)
                }
            }
        }
    }
}