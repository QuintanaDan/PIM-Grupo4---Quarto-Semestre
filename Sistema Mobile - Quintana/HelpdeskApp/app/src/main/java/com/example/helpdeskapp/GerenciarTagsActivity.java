package com.example.helpdeskapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.example.helpdeskapp.adapters.TagAdapter;
import com.example.helpdeskapp.dao.TagDAO;
import com.example.helpdeskapp.models.Tag;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;
import java.util.ArrayList;
import java.util.List;

public class GerenciarTagsActivity extends AppCompatActivity {
    private static final String TAG = "GerenciarTags";

    private RecyclerView recyclerViewTags;
    private Button btnNovaTag;
    private TagAdapter tagAdapter;
    private TagDAO tagDAO;
    private SessionManager sessionManager;
    private List<Tag> listaTags;

    // Cores pré-definidas
    private final String[] CORES = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7",
            "#3F51B5", "#2196F3", "#00BCD4", "#009688",
            "#4CAF50", "#8BC34A", "#CDDC39", "#FFEB3B",
            "#FFC107", "#FF9800", "#FF5722", "#795548"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gerenciar_tags);

        // Verificar se é admin
        sessionManager = new SessionManager(this);
        if (!sessionManager.isAdmin()) {
            Toast.makeText(this, "❌ Acesso negado! Área restrita a administradores.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("🏷️ Gerenciar Tags");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        carregarTags();
    }

    private void inicializarComponentes() {
        recyclerViewTags = findViewById(R.id.recyclerViewTagsGerenciar);
        btnNovaTag = findViewById(R.id.btnNovaTag);

        tagDAO = new TagDAO(this);
        listaTags = new ArrayList<>();

        // Grid com 2 colunas
        recyclerViewTags.setLayoutManager(new GridLayoutManager(this, 2));

        btnNovaTag.setOnClickListener(v -> mostrarDialogoCriarTag());
    }

    private void carregarTags() {
        try {
            Log.d(TAG, "=== CARREGANDO TAGS ===");
            listaTags = tagDAO.buscarTodasTags();

            if (listaTags == null) {
                listaTags = new ArrayList<>();
            }

            Log.d(TAG, "Total de tags: " + listaTags.size());

            if (tagAdapter == null) {
                tagAdapter = new TagAdapter(listaTags, this,
                        new TagAdapter.OnTagClickListener() {
                            @Override
                            public void onTagClick(Tag tag) {
                                mostrarOpcoesTag(tag);
                            }

                            @Override
                            public void onTagRemoveClick(Tag tag) {
                                // Não usado aqui
                            }
                        }, false);
                recyclerViewTags.setAdapter(tagAdapter);
            } else {
                tagAdapter.atualizarLista(listaTags);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar tags: ", e);
            Toast.makeText(this, "Erro ao carregar tags", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDialogoCriarTag() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_criar_tag, null);

        EditText etNomeTag = dialogView.findViewById(R.id.etNomeTag);
        RecyclerView rvCores = dialogView.findViewById(R.id.rvCoresPaleta);

        // Configurar RecyclerView de cores
        rvCores.setLayoutManager(new GridLayoutManager(this, 4));

        final String[] corSelecionada = {CORES[0]};

        // Adapter para cores
        CorAdapter corAdapter = new CorAdapter(CORES, cor -> corSelecionada[0] = cor);
        rvCores.setAdapter(corAdapter);

        new AlertDialog.Builder(this)
                .setTitle("🏷️ Nova Tag")
                .setView(dialogView)
                .setPositiveButton("Criar", (dialog, which) -> {
                    String nome = etNomeTag.getText().toString().trim();

                    if (nome.isEmpty()) {
                        Toast.makeText(this, "Digite um nome para a tag",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    criarTag(nome, corSelecionada[0]);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void criarTag(String nome, String cor) {
        try {
            Tag novaTag = new Tag(nome, cor);
            long resultado = tagDAO.criarTag(novaTag);

            if (resultado > 0) {
                Log.d(TAG, "✅ Tag criada: " + nome);
                Toast.makeText(this, "✅ Tag '" + nome + "' criada!",
                        Toast.LENGTH_SHORT).show();
                carregarTags();
            } else {
                Toast.makeText(this, "❌ Erro ao criar tag", Toast.LENGTH_SHORT).show();
            }

            if (resultado > 0) {
                Log.d(TAG, "✅ Tag criada: " + nome);

                // NOVO: Registrar na auditoria
                AuditoriaHelper.registrarCriacaoTag(
                        this,
                        sessionManager.getUserId(),
                        nome
                );

                Toast.makeText(this, "✅ Tag '" + nome + "' criada!",
                        Toast.LENGTH_SHORT).show();
                carregarTags();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao criar tag: ", e);
            Toast.makeText(this, "Erro: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarOpcoesTag(Tag tag) {
        int quantidadeChamados = tagDAO.contarChamadosComTag(tag.getId());

        String[] opcoes = {
                "✏️ Editar",
                "🗑️ Deletar",
                "❌ Cancelar"
        };

        new AlertDialog.Builder(this)
                .setTitle(tag.getEmoji() + " " + tag.getNome())
                .setMessage(quantidadeChamados + " chamado(s) com esta tag")
                .setItems(opcoes, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            editarTag(tag);
                            break;
                        case 1:
                            confirmarDeletarTag(tag);
                            break;
                    }
                })
                .show();
    }

    private void editarTag(Tag tag) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_criar_tag, null);

        EditText etNomeTag = dialogView.findViewById(R.id.etNomeTag);
        RecyclerView rvCores = dialogView.findViewById(R.id.rvCoresPaleta);

        etNomeTag.setText(tag.getNome());

        rvCores.setLayoutManager(new GridLayoutManager(this, 4));

        final String[] corSelecionada = {tag.getCor()};

        CorAdapter corAdapter = new CorAdapter(CORES, cor -> corSelecionada[0] = cor);
        rvCores.setAdapter(corAdapter);

        new AlertDialog.Builder(this)
                .setTitle("✏️ Editar Tag")
                .setView(dialogView)
                .setPositiveButton("Salvar", (dialog, which) -> {
                    String nome = etNomeTag.getText().toString().trim();

                    if (nome.isEmpty()) {
                        Toast.makeText(this, "Digite um nome para a tag",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    tag.setNome(nome);
                    tag.setCor(corSelecionada[0]);

                    boolean sucesso = tagDAO.atualizarTag(tag);

                    if (sucesso) {
                        Toast.makeText(this, "✅ Tag atualizada!", Toast.LENGTH_SHORT).show();
                        carregarTags();
                    } else {
                        Toast.makeText(this, "❌ Erro ao atualizar tag",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void confirmarDeletarTag(Tag tag) {
        int quantidadeChamados = tagDAO.contarChamadosComTag(tag.getId());

        String mensagem = quantidadeChamados > 0 ?
                "Esta tag está sendo usada em " + quantidadeChamados + " chamado(s).\n\n" +
                        "Deseja realmente deletar?" :
                "Deseja realmente deletar esta tag?";

        new AlertDialog.Builder(this)
                .setTitle("Deletar Tag")
                .setMessage(mensagem)
                .setPositiveButton("Sim", (dialog, which) -> deletarTag(tag))
                .setNegativeButton("Não", null)
                .show();
    }

    private void deletarTag(Tag tag) {
        try {
            boolean sucesso = tagDAO.deletarTag(tag.getId());

            if (sucesso) {
                Log.d(TAG, "✅ Tag deletada: " + tag.getNome());
                Toast.makeText(this, "✅ Tag deletada!", Toast.LENGTH_SHORT).show();
                carregarTags();
            } else {
                Toast.makeText(this, "❌ Erro ao deletar tag", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao deletar tag: ", e);
            Toast.makeText(this, "Erro ao deletar tag", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ========== CLASSE INTERNA: CorAdapter ==========
    private static class CorAdapter extends RecyclerView.Adapter<CorAdapter.CorViewHolder> {
        private String[] cores;
        private OnCorSelecionadaListener listener;

        interface OnCorSelecionadaListener {
            void onCorSelecionada(String cor);
        }

        CorAdapter(String[] cores, OnCorSelecionadaListener listener) {
            this.cores = cores;
            this.listener = listener;
        }

        @Override
        public CorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_cor, parent, false);
            return new CorViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CorViewHolder holder, int position) {
            String cor = cores[position];
            holder.viewCor.setBackgroundColor(Color.parseColor(cor));
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCorSelecionada(cor);
                }
            });
        }

        @Override
        public int getItemCount() {
            return cores.length;
        }

        static class CorViewHolder extends RecyclerView.ViewHolder {
            View viewCor;

            CorViewHolder(View itemView) {
                super(itemView);
                viewCor = itemView.findViewById(R.id.viewCor);
            }
        }
    }
}