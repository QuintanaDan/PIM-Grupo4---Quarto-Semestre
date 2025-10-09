package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.DetalheChamadoActivity;
import java.util.List;

public class ChamadoAdapter extends RecyclerView.Adapter<ChamadoAdapter.ChamadoViewHolder> {
    private static final String TAG = "ChamadoAdapter";

    private Context context;
    private List<Chamado> listaChamados;
    private OnChamadoClickListener clickListener;

    public interface OnChamadoClickListener {
        void onChamadoClick(Chamado chamado, int position);
        void onChamadoLongClick(Chamado chamado, int position);
    }

    public ChamadoAdapter(Context context, List<Chamado> listaChamados) {
        this.context = context;
        this.listaChamados = listaChamados;
        this.clickListener = null;
        Log.d(TAG, "Adapter criado com " + (listaChamados != null ? listaChamados.size() : 0) + " itens");
    }

    public ChamadoAdapter(Context context, List<Chamado> listaChamados, OnChamadoClickListener listener) {
        this.context = context;
        this.listaChamados = listaChamados;
        this.clickListener = listener;
        Log.d(TAG, "Adapter criado com listener personalizado e " +
                (listaChamados != null ? listaChamados.size() : 0) + " itens");
    }

    public void setOnChamadoClickListener(OnChamadoClickListener listener) {
        this.clickListener = listener;
        Log.d(TAG, "Listener definido: " + (listener != null ? "personalizado" : "removido"));
    }

    @NonNull
    @Override
    public ChamadoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_chamado, parent, false);
        return new ChamadoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChamadoViewHolder holder, int position) {
        if (listaChamados == null || position >= listaChamados.size()) {
            Log.w(TAG, "Lista vazia ou posição inválida: " + position);
            return;
        }

        Chamado chamado = listaChamados.get(position);
        if (chamado == null) {
            Log.w(TAG, "Chamado nulo na posição: " + position);
            return;
        }

        Log.d(TAG, "Binding chamado na posição " + position + ": " + chamado.getTitulo());

        bindTexto(holder.txtNumero, chamado.getProtocoloFormatado(), "Nº não disponível");
        bindTexto(holder.txtTitulo, chamado.getTitulo(), "Título não disponível");
        bindTexto(holder.txtDescricao, limitarTexto(chamado.getDescricao(), 100), "Descrição não disponível");
        bindTexto(holder.txtCategoria, chamado.getCategoria(), "Categoria não definida");
        bindTexto(holder.txtStatus, chamado.getStatus(), "Status não definido");
        bindTexto(holder.txtPrioridade, chamado.getPrioridade(), "Prioridade não definida");
        bindTexto(holder.txtData, chamado.getDataCriacaoFormatada(), "Data não disponível");

        aplicarCorStatus(holder.txtStatus, chamado);
        aplicarCorPrioridade(holder.txtPrioridade, chamado);

        holder.cardView.setOnClickListener(v -> {
            Log.d(TAG, "Clique no chamado: " + chamado.getProtocoloFormatado());
            if (clickListener != null) {
                clickListener.onChamadoClick(chamado, position);
            } else {
                abrirDetalhes(chamado);
            }
        });

        holder.cardView.setOnLongClickListener(v -> {
            Log.d(TAG, "Clique longo no chamado: " + chamado.getProtocoloFormatado());
            if (clickListener != null) {
                clickListener.onChamadoLongClick(chamado, position);
                return true;
            }
            return false;
        });
    }

    private void bindTexto(TextView textView, String texto, String textoDefault) {
        if (textView != null) {
            if (texto != null && !texto.trim().isEmpty()) {
                textView.setText(texto);
                textView.setVisibility(View.VISIBLE);
            } else {
                textView.setText(textoDefault);
                textView.setVisibility(View.VISIBLE);
            }
        }
    }

    private String limitarTexto(String texto, int limite) {
        if (texto == null) return null;
        if (texto.length() <= limite) return texto;
        return texto.substring(0, limite) + "...";
    }

    private void aplicarCorStatus(TextView textView, Chamado chamado) {
        if (textView == null || chamado == null) return;

        try {
            String corStatus = chamado.getCorStatus();
            if (corStatus != null && !corStatus.isEmpty()) {
                textView.setTextColor(Color.parseColor(corStatus));
            } else {
                String status = chamado.getStatus();
                if (status != null) {
                    switch (status.toLowerCase()) {
                        case "aberto":
                            textView.setTextColor(Color.parseColor("#FF9800"));
                            break;
                        case "em andamento":
                            textView.setTextColor(Color.parseColor("#2196F3"));
                            break;
                        case "resolvido":
                            textView.setTextColor(Color.parseColor("#4CAF50"));
                            break;
                        case "fechado":
                            textView.setTextColor(Color.parseColor("#9E9E9E"));
                            break;
                        case "cancelado":
                            textView.setTextColor(Color.parseColor("#F44336"));
                            break;
                        default:
                            textView.setTextColor(Color.GRAY);
                    }
                } else {
                    textView.setTextColor(Color.GRAY);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Erro ao aplicar cor do status: " + e.getMessage());
            textView.setTextColor(Color.GRAY);
        }
    }

    private void aplicarCorPrioridade(TextView textView, Chamado chamado) {
        if (textView == null || chamado == null) return;

        try {
            String corPrioridade = chamado.getCorPrioridade();
            if (corPrioridade != null && !corPrioridade.isEmpty()) {
                textView.setTextColor(Color.parseColor(corPrioridade));
            } else {
                String prioridade = chamado.getPrioridade();
                if (prioridade != null) {
                    switch (prioridade.toLowerCase()) {
                        case "baixa":
                            textView.setTextColor(Color.parseColor("#4CAF50"));
                            break;
                        case "média":
                            textView.setTextColor(Color.parseColor("#FF9800"));
                            break;
                        case "alta":
                            textView.setTextColor(Color.parseColor("#FF5722"));
                            break;
                        case "crítica":
                            textView.setTextColor(Color.parseColor("#F44336"));
                            break;
                        default:
                            textView.setTextColor(Color.GRAY);
                    }
                } else {
                    textView.setTextColor(Color.GRAY);
                }
            }
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "Erro ao aplicar cor da prioridade: " + e.getMessage());
            textView.setTextColor(Color.GRAY);
        }
    }

    private void abrirDetalhes(Chamado chamado) {
        if (chamado == null) {
            Log.e(TAG, "Tentativa de abrir detalhes com chamado nulo");
            return;
        }

        try {
            Intent intent = new Intent(context, DetalheChamadoActivity.class);
            intent.putExtra("chamado_id", chamado.getId());
            intent.putExtra("chamado_protocolo", chamado.getProtocoloFormatado());
            intent.putExtra("chamado_titulo", chamado.getTitulo());
            intent.putExtra("chamado_descricao", chamado.getDescricao());
            intent.putExtra("chamado_categoria", chamado.getCategoria());
            intent.putExtra("chamado_prioridade", chamado.getPrioridade());
            intent.putExtra("chamado_status", chamado.getStatus());
            intent.putExtra("chamado_data", chamado.getDataCriacaoFormatada());
            intent.putExtra("chamado_resposta", chamado.getResposta());
            intent.putExtra("chamado_cliente_id", chamado.getClienteId());

            context.startActivity(intent);
            Log.d(TAG, "Abrindo detalhes do chamado: " + chamado.getProtocoloFormatado());
        } catch (Exception e) {
            Log.e(TAG, "Erro ao abrir detalhes do chamado", e);
        }
    }

    @Override
    public int getItemCount() {
        int count = listaChamados != null ? listaChamados.size() : 0;
        Log.d(TAG, "getItemCount retornando: " + count);
        return count;
    }

    public void updateList(List<Chamado> novaLista) {
        Log.d(TAG, "Atualizando lista. Itens antigos: " +
                (this.listaChamados != null ? this.listaChamados.size() : 0) +
                ", novos: " + (novaLista != null ? novaLista.size() : 0));

        this.listaChamados = novaLista;
        notifyDataSetChanged();
    }

    public void addItem(Chamado chamado) {
        if (listaChamados != null && chamado != null) {
            listaChamados.add(chamado);
            int position = listaChamados.size() - 1;
            notifyItemInserted(position);
            Log.d(TAG, "Item adicionado na posição: " + position);
        } else {
            Log.w(TAG, "Não foi possível adicionar item: lista=" +
                    (listaChamados != null) + ", chamado=" + (chamado != null));
        }
    }

    public void removeItem(int position) {
        if (listaChamados != null && position >= 0 && position < listaChamados.size()) {
            Chamado removido = listaChamados.remove(position);
            notifyItemRemoved(position);
            Log.d(TAG, "Item removido da posição " + position + ": " +
                    (removido != null ? removido.getTitulo() : "null"));
        } else {
            Log.w(TAG, "Não foi possível remover item da posição: " + position);
        }
    }

    public void updateItem(int position, Chamado chamado) {
        if (listaChamados != null && position >= 0 && position < listaChamados.size() && chamado != null) {
            listaChamados.set(position, chamado);
            notifyItemChanged(position);
            Log.d(TAG, "Item atualizado na posição " + position + ": " + chamado.getTitulo());
        } else {
            Log.w(TAG, "Não foi possível atualizar item na posição: " + position);
        }
    }

    public Chamado getItem(int position) {
        if (listaChamados != null && position >= 0 && position < listaChamados.size()) {
            return listaChamados.get(position);
        }
        return null;
    }

    public void clearList() {
        if (listaChamados != null) {
            int size = listaChamados.size();
            listaChamados.clear();
            notifyDataSetChanged();
            Log.d(TAG, "Lista limpa. " + size + " itens removidos");
        }
    }

    public static class ChamadoViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView txtNumero, txtTitulo, txtDescricao, txtCategoria, txtStatus, txtPrioridade, txtData;

        public ChamadoViewHolder(@NonNull View itemView) {
            super(itemView);

            if (itemView instanceof CardView) {
                cardView = (CardView) itemView;
            } else {
                cardView = itemView.findViewById(R.id.cardView);
                if (cardView == null) {
                    cardView = null;
                }
            }

            txtNumero = itemView.findViewById(R.id.tvNumeroChamado);
            txtTitulo = itemView.findViewById(R.id.tvTitulo);
            txtDescricao = itemView.findViewById(R.id.tvDescricao);
            txtCategoria = itemView.findViewById(R.id.tvCategoria);
            txtStatus = itemView.findViewById(R.id.tvStatusChamado);
            txtPrioridade = itemView.findViewById(R.id.tvPrioridade);
            txtData = itemView.findViewById(R.id.tvDataCriacao);

            if (cardView == null) {
                cardView = new CardView(itemView.getContext()) {
                    @Override
                    public void setOnClickListener(OnClickListener l) {
                        itemView.setOnClickListener(l);
                    }

                    @Override
                    public void setOnLongClickListener(OnLongClickListener l) {
                        itemView.setOnLongClickListener(l);
                    }
                };
            }

            Log.d("ChamadoViewHolder", "Componentes inicializados:");
            Log.d("ChamadoViewHolder", "  - CardView: " + (cardView != null));
            Log.d("ChamadoViewHolder", "  - txtNumero: " + (txtNumero != null));
            Log.d("ChamadoViewHolder", "  - txtTitulo: " + (txtTitulo != null));
            Log.d("ChamadoViewHolder", "  - txtDescricao: " + (txtDescricao != null));
            Log.d("ChamadoViewHolder", "  - txtCategoria: " + (txtCategoria != null));
            Log.d("ChamadoViewHolder", "  - txtStatus: " + (txtStatus != null));
            Log.d("ChamadoViewHolder", "  - txtPrioridade: " + (txtPrioridade != null));
            Log.d("ChamadoViewHolder", "  - txtData: " + (txtData != null));
        }
    }
}