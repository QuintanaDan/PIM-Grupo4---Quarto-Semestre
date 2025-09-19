package com.example.helpdeskapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import android.util.Log;

import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.utils.SessionManager;

public class AbrirChamadoActivity extends AppCompatActivity {

    private EditText etTitulo, etDescricao;
    private Spinner spinnerCategoria;
    private RadioGroup rgPrioridade;
    private Button btnEnviarChamado, btnCancelar;

    private SessionManager sessionManager;
    private ChamadoDAO chamadoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_abrir_chamado);

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Abrir Chamado");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inicializarComponentes();
        configurarSpinner();
        configurarEventos();

        sessionManager = new SessionManager(this);
        chamadoDAO = new ChamadoDAO(this);
    }

    private void inicializarComponentes() {
        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        rgPrioridade = findViewById(R.id.rgPrioridade);
        btnEnviarChamado = findViewById(R.id.btnEnviarChamado);
        btnCancelar = findViewById(R.id.btnCancelar);
    }

    private void configurarSpinner() {
        String[] categorias = {
                "Selecione uma categoria",
                "Hardware - Problemas com equipamentos",
                "Software - Problemas com aplicativos",
                "Rede - Problemas de conectividade",
                "Impressora - Problemas com impressão",
                "Email - Problemas com email",
                "Sistema - Problemas no sistema",
                "Outros - Outros tipos de problemas"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categorias);
        spinnerCategoria.setAdapter(adapter);
    }

    private void configurarEventos() {
        btnEnviarChamado.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarChamado();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void enviarChamado() {
        Log.d("AbrirChamado", "=== INICIANDO ENVIO DE CHAMADO ===");

        try {
            String titulo = etTitulo.getText().toString().trim();
            String descricao = etDescricao.getText().toString().trim();
            int categoriaPosition = spinnerCategoria.getSelectedItemPosition();
            int prioridadeSelecionada = obterPrioridadeSelecionada();

            Log.d("AbrirChamado", "Dados coletados:");
            Log.d("AbrirChamado", "  - Título: '" + titulo + "'");
            Log.d("AbrirChamado", "  - Descrição: '" + descricao + "'");
            Log.d("AbrirChamado", "  - Categoria: " + categoriaPosition);
            Log.d("AbrirChamado", "  - Prioridade: " + prioridadeSelecionada);

            // Validações
            if (TextUtils.isEmpty(titulo)) {
                Log.w("AbrirChamado", "❌ Validação falhou: Título vazio");
                etTitulo.setError("Digite o título do problema");
                etTitulo.requestFocus();
                return;
            }

            if (titulo.length() < 5) {
                Log.w("AbrirChamado", "❌ Validação falhou: Título muito curto (" + titulo.length() + " chars)");
                etTitulo.setError("Título deve ter pelo menos 5 caracteres");
                etTitulo.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(descricao)) {
                Log.w("AbrirChamado", "❌ Validação falhou: Descrição vazia");
                etDescricao.setError("Descreva o problema");
                etDescricao.requestFocus();
                return;
            }

            if (descricao.length() < 10) {
                Log.w("AbrirChamado", "❌ Validação falhou: Descrição muito curta (" + descricao.length() + " chars)");
                etDescricao.setError("Descrição deve ter pelo menos 10 caracteres");
                etDescricao.requestFocus();
                return;
            }

            if (categoriaPosition == 0) {
                Log.w("AbrirChamado", "❌ Validação falhou: Nenhuma categoria selecionada");
                Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("AbrirChamado", "✅ Todas as validações passaram");

            // Verificar sessão
            if (sessionManager == null) {
                Log.e("AbrirChamado", "❌ ERRO: SessionManager é null!");
                Toast.makeText(this, "Erro: Sessão não inicializada", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            if (!sessionManager.isLoggedIn()) {
                Log.e("AbrirChamado", "❌ ERRO: Usuário não está logado");
                Toast.makeText(this, "Erro: Usuário não está logado", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            long clienteId = sessionManager.getUserId();
            Log.d("AbrirChamado", "Cliente ID obtido: " + clienteId);

            if (clienteId <= 0) {
                Log.e("AbrirChamado", "❌ ERRO: ID do usuário inválido: " + clienteId);
                Toast.makeText(this, "Erro: ID do usuário inválido", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("AbrirChamado", "✅ Sessão validada");

            // Criar o chamado
            Log.d("AbrirChamado", "Criando objeto Chamado...");
            Chamado novoChamado = new Chamado(titulo, descricao, clienteId);

            if (novoChamado == null) {
                Log.e("AbrirChamado", "❌ ERRO: Falha ao criar objeto Chamado");
                Toast.makeText(this, "Erro interno ao criar chamado", Toast.LENGTH_SHORT).show();
                return;
            }

            novoChamado.setPrioridade(prioridadeSelecionada);

            Log.d("AbrirChamado", "Objeto Chamado criado:");
            Log.d("AbrirChamado", "  - Número: " + novoChamado.getNumero());
            Log.d("AbrirChamado", "  - Título: " + novoChamado.getTitulo());
            Log.d("AbrirChamado", "  - Descrição: " + novoChamado.getDescricao());
            Log.d("AbrirChamado", "  - Prioridade: " + novoChamado.getPrioridade());
            Log.d("AbrirChamado", "  - Cliente ID: " + novoChamado.getClienteId());
            Log.d("AbrirChamado", "  - Status: " + novoChamado.getStatus());

            // Verificar DAO
            if (chamadoDAO == null) {
                Log.e("AbrirChamado", "❌ ERRO: ChamadoDAO é null!");
                Toast.makeText(this, "Erro: DAO não inicializado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Salvar no banco
            Log.d("AbrirChamado", "Abrindo conexão com banco...");
            chamadoDAO.open();

            Log.d("AbrirChamado", "Salvando chamado no banco...");
            long resultado = chamadoDAO.abrirChamado(novoChamado);

            Log.d("AbrirChamado", "Fechando conexão com banco...");
            chamadoDAO.close();

            Log.d("AbrirChamado", "Resultado da operação: " + resultado);

            if (resultado > 0) {
                Log.d("AbrirChamado", "✅ SUCESSO! Chamado salvo com ID: " + resultado);
                Toast.makeText(this, "✅ Chamado " + novoChamado.getNumero() + " aberto com sucesso!", Toast.LENGTH_LONG).show();
                finish();
            } else {
                Log.e("AbrirChamado", "❌ FALHA! Erro ao salvar no banco (resultado: " + resultado + ")");
                Toast.makeText(this, "❌ Erro ao abrir chamado. Tente novamente.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("AbrirChamado", "❌ ERRO CRÍTICO ao enviar chamado: ", e);
            Toast.makeText(this, "❌ Erro crítico: " + e.getMessage(), Toast.LENGTH_LONG).show();

            try {
                if (chamadoDAO != null) {
                    chamadoDAO.close();
                }
            } catch (Exception ex) {
                Log.e("AbrirChamado", "❌ Erro ao fechar DAO: ", ex);
            }
        }

        Log.d("AbrirChamado", "=== FIM DO ENVIO DE CHAMADO ===");
    }



    private int obterPrioridadeSelecionada() {
        int selectedId = rgPrioridade.getCheckedRadioButtonId();

        if (selectedId == R.id.rbBaixa) {
            return Chamado.PRIORIDADE_BAIXA;
        } else if (selectedId == R.id.rbMedia) {
            return Chamado.PRIORIDADE_MEDIA;
        } else if (selectedId == R.id.rbAlta) {
            return Chamado.PRIORIDADE_ALTA;
        } else if (selectedId == R.id.rbCritica) {
            return Chamado.PRIORIDADE_CRITICA;
        }

        return Chamado.PRIORIDADE_MEDIA; // Padrão
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (chamadoDAO != null) {
                chamadoDAO.close();
            }
        } catch (Exception e) {
            // Ignorar erro ao fechar
        }
    }
}
