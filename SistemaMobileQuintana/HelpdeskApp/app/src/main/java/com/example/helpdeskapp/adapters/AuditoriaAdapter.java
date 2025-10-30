package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.models.Auditoria;
import java.util.List;

public class AuditoriaAdapter extends RecyclerView.Adapter<AuditoriaAdapter.AuditoriaViewHolder> {
    private List<Auditoria> listaAuditorias;
    private Context context;
    private OnAuditoriaClickListener listener;

    public interface OnAuditoriaClickListener {
        void onAuditoriaClick(Auditoria auditoria);
    }

    public AuditoriaAdapter(List<Auditoria> listaAuditorias, Context context, OnAuditoriaClickListener listener) {
        this.listaAuditorias = listaAuditorias;
        this.context = context;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AuditoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_auditoria, parent, false);
        return new AuditoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AuditoriaViewHolder holder, int position) {
        Auditoria auditoria = listaAuditorias.get(position);

        // Ícone da ação
        holder.tvIconeAcao.setText(auditoria.getIconeAcao());

        try {
            int cor = Color.parseColor(auditoria.getCorAcao());
            holder.tvIconeAcao.setTextColor(cor);
        } catch (Exception e) {
            holder.tvIconeAcao.setTextColor(Color.GRAY);
        }

        // Ação
        holder.tvAcao.setText(auditoria.getAcao());

        // Usuário
        String usuario = auditoria.getNomeUsuario() != null ?
                auditoria.getNomeUsuario() : "Usuário #" + auditoria.getUsuarioId();
        holder.tvUsuario.setText(usuario);

        // Descrição
        if (auditoria.getDescricao() != null && !auditoria.getDescricao().isEmpty()) {
            holder.tvDescricao.setText(auditoria.getDescricao());
            holder.tvDescricao.setVisibility(View.VISIBLE);
        } else {
            holder.tvDescricao.setVisibility(View.GONE);
        }

        // Entidade
        String entidade = auditoria.getEntidade() + " #" + auditoria.getEntidadeId();
        holder.tvEntidade.setText(entidade);

        // Data
        holder.tvData.setText(auditoria.getDataFormatada());

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAuditoriaClick(auditoria);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaAuditorias.size();
    }

    public void atualizarLista(List<Auditoria> novasAuditorias) {
        this.listaAuditorias.clear();
        this.listaAuditorias.addAll(novasAuditorias);
        notifyDataSetChanged();
    }

    static class AuditoriaViewHolder extends RecyclerView.ViewHolder {
        TextView tvIconeAcao, tvAcao, tvUsuario, tvDescricao, tvEntidade, tvData;

        public AuditoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvIconeAcao = itemView.findViewById(R.id.tvIconeAcao);
            tvAcao = itemView.findViewById(R.id.tvAcao);
            tvUsuario = itemView.findViewById(R.id.tvUsuario);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvEntidade = itemView.findViewById(R.id.tvEntidade);
            tvData = itemView.findViewById(R.id.tvData);
        }
    }
}