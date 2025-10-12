package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.models.Anexo;
import java.util.List;

public class AnexoAdapter extends RecyclerView.Adapter<AnexoAdapter.AnexoViewHolder> {
    private List<Anexo> listaAnexos;
    private Context context;
    private OnAnexoClickListener listener;

    public interface OnAnexoClickListener {
        void onAnexoClick(Anexo anexo);
        void onAnexoDeleteClick(Anexo anexo);
    }

    public AnexoAdapter(List<Anexo> listaAnexos, Context context, OnAnexoClickListener listener) {
        this.listaAnexos = listaAnexos;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnexoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_anexo, parent, false);
        return new AnexoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnexoViewHolder holder, int position) {
        Anexo anexo = listaAnexos.get(position);

        // Ícone baseado no tipo
        holder.tvIcone.setText(anexo.getIcone());

        // Nome do arquivo
        holder.tvNomeArquivo.setText(anexo.getNomeArquivo());

        // Tamanho formatado
        holder.tvTamanho.setText(anexo.getTamanhoFormatado());

        // Clique no item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAnexoClick(anexo);
            }
        });

        // Botão deletar
        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAnexoDeleteClick(anexo);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaAnexos.size();
    }

    public void atualizarLista(List<Anexo> novosAnexos) {
        this.listaAnexos.clear();
        this.listaAnexos.addAll(novosAnexos);
        notifyDataSetChanged();
    }

    static class AnexoViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcone, tvNomeArquivo, tvTamanho;
        ImageButton btnDelete;

        public AnexoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIcone = itemView.findViewById(R.id.tvIconeAnexo);
            tvNomeArquivo = itemView.findViewById(R.id.tvNomeArquivo);
            tvTamanho = itemView.findViewById(R.id.tvTamanhoArquivo);
            btnDelete = itemView.findViewById(R.id.btnDeleteAnexo);
        }
    }
}