
package com.apol.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.LogEntry
import com.google.android.material.textfield.TextInputLayout

class LogAdapter(
    var listaDeLogs: MutableList<LogEntry>,
    var templateColunas: List<String>,
    private val onDeleteClick: (LogEntry, Int) -> Unit
) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numero: TextView = itemView.findViewById(R.id.numero_log)
        val btnApagar: ImageButton = itemView.findViewById(R.id.btn_apagar_log)
        val layouts = listOf<TextInputLayout>(
            itemView.findViewById(R.id.layout_campo1),
            itemView.findViewById(R.id.layout_campo2),
            itemView.findViewById(R.id.layout_campo3),
            itemView.findViewById(R.id.layout_campo4)
        )
        val inputs = listOf<EditText>(
            itemView.findViewById(R.id.input_campo1),
            itemView.findViewById(R.id.input_campo2),
            itemView.findViewById(R.id.input_campo3),
            itemView.findViewById(R.id.input_campo4)
        )
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log_entry, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = listaDeLogs[position]
        holder.numero.text = "${position + 1}."

        // A única tarefa é exibir os dados e os campos corretos. Nada mais.
        holder.layouts.forEachIndexed { index, textInputLayout ->
            if (index < templateColunas.size) {
                textInputLayout.visibility = View.VISIBLE
                textInputLayout.hint = templateColunas[index]
                val editText = holder.inputs[index]
                val textoAtual = when(index) {
                    0 -> log.campo1; 1 -> log.campo2; 2 -> log.campo3; 3 -> log.campo4; else -> ""
                }
                editText.setText(textoAtual)
            } else {
                textInputLayout.visibility = View.GONE
            }
        }
        holder.btnApagar.setOnClickListener { onDeleteClick(log, position) }
    }

    override fun getItemCount() = listaDeLogs.size
}