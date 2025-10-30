package com.example.helpdeskapp.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.models.Tag;
import java.util.List;

public class TagAdapter extends RecyclerView.Adapter<TagAdapter.TagViewHolder> {
    private List<Tag> listaTags;
    private Context context;
    private OnTagClickListener listener;
    private boolean mostrarBotaoRemover;

    public interface OnTagClickListener {
        void onTagClick(Tag tag);
        void onTagRemoveClick(Tag tag);
    }

    public TagAdapter(List<Tag> listaTags, Context context, OnTagClickListener listener, boolean mostrarBotaoRemover) {
        this.listaTags = listaTags;
        this.context = context;
        this.listener = listener;
        this.mostrarBotaoRemover = mostrarBotaoRemover;
    }

    @NonNull
    @Override
    public TagViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tag, parent, false);
        return new TagViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TagViewHolder holder, int position) {
        Tag tag = listaTags.get(position);

        // Nome da tag com emoji
        holder.tvNomeTag.setText(tag.getEmoji() + " " + tag.getNome());

        // Cor de fundo
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(50); // Bordas arredondadas

        try {
            int cor = Color.parseColor(tag.getCor());
            drawable.setColor(cor);

            // Texto branco ou preto dependendo da cor de fundo
            if (isColorDark(cor)) {
                holder.tvNomeTag.setTextColor(Color.WHITE);
            } else {
                holder.tvNomeTag.setTextColor(Color.BLACK);
            }
        } catch (Exception e) {
            drawable.setColor(Color.GRAY);
            holder.tvNomeTag.setTextColor(Color.WHITE);
        }

        holder.itemView.setBackground(drawable);

        // Botão remover
        if (mostrarBotaoRemover) {
            holder.btnRemover.setVisibility(View.VISIBLE);
            holder.btnRemover.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTagRemoveClick(tag);
                }
            });
        } else {
            holder.btnRemover.setVisibility(View.GONE);
        }

        // Clique no item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTagClick(tag);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaTags.size();
    }

    public void atualizarLista(List<Tag> novasTags) {
        this.listaTags.clear();
        this.listaTags.addAll(novasTags);
        notifyDataSetChanged();
    }

    // Verificar se a cor é escura
    private boolean isColorDark(int color) {
        double darkness = 1 - (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return darkness >= 0.5;
    }

    static class TagViewHolder extends RecyclerView.ViewHolder {
        TextView tvNomeTag;
        ImageButton btnRemover;

        public TagViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNomeTag = itemView.findViewById(R.id.tvNomeTag);
            btnRemover = itemView.findViewById(R.id.btnRemoverTag);
        }
    }
}