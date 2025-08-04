
package com.apol.myapplication

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.apol.myapplication.data.model.LogEntry

// NOME DA CLASSE MUDOU
class LogEntryAdapter(
    val listaDeLogs: MutableList<LogEntry>,
    private val onDeleteClick: (LogEntry, Int) -> Unit
) : RecyclerView.Adapter<LogEntryAdapter.LogEntryViewHolder>() { // NOME DO VIEWHOLDER MUDOU

    // NOME DA CLASSE VIEWHOLDER MUDOU
    class LogEntryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numero: TextView = itemView.findViewById(R.id.numero_exercicio)
        val nome: EditText = itemView.findViewById(R.id.input_nome_exercicio)
        val carga: EditText = itemView.findViewById(R.id.input_carga)
        val repeticoes: EditText = itemView.findViewById(R.id.input_repeticoes)
        val btnApagar: ImageButton = itemView.findViewById(R.id.btn_apagar_exercicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogEntryViewHolder { // TIPO DE RETORNO MUDOU
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercicio, parent, false)
        return LogEntryViewHolder(view)
    }

    override fun getItemCount() = listaDeLogs.size

    override fun onBindViewHolder(holder: LogEntryViewHolder, position: Int) { // TIPO DO HOLDER MUDOU
        val log = listaDeLogs[position]

        // Remove listeners antigos para evitar bugs na reciclagem da lista
        (holder.itemView.tag as? Array<TextWatcher>)?.forEach { watcher ->
            holder.nome.removeTextChangedListener(watcher)
            holder.carga.removeTextChangedListener(watcher)
            holder.repeticoes.removeTextChangedListener(watcher)
        }

        holder.numero.text = "${position + 1}."
        holder.nome.setText(log.campo1)
        holder.carga.setText(log.campo2)
        holder.repeticoes.setText(log.campo4)

        // TextWatcher para atualizar a lista em memória enquanto o usuário digita
        val nomeWatcher = createTextWatcher { log.campo1 = it }
        val cargaWatcher = createTextWatcher { log.campo2 = it }
        val repsWatcher = createTextWatcher { log.campo4 = it }

        holder.nome.addTextChangedListener(nomeWatcher)
        holder.carga.addTextChangedListener(cargaWatcher)
        holder.repeticoes.addTextChangedListener(repsWatcher)

        holder.itemView.tag = arrayOf(nomeWatcher, cargaWatcher, repsWatcher)

        holder.btnApagar.setOnClickListener {
            onDeleteClick(log, position)
        }
    }

    // Função auxiliar para criar TextWatcher de forma limpa
    private fun createTextWatcher(onTextChanged: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s.toString())
        }
        override fun afterTextChanged(s: Editable?) {}
    }
}