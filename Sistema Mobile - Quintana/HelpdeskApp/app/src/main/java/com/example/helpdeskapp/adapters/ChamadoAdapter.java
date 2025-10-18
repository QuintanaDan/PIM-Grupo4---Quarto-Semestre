package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpdeskapp.R;
import com.example.helpdeskapp.dao.TagDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.models.Tag;

import java.util.List;

public class ChamadoAdapter extends RecyclerView.Adapter<ChamadoAdapter.ChamadoViewHolder> {
    private Context context;
    private List<Chamado> listaChamados;
    private OnChamadoClickListener onChamadoClickListener;

    public interface OnChamadoClickListener {
        void onChamadoClick(Chamado chamado, int position);
        void onChamadoLongClick(Chamado chamado, int position);
    }

    public ChamadoAdapter(Context context, List<Chamado> listaChamados) {
        this.context = context;
        this.listaChamados = listaChamados;
    }

    public void setOnChamadoClickListener(OnChamadoClickListener listener) {
        this.onChamadoClickListener = listener;
    }

    @NonNull
    @Override
    public ChamadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chamado, parent, false);
        return new ChamadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChamadoViewHolder holder, int position) {
        Chamado chamado = listaChamados.get(position);

        // Protocolo/Número
        if (holder.tvNumero != null) {
            holder.tvNumero.setText(chamado.getProtocoloFormatado());
        }

        // Título
        if (holder.tvTitulo != null) {
            holder.tvTitulo.setText(chamado.getTitulo());
        }

        // Status
        if (holder.tvStatus != null) {
            holder.tvStatus.setText(chamado.getStatus());

            // Cor do status
            String status = chamado.getStatus().toLowerCase();
            if (status.contains("aberto")) {
                holder.tvStatus.setTextColor(Color.parseColor("#4CAF50"));
            } else if (status.contains("andamento")) {
                holder.tvStatus.setTextColor(Color.parseColor("#FF9800"));
            } else if (status.contains("resolvido") || status.contains("fechado")) {
                holder.tvStatus.setTextColor(Color.parseColor("#2196F3"));
            }
        }

        // Prioridade
        if (holder.tvPrioridade != null) {
            holder.tvPrioridade.setText(chamado.getPrioridade());
        }

        // Data
        if (holder.tvData != null && chamado.getDataCriacaoFormatada() != null) {
            holder.tvData.setText(chamado.getDataCriacaoFormatada());
        }

        // OPCIONAL: Carregar e mostrar tags
        try {
            if (holder.recyclerViewTags != null) {
                TagDAO tagDAO = new TagDAO(context);
                List<Tag> tags = tagDAO.buscarTagsDoChamado(chamado.getId());

                if (tags != null && !tags.isEmpty()) {
                    holder.recyclerViewTags.setVisibility(View.VISIBLE);

                    LinearLayoutManager layoutManager = new LinearLayoutManager(
                            context, LinearLayoutManager.HORIZONTAL, false);
                    holder.recyclerViewTags.setLayoutManager(layoutManager);

                    TagAdapter tagAdapter = new TagAdapter(tags, context, null, false);
                    holder.recyclerViewTags.setAdapter(tagAdapter);
                } else {
                    holder.recyclerViewTags.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            // Se der erro (ex: recyclerViewTags não existe no layout), ignora
            if (holder.recyclerViewTags != null) {
                holder.recyclerViewTags.setVisibility(View.GONE);
            }
        }

        // Click listener
        holder.itemView.setOnClickListener(v -> {
            if (onChamadoClickListener != null) {
                onChamadoClickListener.onChamadoClick(chamado, position);
            }
        });

        // Long click listener
        holder.itemView.setOnLongClickListener(v -> {
            if (onChamadoClickListener != null) {
                onChamadoClickListener.onChamadoLongClick(chamado, position);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return listaChamados != null ? listaChamados.size() : 0;
    }

    public void updateList(List<Chamado> novaLista) {
        this.listaChamados = novaLista;
        notifyDataSetChanged();
    }

    public static class ChamadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero;
        TextView tvTitulo;
        TextView tvStatus;
        TextView tvPrioridade;
        TextView tvData;
        RecyclerView recyclerViewTags;

        public ChamadoViewHolder(@NonNull View itemView) {
            super(itemView);

            // Inicializar views
            tvNumero = itemView.findViewById(R.id.tvNumeroChamado);
            tvTitulo = itemView.findViewById(R.id.tvTituloChamado);
            tvStatus = itemView.findViewById(R.id.tvStatusChamado);
            tvPrioridade = itemView.findViewById(R.id.tvPrioridadeChamado);
            tvData = itemView.findViewById(R.id.tvDataChamado);

            //  RecyclerView de tags
            try {
                recyclerViewTags = itemView.findViewById(R.id.rvTagsChamado);
            } catch (Exception e) {
                recyclerViewTags = null;
            }
        }
    }
}