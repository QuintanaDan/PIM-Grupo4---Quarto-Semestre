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
    private static final String TAG = "AbrirChamado";

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

        Log.d(TAG, "Activity inicializada com sucesso");
    }

    private void inicializarComponentes() {
        etTitulo = findViewById(R.id.etTitulo);
        etDescricao = findViewById(R.id.etDescricao);
        spinnerCategoria = findViewById(R.id.spinnerCategoria);
        rgPrioridade = findViewById(R.id.rgPrioridade);
        btnEnviarChamado = findViewById(R.id.btnEnviarChamado);
        btnCancelar = findViewById(R.id.btnCancelar);

        Log.d(TAG, "Componentes inicializados");
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

        Log.d(TAG, "Spinner configurado com " + categorias.length + " categorias");
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
                Log.d(TAG, "Cancelando abertura de chamado");
                finish();
            }
        });

        Log.d(TAG, "Eventos configurados");
    }

    private void enviarChamado() {
        Log.d(TAG, "=== INICIANDO ENVIO DE CHAMADO ===");

        try {
            // Coletar dados do formulário
            String titulo = etTitulo.getText().toString().trim();
            String descricao = etDescricao.getText().toString().trim();
            int categoriaPosition = spinnerCategoria.getSelectedItemPosition();
            String categoria = categoriaPosition > 0 ?
                    spinnerCategoria.getSelectedItem().toString() : "";
            String prioridadeSelecionada = obterPrioridadeSelecionada();

            Log.d(TAG, "Dados coletados:");
            Log.d(TAG, "  - Título: '" + titulo + "'");
            Log.d(TAG, "  - Descrição: '" + descricao + "'");
            Log.d(TAG, "  - Categoria posição: " + categoriaPosition);
            Log.d(TAG, "  - Categoria: '" + categoria + "'");
            Log.d(TAG, "  - Prioridade: " + prioridadeSelecionada);

            // Validações
            if (!validarFormulario(titulo, descricao, categoriaPosition)) {
                Log.w(TAG, "❌ Validação do formulário falhou");
                return;
            }

            // Verificar sessão
            if (!validarSessao()) {
                Log.e(TAG, "❌ Validação da sessão falhou");
                return;
            }

            long clienteId = sessionManager.getUserId();
            Log.d(TAG, "Cliente ID obtido: " + clienteId);

            // Criar o chamado
            Log.d(TAG, "Criando objeto Chamado...");
            Chamado novoChamado = new Chamado(titulo, descricao, clienteId);

            if (novoChamado == null) {
                Log.e(TAG, "❌ ERRO: Falha ao criar objeto Chamado");
                Toast.makeText(this, "Erro interno ao criar chamado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Configurar dados adicionais do chamado
            novoChamado.setPrioridade(prioridadeSelecionada);
            novoChamado.setCategoria(categoria);

            Log.d(TAG, "Objeto Chamado criado:");
            Log.d(TAG, "  - Protocolo: " + novoChamado.getProtocoloFormatado());
            Log.d(TAG, "  - Título: " + novoChamado.getTitulo());
            Log.d(TAG, "  - Descrição: " + novoChamado.getDescricao());
            Log.d(TAG, "  - Categoria: " + novoChamado.getCategoria());
            Log.d(TAG, "  - Prioridade: " + novoChamado.getPrioridade());
            Log.d(TAG, "  - Cliente ID: " + novoChamado.getClienteId());
            Log.d(TAG, "  - Status: " + novoChamado.getStatus());

            // Verificar DAO
            if (chamadoDAO == null) {
                Log.e(TAG, "❌ ERRO: ChamadoDAO é null!");
                Toast.makeText(this, "Erro: DAO não inicializado", Toast.LENGTH_SHORT).show();
                return;
            }

            // Salvar no banco
            Log.d(TAG, "Salvando chamado no banco...");
            long resultado = chamadoDAO.abrirChamado(novoChamado);
            Log.d(TAG, "Resultado da operação: " + resultado);

            if (resultado > 0) {
                Log.d(TAG, "✅ SUCESSO! Chamado salvo com ID: " + resultado);

                // Atualizar o ID do chamado para gerar protocolo correto
                novoChamado.setId(resultado);

                // Log de debug adicional
                Log.d(TAG, "=== DEBUG INFO APÓS INSERÇÃO ===");
                Log.d(TAG, "ID retornado: " + resultado);
                Log.d(TAG, "Protocolo final: " + novoChamado.getProtocoloFormatado());
                Log.d(TAG, "Data de criação: " + novoChamado.getDataCriacaoFormatada());

                // Verificar se o chamado foi realmente inserido
                Chamado chamadoVerificacao = chamadoDAO.buscarChamadoPorId(resultado);
                if (chamadoVerificacao != null) {
                    Log.d(TAG, "✅ Verificação: Chamado encontrado no banco");
                    Log.d(TAG, "Título verificado: " + chamadoVerificacao.getTitulo());
                } else {
                    Log.w(TAG, "⚠️ Atenção: Chamado não encontrado na verificação");
                }

                Toast.makeText(this,
                        "✅ Chamado " + novoChamado.getProtocoloFormatado() + " aberto com sucesso!",
                        Toast.LENGTH_LONG).show();

                // Fechar a activity após sucesso
                finish();

            } else {
                Log.e(TAG, "❌ FALHA! Erro ao salvar no banco (resultado: " + resultado + ")");
                Toast.makeText(this, "❌ Erro ao abrir chamado. Tente novamente.",
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO CRÍTICO ao enviar chamado: ", e);
            Toast.makeText(this, "❌ Erro crítico: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }

        Log.d(TAG, "=== FIM DO ENVIO DE CHAMADO ===");
    }

    private boolean validarFormulario(String titulo, String descricao, int categoriaPosition) {
        if (TextUtils.isEmpty(titulo)) {
            Log.w(TAG, "❌ Validação falhou: Título vazio");
            etTitulo.setError("Digite o título do problema");
            etTitulo.requestFocus();
            return false;
        }

        if (titulo.length() < 5) {
            Log.w(TAG, "❌ Validação falhou: Título muito curto (" + titulo.length() + " chars)");
            etTitulo.setError("Título deve ter pelo menos 5 caracteres");
            etTitulo.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(descricao)) {
            Log.w(TAG, "❌ Validação falhou: Descrição vazia");
            etDescricao.setError("Descreva o problema");
            etDescricao.requestFocus();
            return false;
        }

        if (descricao.length() < 10) {
            Log.w(TAG, "❌ Validação falhou: Descrição muito curta (" + descricao.length() + " chars)");
            etDescricao.setError("Descrição deve ter pelo menos 10 caracteres");
            etDescricao.requestFocus();
            return false;
        }

        if (categoriaPosition == 0) {
            Log.w(TAG, "❌ Validação falhou: Nenhuma categoria selecionada");
            Toast.makeText(this, "Selecione uma categoria", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "✅ Todas as validações do formulário passaram");
        return true;
    }

    private boolean validarSessao() {
        if (sessionManager == null) {
            Log.e(TAG, "❌ ERRO: SessionManager é null!");
            Toast.makeText(this, "Erro: Sessão não inicializada", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        if (!sessionManager.isLoggedIn()) {
            Log.e(TAG, "❌ ERRO: Usuário não está logado");
            Toast.makeText(this, "Erro: Usuário não está logado", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }

        long clienteId = sessionManager.getUserId();
        if (clienteId <= 0) {
            Log.e(TAG, "❌ ERRO: ID do usuário inválido: " + clienteId);
            Toast.makeText(this, "Erro: ID do usuário inválido", Toast.LENGTH_SHORT).show();
            return false;
        }

        Log.d(TAG, "✅ Sessão validada com sucesso");
        return true;
    }

    private String obterPrioridadeSelecionada() {
        int selectedId = rgPrioridade.getCheckedRadioButtonId();

        if (selectedId == R.id.rbBaixa) {
            return "Baixa";
        } else if (selectedId == R.id.rbMedia) {
            return "Média";
        } else if (selectedId == R.id.rbAlta) {
            return "Alta";
        } else if (selectedId == R.id.rbCritica) {
            return "Crítica";
        }

        // Se nenhuma prioridade foi selecionada, selecionar Média como padrão
        Log.d(TAG, "Nenhuma prioridade selecionada, usando 'Média' como padrão");
        return "Média";
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navegação para trás pressionada");
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destruída");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Activity pausada");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity retomada");
    }
}
