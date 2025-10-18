package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.DetalheChamadoActivity;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.dao.NotificacaoDAO;
import com.example.helpdeskapp.models.Notificacao;
import java.util.List;

public class NotificacaoAdapter extends RecyclerView.Adapter<NotificacaoAdapter.ViewHolder> {
    private Context context;
    private List<Notificacao> notificacoes;
    private NotificacaoDAO notificacaoDAO;
    private OnNotificacaoClickListener listener;

    public interface OnNotificacaoClickListener {
        void onNotificacaoClick(Notificacao notificacao);
        void onNotificacaoLongClick(Notificacao notificacao);
    }

    public NotificacaoAdapter(Context context, List<Notificacao> notificacoes) {
        this.context = context;
        this.notificacoes = notificacoes;
        this.notificacaoDAO = new NotificacaoDAO(context);
    }

    public void setOnNotificacaoClickListener(OnNotificacaoClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notificacao, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notificacao notificacao = notificacoes.get(position);

        // Ícone do tipo
        holder.tvIcone.setText(notificacao.getIcone());

        // Título
        holder.tvTitulo.setText(notificacao.getTitulo());

        // Mensagem
        holder.tvMensagem.setText(notificacao.getMensagem());

        // Tempo decorrido
        holder.tvTempo.setText(getTempoDecorrido(notificacao.getCreatedAt()));

        // Indicador de não lida
        if (notificacao.isLida()) {
            holder.viewIndicadorNaoLida.setVisibility(View.GONE);
            holder.tvTitulo.setTypeface(null, Typeface.NORMAL);
            holder.tvMensagem.setTypeface(null, Typeface.NORMAL);
            holder.cardNotificacao.setCardBackgroundColor(
                    context.getResources().getColor(android.R.color.transparent)
            );
        } else {
            holder.viewIndicadorNaoLida.setVisibility(View.VISIBLE);
            holder.tvTitulo.setTypeface(null, Typeface.BOLD);
            holder.tvMensagem.setTypeface(null, Typeface.BOLD);
            holder.cardNotificacao.setCardBackgroundColor(
                    context.getResources().getColor(R.color.notificacao_nao_lida)
            );
        }

        // Cor do indicador baseada no tipo
        holder.viewIndicadorNaoLida.setBackgroundColor(notificacao.getCorTipo());

        // Click
        holder.itemView.setOnClickListener(v -> {
            // Marcar como lida
            if (!notificacao.isLida()) {
                notificacaoDAO.marcarComoLida(notificacao.getId());
                notificacao.setLida(true);
                notifyItemChanged(position);
            }

            // Abrir chamado se tiver
            if (notificacao.getChamadoId() > 0) {
                Intent intent = new Intent(context, DetalheChamadoActivity.class);
                intent.putExtra("chamado_id", notificacao.getChamadoId());
                context.startActivity(intent);
            }

            if (listener != null) {
                listener.onNotificacaoClick(notificacao);
            }
        });

        // Long click
        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onNotificacaoLongClick(notificacao);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return notificacoes.size();
    }

    public void updateList(List<Notificacao> novasNotificacoes) {
        this.notificacoes = novasNotificacoes;
        notifyDataSetChanged();
    }

    private String getTempoDecorrido(String createdAt) {

        try {

            return "Agora";
        } catch (Exception e) {
            return "";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardNotificacao;
        TextView tvIcone, tvTitulo, tvMensagem, tvTempo;
        View viewIndicadorNaoLida;

        ViewHolder(View itemView) {
            super(itemView);
            cardNotificacao = itemView.findViewById(R.id.cardNotificacao);
            tvIcone = itemView.findViewById(R.id.tvIcone);
            tvTitulo = itemView.findViewById(R.id.tvTitulo);
            tvMensagem = itemView.findViewById(R.id.tvMensagem);
            tvTempo = itemView.findViewById(R.id.tvTempo);
            viewIndicadorNaoLida = itemView.findViewById(R.id.viewIndicadorNaoLida);
        }
    }
}