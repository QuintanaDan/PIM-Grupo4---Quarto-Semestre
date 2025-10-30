package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.models.Comentario;
import java.util.List;

public class ComentarioAdapter extends RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder> {
    private List<Comentario> listaComentarios;
    private Context context;

    public ComentarioAdapter(List<Comentario> listaComentarios, Context context) {
        this.listaComentarios = listaComentarios;
        this.context = context;
    }

    @NonNull
    @Override
    public ComentarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_comentario, parent, false);
        return new ComentarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComentarioViewHolder holder, int position) {
        Comentario comentario = listaComentarios.get(position);

        // Usar o nome do usuário ou autor
        String nomeExibicao = comentario.getNomeUsuario() != null ?
                comentario.getNomeUsuario() :
                comentario.getAutor();

        holder.tvAutor.setText(nomeExibicao != null ? nomeExibicao : "Usuário Desconhecido");
        holder.tvTexto.setText(comentario.getTexto());
        holder.tvDataComentario.setText(comentario.getDataCriacaoFormatada());

        // Destacar comentários do técnico baseado no tipo ou nome
        if (isTecnico(comentario)) {
            holder.itemView.setBackgroundResource(R.drawable.comentario_tecnico_background);
            holder.tvAutor.setTextColor(context.getResources().getColor(R.color.primary));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.comentario_usuario_background);
            holder.tvAutor.setTextColor(context.getResources().getColor(R.color.text_dark));
        }
    }

    @Override
    public int getItemCount() {
        return listaComentarios.size();
    }

    private boolean isTecnico(Comentario comentario) {
        // Verificar pelo tipo do comentário primeiro
        if ("tecnico".equalsIgnoreCase(comentario.getTipo()) ||
                "suporte".equalsIgnoreCase(comentario.getTipo())) {
            return true;
        }

        // Verificar pelo nome do usuário
        String nomeUsuario = comentario.getNomeUsuario() != null ?
                comentario.getNomeUsuario() :
                comentario.getAutor();

        if (nomeUsuario != null) {
            return nomeUsuario.equals("Maria Santos") ||
                    nomeUsuario.contains("Técnico") ||
                    nomeUsuario.contains("Suporte");
        }

        return false;
    }

    public void adicionarComentario(Comentario comentario) {
        listaComentarios.add(comentario);
        notifyItemInserted(listaComentarios.size() - 1);
    }

    public void atualizarLista(List<Comentario> novosComentarios) {
        this.listaComentarios.clear();
        this.listaComentarios.addAll(novosComentarios);
        notifyDataSetChanged();
    }

    static class ComentarioViewHolder extends RecyclerView.ViewHolder {
        TextView tvAutor, tvTexto, tvDataComentario;

        public ComentarioViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAutor = itemView.findViewById(R.id.tvAutor);
            tvTexto = itemView.findViewById(R.id.tvTexto);
            tvDataComentario = itemView.findViewById(R.id.tvDataComentario);
        }
    }
}
