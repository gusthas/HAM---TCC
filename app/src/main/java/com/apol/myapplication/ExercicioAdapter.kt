// Crie um novo arquivo: ExercicioAdapter.kt
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
import com.apol.myapplication.data.model.Exercicio

class ExercicioAdapter(
    val listaExercicios: MutableList<Exercicio>,
    private val onDeleteClick: (Exercicio, Int) -> Unit
) : RecyclerView.Adapter<ExercicioAdapter.ExercicioViewHolder>() {

    class ExercicioViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val numero: TextView = itemView.findViewById(R.id.numero_exercicio)
        val nome: EditText = itemView.findViewById(R.id.input_nome_exercicio)
        val carga: EditText = itemView.findViewById(R.id.input_carga)
        val repeticoes: EditText = itemView.findViewById(R.id.input_repeticoes)
        val btnApagar: ImageButton = itemView.findViewById(R.id.btn_apagar_exercicio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExercicioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercicio, parent, false)
        return ExercicioViewHolder(view)
    }

    override fun getItemCount() = listaExercicios.size

    override fun onBindViewHolder(holder: ExercicioViewHolder, position: Int) {
        val exercicio = listaExercicios[position]

        // Remove os TextWatchers antigos para evitar atualizações incorretas
        holder.nome.removeTextChangedListener(holder.itemView.tag as? TextWatcher)
        holder.carga.removeTextChangedListener(holder.itemView.tag as? TextWatcher)
        holder.repeticoes.removeTextChangedListener(holder.itemView.tag as? TextWatcher)

        // Preenche os campos
        holder.numero.text = "${position + 1}."
        holder.nome.setText(exercicio.nome)
        holder.carga.setText(exercicio.carga)
        holder.repeticoes.setText(exercicio.repeticoes)

        // Cria um novo TextWatcher para atualizar o objeto em memória
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Atualiza o objeto na lista conforme o usuário digita
                exercicio.nome = holder.nome.text.toString()
                exercicio.carga = holder.carga.text.toString()
                exercicio.repeticoes = holder.repeticoes.text.toString()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        // Adiciona os listeners e os guarda na tag da view
        holder.nome.addTextChangedListener(textWatcher)
        holder.carga.addTextChangedListener(textWatcher)
        holder.repeticoes.addTextChangedListener(textWatcher)
        holder.itemView.tag = textWatcher

        holder.btnApagar.setOnClickListener {
            onDeleteClick(exercicio, position)
        }
    }
}