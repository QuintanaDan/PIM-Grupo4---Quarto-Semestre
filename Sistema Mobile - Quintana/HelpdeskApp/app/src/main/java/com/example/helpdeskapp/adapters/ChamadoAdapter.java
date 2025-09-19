package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.DetalhesChamadoActivity;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.models.Chamado;
import java.util.List;

public class ChamadoAdapter extends RecyclerView.Adapter<ChamadoAdapter.ChamadoViewHolder> {

    private List<Chamado> chamados;
    private Context context;
    private OnChamadoClickListener listener;

    public interface OnChamadoClickListener {
        void onChamadoClick(Chamado chamado);
    }

    public ChamadoAdapter(Context context, List<Chamado> chamados) {
        this.context = context;
        this.chamados = chamados;
    }

    public void setOnChamadoClickListener(OnChamadoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChamadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chamado, parent, false);
        return new ChamadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChamadoViewHolder holder, int position) {
        Chamado chamado = chamados.get(position);

        holder.tvNumero.setText(chamado.getNumero());
        holder.tvTitulo.setText(chamado.getTitulo());
        holder.tvDescricao.setText(chamado.getDescricao());
        holder.tvStatus.setText(chamado.getStatusTexto());
        holder.tvPrioridade.setText(chamado.getPrioridadeTextoCompleto());

        // Data (se existir)
        if (chamado.getCreatedAt() != null && !chamado.getCreatedAt().isEmpty()) {
            holder.tvData.setText(formatarData(chamado.getCreatedAt()));
        } else {
            holder.tvData.setText("Data não disponível");
        }

        // ✅ ÚNICO click listener (SEM LAMBDA)
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Abrir tela de detalhes
                Intent intent = new Intent(context, DetalhesChamadoActivity.class);
                intent.putExtra("chamado_id", chamado.getId());
                intent.putExtra("numero_chamado", chamado.getNumero());
                context.startActivity(intent);

                // Se há listener customizado, chamar também
                if (listener != null) {
                    listener.onChamadoClick(chamado);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chamados.size();
    }

    // ✅ MÉTODO formatarData que estava faltando
    private String formatarData(String dataCompleta) {
        try {
            // Formato: 2025-09-14 20:31:33 -> 14/09/2025
            String[] parts = dataCompleta.split(" ");
            if (parts.length >= 1) {
                String[] dateParts = parts[0].split("-");
                if (dateParts.length >= 3) {
                    return dateParts[2] + "/" + dateParts[1] + "/" + dateParts[0];
                }
            }
        } catch (Exception e) {
            // Se der erro, retorna a data original
        }
        return dataCompleta;
    }

    // Métodos auxiliares (caso precise usar no futuro)
    private String getStatusTexto(int status) {
        switch (status) {
            case Chamado.STATUS_ABERTO: return "ABERTO";
            case Chamado.STATUS_EM_ANDAMENTO: return "EM ANDAMENTO";
            case Chamado.STATUS_RESOLVIDO: return "RESOLVIDO";
            case Chamado.STATUS_FECHADO: return "FECHADO";
            default: return "DESCONHECIDO";
        }
    }

    private int getStatusBackground(int status) {
        switch (status) {
            case Chamado.STATUS_ABERTO: return R.drawable.status_aberto;
            case Chamado.STATUS_EM_ANDAMENTO: return R.drawable.status_progresso;
            case Chamado.STATUS_RESOLVIDO: return R.drawable.status_resolvido;
            case Chamado.STATUS_FECHADO: return R.drawable.status_fechado;
            default: return R.drawable.status_aberto;
        }
    }

    private String getPrioridadeTexto(int prioridade) {
        switch (prioridade) {
            case Chamado.PRIORIDADE_BAIXA: return "🟢 BAIXA";
            case Chamado.PRIORIDADE_MEDIA: return "🟡 MÉDIA";
            case Chamado.PRIORIDADE_ALTA: return "🟠 ALTA";
            case Chamado.PRIORIDADE_CRITICA: return "🔴 CRÍTICA";
            default: return "🟡 MÉDIA";
        }
    }

    public static class ChamadoViewHolder extends RecyclerView.ViewHolder {
        TextView tvNumero, tvTitulo, tvDescricao, tvStatus, tvPrioridade, tvData;

        public ChamadoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNumero = itemView.findViewById(R.id.tvNumero);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrioridade = itemView.findViewById(R.id.tvPrioridade);
            tvData = itemView.findViewById(R.id.tvData);
        }
    }

    // ✅ Método para atualizar lista
    public void atualizarLista(List<Chamado> novosChamados) {
        this.chamados = novosChamados;
        notifyDataSetChanged();
    }
}
