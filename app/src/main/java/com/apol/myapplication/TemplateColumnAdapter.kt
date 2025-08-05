
package com.apol.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TemplateColumnAdapter(
    val colunas: MutableList<String>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<TemplateColumnAdapter.ColumnViewHolder>() {

    class ColumnViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nome: TextView = itemView.findViewById(R.id.nome_coluna)
        val btnApagar: ImageButton = itemView.findViewById(R.id.btn_apagar_coluna)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColumnViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_coluna_template, parent, false)
        return ColumnViewHolder(view)
    }

    override fun onBindViewHolder(holder: ColumnViewHolder, position: Int) {
        holder.nome.text = colunas[position]
        holder.btnApagar.setOnClickListener { onDeleteClick(position) }
    }

    override fun getItemCount() = colunas.size
}