import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import com.apol.myapplication.R
import com.apol.myapplication.data.model.Note

class NotesAdapter(
    private val onItemClick: (Note) -> Unit,
    private val onItemLongClick: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private var notes: List<Note> = emptyList()
    var modoExclusaoAtivo: Boolean = false
    var onExclusaoModoVazio: (() -> Unit)? = null

    fun submitList(novaLista: List<Note>) {
        notes = novaLista
        notifyDataSetChanged()
    }

    fun limparSelecao() {
        notes.forEach { it.isSelected = false }
        modoExclusaoAtivo = false
        notifyDataSetChanged()
    }

    fun getSelecionados(): List<Note> = notes.filter { it.isSelected }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val icon: ImageView = itemView.findViewById(R.id.noteIcon)
        private val text: TextView = itemView.findViewById(R.id.noteText)
        private val date: TextView = itemView.findViewById(R.id.noteDate)

        fun bind(note: Note) {
            icon.setImageResource(R.drawable.ic_notes)
            text.text = note.text
            date.text = formatDate(note.lastModified)

            // Fundo visual
            val background = if (note.isSelected) R.drawable.bg_selected_note else R.drawable.rounded_semi_transparent
            itemView.background = ContextCompat.getDrawable(itemView.context, background)

            itemView.setOnClickListener {
                Log.d("NoteDebug", "Item clicado: ${note.text}")
                onItemClick(note)
            }

            itemView.setOnLongClickListener {
                Log.d("NoteDebug", "Long click em: ${note.text}")
                onItemLongClick(note)
                true
            }
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd 'de' MMMM, HH:mm", java.util.Locale("pt", "BR"))
        return sdf.format(java.util.Date(timestamp))
    }
}
