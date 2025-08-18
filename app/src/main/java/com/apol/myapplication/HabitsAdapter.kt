package com.apol.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.HabitUI // MUDANÇA IMPORTANTE AQUI

class HabitsAdapter(
    // Funções agora recebem o modelo de UI: HabitUI
    private val onItemClick: (HabitUI) -> Unit,
    private val onItemLongClick: (HabitUI) -> Unit,
    private val onMarkDone: (HabitUI) -> Unit,
    private val onUndoDone: (HabitUI) -> Unit,
    private val onToggleFavorite: (HabitUI) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    private var habitList: MutableList<HabitUI> = mutableListOf()
    var modoExclusaoAtivo: Boolean = false

    fun submitList(novaLista: List<HabitUI>) {
        habitList.clear()
        habitList.addAll(novaLista)
        notifyDataSetChanged()
    }

    fun toggleSelecao(habit: HabitUI) {
        val habitNaLista = habitList.find { it.id == habit.id }
        habitNaLista?.let {
            it.isSelected = !it.isSelected
            notifyItemChanged(habitList.indexOf(it))
        }
    }

    fun getSelecionados(): List<HabitUI> = habitList.filter { it.isSelected }

    fun limparSelecao() {
        habitList.forEach { it.isSelected = false }
        notifyDataSetChanged()
    }

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icone: ImageView = itemView.findViewById(R.id.icone_habito)
        val nome: TextView = itemView.findViewById(R.id.habit_name)
        val streakDays: TextView = itemView.findViewById(R.id.text_streak_days)
        val message: TextView = itemView.findViewById(R.id.text_streak_message)
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

        holder.nome.text = habit.name
        holder.streakDays.text = "${habit.streakDays} dias seguidos"
        holder.message.text = habit.message
        holder.icone.setImageResource(R.drawable.ic_habits)

        if (habit.count > 0) {
            holder.btnCheck.visibility = View.GONE
            holder.btnCheckDone.visibility = View.VISIBLE
        } else {
            holder.btnCheck.visibility = View.VISIBLE
            holder.btnCheckDone.visibility = View.GONE
        }

        holder.btnFavorite.setImageResource(
            if (habit.isFavorited) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )

        val background = if (habit.isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
        holder.itemView.background = ContextCompat.getDrawable(context, background)

        holder.itemView.setOnClickListener { onItemClick(habit) }
        holder.itemView.setOnLongClickListener {
            onItemLongClick(habit)
            true
        }
        holder.btnFavorite.setOnClickListener { onToggleFavorite(habit) }
        holder.btnCheck.setOnClickListener { onMarkDone(habit) }
        holder.btnCheckDone.setOnClickListener { onUndoDone(habit) }
    }

    override fun getItemCount(): Int = habitList.size
}