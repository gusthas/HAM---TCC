package com.apol.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.Bloco

class BlocosAdapter(
    private val onItemClick: (Bloco) -> Unit,
    private val onItemLongClick: (Bloco) -> Unit
) : RecyclerView.Adapter<BlocosAdapter.BlocoViewHolder>() {

    private var blocos: List<Bloco> = emptyList()
    var modoExclusaoAtivo: Boolean = false
    var onExclusaoModoVazio: (() -> Unit)? = null

    fun submitList(novaLista: List<Bloco>) {
        blocos = novaLista
        notifyDataSetChanged()
    }

    fun limparSelecao() {
        blocos.forEach { it.isSelected = false }
        modoExclusaoAtivo = false // Garante que o modo de exclusão seja desativado
        notifyDataSetChanged()
    }

    fun getSelecionados(): List<Bloco> = blocos.filter { it.isSelected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlocoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_bloco, parent, false)
        return BlocoViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlocoViewHolder, position: Int) {
        holder.bind(blocos[position])
    }

    override fun getItemCount(): Int = blocos.size

    inner class BlocoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Supondo que seu item_bloco.xml tenha um TextView com id 'texto_bloco'
        // e um container com id 'item_bloco_container'
        private val texto = itemView.findViewById<TextView>(R.id.texto_bloco)
        private val container = itemView.findViewById<View>(R.id.item_bloco_container)

        fun bind(bloco: Bloco) {
            texto.text = if (bloco.subtitulo.isNotEmpty()) {
                "${bloco.nome} - ${bloco.subtitulo}"
            } else {
                bloco.nome
            }

            val background = if (bloco.isSelected) R.drawable.bg_selected_item else R.drawable.rounded_semi_transparent
            container.background = ContextCompat.getDrawable(itemView.context, background)

            itemView.setOnClickListener {
                if (modoExclusaoAtivo) {
                    bloco.isSelected = !bloco.isSelected
                    notifyItemChanged(adapterPosition)
                    if (getSelecionados().isEmpty()) onExclusaoModoVazio?.invoke()
                } else {
                    onItemClick(bloco)
                }
            }

            itemView.setOnLongClickListener {
                if (!modoExclusaoAtivo) {
                    onItemLongClick(bloco) // A Activity vai ativar o modo de exclusão
                }
                true
            }
        }
    }
}