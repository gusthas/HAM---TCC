// Crie este novo arquivo: TreinoNotaAdapter.kt
package com.apol.myapplication.data.model

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.R

class TreinoNotaAdapter(
    private var notas: List<TreinoNota>,
    private val onItemClick: (TreinoNota) -> Unit
) : RecyclerView.Adapter<TreinoNotaAdapter.NotaViewHolder>() {

    fun submitList(newNotas: List<TreinoNota>) {
        notas = newNotas
        notifyDataSetChanged()
    }

    class NotaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titulo: TextView = itemView.findViewById(R.id.titulo_nota)
        val conteudo: TextView = itemView.findViewById(R.id.conteudo_nota_snippet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_treino_nota, parent, false)
        return NotaViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotaViewHolder, position: Int) {
        val nota = notas[position]
        holder.titulo.text = nota.titulo
        if (nota.conteudo.isNotBlank()) {
            holder.conteudo.visibility = View.VISIBLE
            holder.conteudo.text = nota.conteudo
        } else {
            holder.conteudo.visibility = View.GONE
        }
        holder.itemView.setOnClickListener { onItemClick(nota) }
    }

    override fun getItemCount() = notas.size
}