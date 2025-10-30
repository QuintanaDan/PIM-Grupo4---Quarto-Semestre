package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.DetalheChamadoActivity;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.models.Chamado;
import java.util.List;

public class ChamadoRecenteAdapter extends RecyclerView.Adapter<ChamadoRecenteAdapter.ViewHolder> {
    private Context context;
    private List<Chamado> chamados;

    public ChamadoRecenteAdapter(Context context, List<Chamado> chamados) {
        this.context = context;
        this.chamados = chamados;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chamado_recente, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Chamado chamado = chamados.get(position);

        holder.tvProtocolo.setText(chamado.getNumero());
        holder.tvTitulo.setText(chamado.getTitulo());
        holder.tvStatus.setText(chamado.getStatus());
        holder.tvPrioridade.setText(chamado.getPrioridade());

        // Cor do indicador baseada no status
        int corStatus = getCorStatus(chamado.getStatus());
        holder.viewIndicadorStatus.setBackgroundColor(corStatus);

        // Click para abrir detalhes
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetalheChamadoActivity.class);
            intent.putExtra("chamado_id", chamado.getId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return Math.min(chamados.size(), 5); // Mostrar no máximo 5
    }

    private int getCorStatus(String status) {
        switch (status) {
            case "Aberto":
                return context.getResources().getColor(R.color.status_open);
            case "Em Andamento":
                return context.getResources().getColor(R.color.status_in_progress);
            case "Resolvido":
                return context.getResources().getColor(R.color.status_resolved);
            case "Fechado":
                return context.getResources().getColor(R.color.status_closed);
            default:
                return context.getResources().getColor(R.color.text_secondary);
        }
    }

    public void updateList(List<Chamado> novosChamados) {
        this.chamados = novosChamados;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        View viewIndicadorStatus;
        TextView tvProtocolo, tvTitulo, tvStatus, tvPrioridade;

        ViewHolder(View itemView) {
            super(itemView);
            viewIndicadorStatus = itemView.findViewById(R.id.viewIndicadorStatus);
            tvProtocolo = itemView.findViewById(R.id.tvProtocolo);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvPrioridade = itemView.findViewById(R.id.tvPrioridade);
        }
    }
}