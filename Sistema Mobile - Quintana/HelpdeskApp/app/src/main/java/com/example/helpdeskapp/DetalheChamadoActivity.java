package com.example.helpdeskapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.helpdeskapp.adapters.AnexoAdapter;
import com.example.helpdeskapp.adapters.ComentarioAdapter;
import com.example.helpdeskapp.adapters.TagAdapter;
import com.example.helpdeskapp.dao.AnexoDAO;
import com.example.helpdeskapp.dao.AvaliacaoDAO;
import com.example.helpdeskapp.dao.ComentarioDAO;
import com.example.helpdeskapp.dao.TagDAO;
import com.example.helpdeskapp.models.Anexo;
import com.example.helpdeskapp.models.Avaliacao;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.models.Comentario;
import com.example.helpdeskapp.models.Tag;
import com.example.helpdeskapp.utils.FileHelper;
import com.example.helpdeskapp.utils.SessionManager;
import com.example.helpdeskapp.utils.NotificationHelper;
import com.example.helpdeskapp.utils.PDFHelper;
import com.example.helpdeskapp.helpers.AuditoriaHelper;
import com.example.helpdeskapp.utils.ThemeManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DetalheChamadoActivity extends AppCompatActivity {
    private static final String TAG = "DetalheChamadoActivity";

    private Chamado chamado;

    private Button btnGerarPDF;

    // Componentes da interface - Informações do Chamado
    private TextView txtTituloDetalhe;
    private TextView txtDescricaoDetalhe;
    private TextView txtCategoriaDetalhe;
    private TextView txtPrioridadeDetalhe;
    private TextView txtStatusDetalhe;
    private TextView txtDataDetalhe;
    private TextView txtRespostaDetalhe;

    // Botões
    private Button btnVoltar;
    private Button btnAvaliarChamado;
    private Button btnAnexarFoto; // NOVO

    // Componentes de Comentários
    private RecyclerView recyclerViewComentarios;
    private EditText etNovoComentario;
    private Button btnEnviarComentario;

    // NOVO: Componentes de Anexos
    private RecyclerView recyclerViewAnexos;
    private TextView tvContadorAnexos;

    // Adapters e DAOs
    private ComentarioAdapter comentarioAdapter;
    private AnexoAdapter anexoAdapter; // NOVO
    private ComentarioDAO comentarioDAO;
    private AvaliacaoDAO avaliacaoDAO;
    private AnexoDAO anexoDAO; // NOVO
    private SessionManager sessionManager;

    // Dados
    private long chamadoId;
    private List<Comentario> listaComentarios;
    private List<Anexo> listaAnexos; // NOVO

    // NOVO: Gerenciamento de fotos
    private File fotoAtual;
    private Uri fotoUri;

    // NOVO: Launchers para câmera e galeria
    private ActivityResultLauncher<Intent> cameraLauncher;
    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<String> permissaoCameraLauncher;
    private ActivityResultLauncher<String> permissaoGaleriaLauncher;

    private RecyclerView recyclerViewTags;
    private TextView tvAdicionarTag;
    private TagAdapter tagAdapter;
    private TagDAO tagDAO;
    private List<Tag> listaTagsChamado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        new ThemeManager(this).applyTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhes_chamado);

        // Inicializar DAOs e Session
        comentarioDAO = new ComentarioDAO(this);
        avaliacaoDAO = new AvaliacaoDAO(this);
        anexoDAO = new AnexoDAO(this); // NOVO
        sessionManager = new SessionManager(this);
        listaComentarios = new ArrayList<>();
        listaAnexos = new ArrayList<>(); // NOVO

        // NOVO: Inicializar launchers
        inicializarLaunchers();

        // Inicializar componentes
        inicializarViews();

        // Receber dados do Intent
        receberDadosChamado();

        // Configurar eventos
        configurarEventos();

        // Configurar ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Detalhes do Chamado");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Carregar comentários e anexos
        carregarComentarios();
        carregarAnexos(); // NOVO
    }

    private void inicializarViews() {
        // Informações do Chamado
        txtTituloDetalhe = findViewById(R.id.txtTituloDetalhe);
        txtDescricaoDetalhe = findViewById(R.id.txtDescricaoDetalhe);
        txtCategoriaDetalhe = findViewById(R.id.txtCategoriaDetalhe);
        txtPrioridadeDetalhe = findViewById(R.id.txtPrioridadeDetalhe);
        txtStatusDetalhe = findViewById(R.id.txtStatusDetalhe);
        txtDataDetalhe = findViewById(R.id.txtDataDetalhe);
        txtRespostaDetalhe = findViewById(R.id.txtRespostaDetalhe);

        // Botões
        btnVoltar = findViewById(R.id.btnVoltar);
        btnAvaliarChamado = findViewById(R.id.btnAvaliarChamado);
        btnAnexarFoto = findViewById(R.id.btnAnexarFoto); // NOVO
        btnGerarPDF = findViewById(R.id.btnGerarPDF);

        // Componentes de Comentários
        recyclerViewComentarios = findViewById(R.id.recyclerViewComentarios);
        etNovoComentario = findViewById(R.id.etNovoComentario);
        btnEnviarComentario = findViewById(R.id.btnEnviarComentario);

        // NOVO: Componentes de Anexos
        recyclerViewAnexos = findViewById(R.id.recyclerViewAnexos);
        tvContadorAnexos = findViewById(R.id.tvContadorAnexos);

        // Configurar RecyclerViews
        recyclerViewComentarios.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewAnexos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)); // NOVO

        recyclerViewTags = findViewById(R.id.recyclerViewTags);
        tvAdicionarTag = findViewById(R.id.tvAdicionarTag);

        LinearLayoutManager layoutManagerTags = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        recyclerViewTags.setLayoutManager(layoutManagerTags);

        tagDAO = new TagDAO(this);
        listaTagsChamado = new ArrayList<>();
        Log.d(TAG, "Views inicializadas com sucesso");
    }

    // NOVO: Inicializar Launchers
    private void inicializarLaunchers() {
        // Launcher para câmera
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.d(TAG, "✅ Foto capturada com sucesso");
                        processarFotoCapturada();
                    } else {
                        Log.w(TAG, "⚠️ Captura de foto cancelada");
                    }
                }
        );

        // Launcher para galeria
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri selectedImage = result.getData().getData();
                        if (selectedImage != null) {
                            Log.d(TAG, "✅ Imagem selecionada da galeria");
                            processarImagemGaleria(selectedImage);
                        }
                    }
                }
        );

        // Launcher para permissão de câmera
        permissaoCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Permissão de câmera concedida");
                        abrirCamera();
                    } else {
                        Toast.makeText(this, "❌ Permissão de câmera negada", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Launcher para permissão de galeria
        permissaoGaleriaLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "✅ Permissão de galeria concedida");
                        abrirGaleria();
                    } else {
                        Toast.makeText(this, "❌ Permissão de galeria negada", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void configurarEventos() {
        // Botão Voltar
        btnVoltar.setOnClickListener(v -> finish());

        // Botão Enviar Comentário
        btnEnviarComentario.setOnClickListener(v -> enviarComentario());

        // NOVO: Botão Anexar Foto
        btnAnexarFoto.setOnClickListener(v -> mostrarDialogoAnexar());

        btnGerarPDF.setOnClickListener(v -> mostrarDialogoGerarPDF());

        tvAdicionarTag.setOnClickListener(v -> mostrarDialogoAdicionarTag());

        Log.d(TAG, "Eventos configurados");
    }

    // NOVO: Mostrar diálogo para escolher entre câmera ou galeria
    private void mostrarDialogoAnexar() {
        String[] opcoes = {"📷 Tirar Foto", "🖼️ Escolher da Galeria"};

        new AlertDialog.Builder(this)
                .setTitle("Adicionar Anexo")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        solicitarPermissaoCamera();
                    } else {
                        solicitarPermissaoGaleria();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // NOVO: Solicitar permissão de câmera
    private void solicitarPermissaoCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            abrirCamera();
        } else {
            permissaoCameraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // NOVO: Solicitar permissão de galeria
    private void solicitarPermissaoGaleria() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                permissaoGaleriaLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Android 12 e anteriores
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                abrirGaleria();
            } else {
                permissaoGaleriaLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    // NOVO: Abrir câmera
    private void abrirCamera() {
        try {
            fotoAtual = FileHelper.criarArquivoFoto(this);
            fotoUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    fotoAtual);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fotoUri);
            cameraLauncher.launch(intent);

            Log.d(TAG, "📷 Câmera aberta");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao abrir câmera: ", e);
            Toast.makeText(this, "Erro ao abrir câmera", Toast.LENGTH_SHORT).show();
        }
    }

    // NOVO: Abrir galeria
    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galeriaLauncher.launch(intent);
        Log.d(TAG, "🖼️ Galeria aberta");
    }

    // NOVO: Processar foto capturada
    private void processarFotoCapturada() {
        if (fotoAtual != null && fotoAtual.exists()) {
            salvarAnexo(fotoAtual);
        } else {
            Toast.makeText(this, "Erro ao processar foto", Toast.LENGTH_SHORT).show();
        }
    }

    // NOVO: Processar imagem da galeria
    private void processarImagemGaleria(Uri imageUri) {
        try {
            String nomeArquivo = "IMG_" + System.currentTimeMillis() + ".jpg";
            File arquivo = FileHelper.copiarArquivo(this, imageUri, nomeArquivo);

            if (arquivo != null) {
                salvarAnexo(arquivo);
            } else {
                Toast.makeText(this, "Erro ao copiar imagem", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao processar imagem da galeria: ", e);
            Toast.makeText(this, "Erro ao processar imagem", Toast.LENGTH_SHORT).show();
        }
    }

    // NOVO: Salvar anexo no banco
    private void salvarAnexo(File arquivo) {
        try {
            Anexo anexo = new Anexo();
            anexo.setChamadoId(chamadoId);
            anexo.setNomeArquivo(arquivo.getName());
            anexo.setCaminho(arquivo.getAbsolutePath());
            anexo.setTipo(FileHelper.obterTipoMime(arquivo.getName()));
            anexo.setTamanho(FileHelper.obterTamanhoArquivo(arquivo));

            long resultado = anexoDAO.inserirAnexo(anexo);

            if (resultado > 0) {
                Log.d(TAG, "✅ Anexo salvo com sucesso: " + resultado);
                Toast.makeText(this, "✅ Foto anexada com sucesso!", Toast.LENGTH_SHORT).show();
                carregarAnexos();
            } else {
                Toast.makeText(this, "❌ Erro ao salvar anexo", Toast.LENGTH_SHORT).show();
            }

            if (resultado > 0) {
                Log.d(TAG, "✅ Anexo salvo com sucesso: " + resultado);

                // NOVO: Registrar na auditoria
                AuditoriaHelper.registrarAnexo(
                        this,
                        sessionManager.getUserId(),
                        chamadoId,
                        arquivo.getName()
                );

                Toast.makeText(this, "✅ Foto anexada com sucesso!", Toast.LENGTH_SHORT).show();
                carregarAnexos();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao salvar anexo: ", e);
            Toast.makeText(this, "Erro ao salvar anexo", Toast.LENGTH_SHORT).show();
        }
    }

    // NOVO: Carregar anexos
    private void carregarAnexos() {
        if (chamadoId > 0) {
            try {
                Log.d(TAG, "=== CARREGANDO ANEXOS ===");
                listaAnexos = anexoDAO.buscarAnexosPorChamado(chamadoId);

                if (listaAnexos == null) {
                    listaAnexos = new ArrayList<>();
                }

                Log.d(TAG, "Total de anexos: " + listaAnexos.size());

                // Atualizar contador
                if (tvContadorAnexos != null) {
                    String texto = listaAnexos.size() == 1 ?
                            "1 anexo" : listaAnexos.size() + " anexos";
                    tvContadorAnexos.setText(texto);
                    tvContadorAnexos.setVisibility(listaAnexos.isEmpty() ? View.GONE : View.VISIBLE);
                }

                // Configurar adapter
                if (anexoAdapter == null) {
                    anexoAdapter = new AnexoAdapter(listaAnexos, this, new AnexoAdapter.OnAnexoClickListener() {
                        @Override
                        public void onAnexoClick(Anexo anexo) {
                            abrirAnexo(anexo);
                        }

                        @Override
                        public void onAnexoDeleteClick(Anexo anexo) {
                            confirmarDeletarAnexo(anexo);
                        }
                    });
                    recyclerViewAnexos.setAdapter(anexoAdapter);
                } else {
                    anexoAdapter.atualizarLista(listaAnexos);
                }

                // Mostrar/ocultar RecyclerView
                recyclerViewAnexos.setVisibility(listaAnexos.isEmpty() ? View.GONE : View.VISIBLE);

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao carregar anexos: ", e);
            }
        }
    }

    // NOVO: Abrir anexo
    private void abrirAnexo(Anexo anexo) {
        try {
            File arquivo = new File(anexo.getCaminho());

            if (!arquivo.exists()) {
                Toast.makeText(this, "❌ Arquivo não encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".fileprovider",
                    arquivo);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, anexo.getTipo());
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Abrir com"));

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao abrir anexo: ", e);
            Toast.makeText(this, "Erro ao abrir arquivo", Toast.LENGTH_SHORT).show();
        }
    }

    // NOVO: Confirmar deletar anexo
    private void confirmarDeletarAnexo(Anexo anexo) {
        new AlertDialog.Builder(this)
                .setTitle("Deletar Anexo")
                .setMessage("Deseja realmente deletar este anexo?")
                .setPositiveButton("Sim", (dialog, which) -> deletarAnexo(anexo))
                .setNegativeButton("Não", null)
                .show();
    }

    // NOVO: Deletar anexo
    private void deletarAnexo(Anexo anexo) {
        try {
            // Deletar do banco
            boolean sucesso = anexoDAO.deletarAnexo(anexo.getId());

            if (sucesso) {
                // Deletar arquivo físico
                FileHelper.deletarArquivo(anexo.getCaminho());

                Toast.makeText(this, "✅ Anexo deletado", Toast.LENGTH_SHORT).show();
                carregarAnexos();
            } else {
                Toast.makeText(this, "❌ Erro ao deletar anexo", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao deletar anexo: ", e);
            Toast.makeText(this, "Erro ao deletar anexo", Toast.LENGTH_SHORT).show();
        }
    }

    private void receberDadosChamado() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // Tentar receber como objeto Chamado primeiro
            Chamado chamado = (Chamado) extras.getSerializable("chamado");

            if (chamado != null) {
                // Recebeu objeto completo
                preencherCamposChamado(chamado);
                chamadoId = chamado.getId();

                // Verificar se pode avaliar
                verificarSePodeAvaliar(chamado);

                Log.d(TAG, "Chamado recebido como objeto: ID=" + chamadoId);
            } else {
                // Receber campos individuais
                preencherCamposIndividuais(extras);
                chamadoId = extras.getLong("chamado_id", -1);

                // Verificar status para mostrar botão avaliar
                String status = extras.getString("chamado_status", "");
                mostrarBotaoAvaliar(status);

                Log.d(TAG, "Chamado recebido por campos: ID=" + chamadoId);
            }

            // Validar ID do chamado
            if (chamadoId <= 0) {
                Log.e(TAG, "❌ ID do chamado inválido: " + chamadoId);
                Toast.makeText(this, "Erro: ID do chamado inválido", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "❌ Nenhum dado recebido no Intent");
            Toast.makeText(this, "Erro ao carregar detalhes do chamado", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void preencherCamposChamado(Chamado chamado) {
        try {
            // Título
            if (chamado.getTitulo() != null && !chamado.getTitulo().isEmpty()) {
                txtTituloDetalhe.setText(chamado.getTitulo());
            } else {
                txtTituloDetalhe.setText("Título não disponível");
            }

            // Descrição
            if (chamado.getDescricao() != null && !chamado.getDescricao().isEmpty()) {
                txtDescricaoDetalhe.setText(chamado.getDescricao());
            } else {
                txtDescricaoDetalhe.setText("Descrição não disponível");
            }

            // Categoria
            if (chamado.getCategoria() != null && !chamado.getCategoria().isEmpty()) {
                txtCategoriaDetalhe.setText(chamado.getCategoria());
            } else {
                txtCategoriaDetalhe.setText("Não especificada");
            }

            // Prioridade
            if (chamado.getPrioridade() != null && !chamado.getPrioridade().isEmpty()) {
                txtPrioridadeDetalhe.setText(chamado.getPrioridade());
            } else {
                txtPrioridadeDetalhe.setText("Não definida");
            }

            // Status
            if (chamado.getStatus() != null && !chamado.getStatus().isEmpty()) {
                txtStatusDetalhe.setText(chamado.getStatus());
            } else {
                txtStatusDetalhe.setText("Status não disponível");
            }

            // Data
            if (chamado.getDataCriacaoFormatada() != null) {
                txtDataDetalhe.setText(chamado.getDataCriacaoFormatada());
            } else {
                txtDataDetalhe.setText("Data não disponível");
            }

            // Resposta
            if (chamado.getResposta() != null && !chamado.getResposta().isEmpty()) {
                txtRespostaDetalhe.setText(chamado.getResposta());
            } else {
                txtRespostaDetalhe.setText("Aguardando resposta...");
            }

            Log.d(TAG, "✅ Campos preenchidos com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao preencher campos: ", e);
            Toast.makeText(this, "Erro ao exibir informações", Toast.LENGTH_SHORT).show();
        }
    }

    private void preencherCamposIndividuais(Bundle extras) {
        try {
            String titulo = extras.getString("chamado_titulo", "");
            String descricao = extras.getString("chamado_descricao", "");
            String categoria = extras.getString("chamado_categoria", "Não especificada");
            String prioridade = extras.getString("chamado_prioridade", "");
            String status = extras.getString("chamado_status", "");
            String data = extras.getString("chamado_data", "");
            String resposta = extras.getString("chamado_resposta", "");

            // Preencher TextViews
            txtTituloDetalhe.setText(!titulo.isEmpty() ? titulo : "Título não disponível");
            txtDescricaoDetalhe.setText(!descricao.isEmpty() ? descricao : "Descrição não disponível");
            txtCategoriaDetalhe.setText(categoria);
            txtPrioridadeDetalhe.setText(!prioridade.isEmpty() ? prioridade : "Não definida");
            txtStatusDetalhe.setText(!status.isEmpty() ? status : "Status não disponível");
            txtDataDetalhe.setText(!data.isEmpty() ? data : "Data não disponível");

            if (resposta != null && !resposta.isEmpty()) {
                txtRespostaDetalhe.setText(resposta);
            } else {
                txtRespostaDetalhe.setText("Aguardando resposta...");
            }

            Log.d(TAG, "✅ Campos individuais preenchidos com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao preencher campos individuais: ", e);
            Toast.makeText(this, "Erro ao exibir informações", Toast.LENGTH_SHORT).show();
        }
    }

    private void carregarComentarios() {
        if (chamadoId > 0) {
            try {
                Log.d(TAG, "=== CARREGANDO COMENTÁRIOS ===");
                Log.d(TAG, "Chamado ID: " + chamadoId);

                listaComentarios = comentarioDAO.buscarComentariosPorChamado(chamadoId);

                if (listaComentarios == null) {
                    listaComentarios = new ArrayList<>();
                    Log.w(TAG, "⚠️ Lista de comentários retornou null, criando lista vazia");
                }

                Log.d(TAG, "Total de comentários carregados: " + listaComentarios.size());

                // Configurar adapter
                comentarioAdapter = new ComentarioAdapter(listaComentarios, this);
                recyclerViewComentarios.setAdapter(comentarioAdapter);

                if (listaComentarios.isEmpty()) {
                    Log.d(TAG, "ℹ️ Nenhum comentário encontrado para este chamado");
                } else {
                    Log.d(TAG, "✅ Comentários carregados e exibidos com sucesso");
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao carregar comentários: ", e);
                Toast.makeText(this, "Erro ao carregar comentários", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "❌ ID do chamado inválido ao carregar comentários: " + chamadoId);
        }
    }

    private void enviarComentario() {
        String textoComentario = etNovoComentario.getText().toString().trim();

        if (textoComentario.isEmpty()) {
            Toast.makeText(this, "⚠️ Digite um comentário", Toast.LENGTH_SHORT).show();
            return;
        }

        if (sessionManager == null) {
            Log.e(TAG, "SessionManager está nulo ao tentar enviar comentário.");
            Toast.makeText(this, "Erro interno (sessão). Reinicie o app.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Proteção caso id do chamado esteja inválido
        if (chamadoId <= 0) {
            Log.e(TAG, "ID do chamado inválido: " + chamadoId);
            Toast.makeText(this, "Erro: chamado inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Desabilitar botão enquanto processa
        btnEnviarComentario.setEnabled(false);

        Comentario comentario = new Comentario();
        comentario.setChamadoId(chamadoId);
        comentario.setUsuarioId(sessionManager.getUserId()); // pode ser zero se não logado
        comentario.setTexto(textoComentario);

        // Operação de DB em background
        new Thread(() -> {
            long resultado = -1;
            try {
                resultado = comentarioDAO.inserirComentario(comentario);
            } catch (Exception e) {
                Log.e(TAG, "Erro inserindo comentário em background", e);
            }

            long finalResultado = resultado;
            runOnUiThread(() -> {
                btnEnviarComentario.setEnabled(true);
                if (finalResultado > 0) {
                    Log.d(TAG, "✅ Comentário inserido com sucesso! ID: " + finalResultado);
                    etNovoComentario.setText("");

                    // Registrar auditoria (se houver)
                    try {
                        AuditoriaHelper.registrarComentario(
                                this,
                                sessionManager.getUserId(),
                                chamadoId
                        );
                    } catch (Exception e) {
                        Log.w(TAG, "Falha ao registrar auditoria do comentário", e);
                    }

                    // Enviar notificação ao dono do chamado (se implementado)
                    try {
                        NotificationHelper notificationHelper = new NotificationHelper(this);
                        if (chamado != null && chamado.getClienteId() != sessionManager.getUserId()) {
                            notificationHelper.enviarNotificacaoNovoComentario(
                                    chamado.getClienteId(),
                                    chamadoId,
                                    chamado.getTitulo(),
                                    sessionManager.getUserName()
                            );
                        }
                    } catch (Exception e) {
                        Log.w(TAG, "Erro ao enviar notificação de novo comentário", e);
                    }

                    // Recarrega lista de comentários
                    carregarComentarios();

                    Toast.makeText(this, "✅ Comentário adicionado!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "❌ Falha ao inserir comentário. resultado=" + finalResultado);
                    Toast.makeText(this, "❌ Erro ao adicionar comentário. Tente novamente.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }


    // ========== MÉTODOS DE AVALIAÇÃO ==========

    private void verificarSePodeAvaliar(Chamado chamado) {
        try {
            String status = chamado.getStatus().toLowerCase();
            boolean resolvido = status.contains("resolvido") || status.contains("fechado");
            boolean jaAvaliado = avaliacaoDAO.chamadoJaAvaliado(chamadoId);

            Log.d(TAG, "=== VERIFICAÇÃO DE AVALIAÇÃO ===");
            Log.d(TAG, "Status: " + status);
            Log.d(TAG, "Resolvido: " + resolvido);
            Log.d(TAG, "Já avaliado: " + jaAvaliado);

            if (resolvido && !jaAvaliado) {
                btnAvaliarChamado.setVisibility(View.VISIBLE);
                btnAvaliarChamado.setOnClickListener(v -> abrirTelaAvaliacao());
                Log.d(TAG, "✅ Botão de avaliar habilitado");
            } else {
                btnAvaliarChamado.setVisibility(View.GONE);
                Log.d(TAG, "ℹ️ Botão de avaliar desabilitado");
            }

            // Se já foi avaliado, mostrar avaliação existente
            if (jaAvaliado) {
                mostrarAvaliacaoExistente();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar se pode avaliar: ", e);
            btnAvaliarChamado.setVisibility(View.GONE);
        }
    }

    private void mostrarBotaoAvaliar(String status) {
        if (status != null && !status.isEmpty()) {
            try {
                String statusLower = status.toLowerCase();
                boolean resolvido = statusLower.contains("resolvido") || statusLower.contains("fechado");
                boolean jaAvaliado = avaliacaoDAO.chamadoJaAvaliado(chamadoId);

                Log.d(TAG, "=== MOSTRAR BOTÃO AVALIAR ===");
                Log.d(TAG, "Status: " + status);
                Log.d(TAG, "Resolvido: " + resolvido);
                Log.d(TAG, "Já avaliado: " + jaAvaliado);

                if (resolvido && !jaAvaliado) {
                    btnAvaliarChamado.setVisibility(View.VISIBLE);
                    btnAvaliarChamado.setOnClickListener(v -> abrirTelaAvaliacao());
                    Log.d(TAG, "✅ Botão de avaliar habilitado");
                } else {
                    btnAvaliarChamado.setVisibility(View.GONE);
                    Log.d(TAG, "ℹ️ Botão de avaliar desabilitado");
                }

                // Se já foi avaliado, mostrar avaliação
                if (jaAvaliado) {
                    mostrarAvaliacaoExistente();
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao mostrar botão avaliar: ", e);
                btnAvaliarChamado.setVisibility(View.GONE);
            }
        } else {
            btnAvaliarChamado.setVisibility(View.GONE);
            Log.w(TAG, "⚠️ Status não fornecido, botão avaliar oculto");
        }
    }

    private void abrirTelaAvaliacao() {
        try {
            Log.d(TAG, "Abrindo tela de avaliação para chamado ID: " + chamadoId);

            Intent intent = new Intent(this, AvaliarChamadoActivity.class);
            intent.putExtra("chamado_id", chamadoId);
            intent.putExtra("chamado_titulo", txtTituloDetalhe.getText().toString());
            startActivity(intent);

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao abrir tela de avaliação: ", e);
            Toast.makeText(this, "Erro ao abrir avaliação", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarAvaliacaoExistente() {
        try {
            Avaliacao avaliacao = avaliacaoDAO.buscarAvaliacaoPorChamado(chamadoId);

            if (avaliacao != null) {
                String mensagem = "✅ Avaliado: " + avaliacao.getEstrelas() +
                        "\n" + avaliacao.getNotaTexto();

                if (avaliacao.getComentario() != null && !avaliacao.getComentario().isEmpty()) {
                    mensagem += "\n💬 " + avaliacao.getComentario();
                }

                Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show();
                Log.d(TAG, "✅ Avaliação existente exibida: " + avaliacao.getNota() + " estrelas");
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao mostrar avaliação existente: ", e);
        }
    }

    private void mostrarDialogoGerarPDF() {
        final CharSequence[] opcoes = {
                "📄 Gerar PDF (salvar no dispositivo)",
                "📤 Gerar e Compartilhar (enviar para outros apps)"
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Relatório em PDF");
        builder.setItems(opcoes, (dialog, which) -> {
            switch (which) {
                case 0:
                    // Gerar PDF
                    gerarPDF(false);
                    break;
                case 1:
                    // Gerar e Compartilhar
                    gerarPDF(true);
                    break;
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void gerarPDF(boolean compartilhar) {
        // Mostrar progresso
        android.app.ProgressDialog progressDialog = new android.app.ProgressDialog(this);
        progressDialog.setMessage("📄 Gerando PDF...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Gerar PDF em thread separada
        new Thread(() -> {
            try {
                // Buscar dados atualizados
                Chamado chamado = new Chamado();
                chamado.setId(chamadoId);
                chamado.setTitulo(txtTituloDetalhe.getText().toString());
                chamado.setDescricao(txtDescricaoDetalhe.getText().toString());
                chamado.setCategoria(txtCategoriaDetalhe.getText().toString());
                chamado.setPrioridade(txtPrioridadeDetalhe.getText().toString());
                chamado.setStatus(txtStatusDetalhe.getText().toString());

                // Gerar PDF
                File pdfFile = PDFHelper.gerarPDFChamado(
                        this,
                        chamado,
                        listaComentarios,
                        listaAnexos
                );

                // Atualizar UI na thread principal
                runOnUiThread(() -> {
                    progressDialog.dismiss();

                    if (pdfFile != null) {
                        if (compartilhar) {
                            PDFHelper.compartilharPDF(this, pdfFile);
                        } else {
                            mostrarDialogoSucessoPDF(pdfFile);
                        }
                    } else {
                        Toast.makeText(this, "❌ Erro ao gerar PDF",
                                Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao gerar PDF: ", e);
                runOnUiThread(() -> {
                    progressDialog.dismiss();
                    Toast.makeText(this, "Erro: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    private void mostrarDialogoSucessoPDF(File pdfFile) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("✅ PDF Gerado!")
                .setMessage("Relatório salvo em:\n" + pdfFile.getName())
                .setPositiveButton("📂 Abrir", (dialog, which) -> {
                    PDFHelper.abrirPDF(this, pdfFile);
                })
                .setNeutralButton("📤 Compartilhar", (dialog, which) -> {
                    PDFHelper.compartilharPDF(this, pdfFile);
                })
                .setNegativeButton("OK", null)
                .show();
    }

    private void carregarTags() {
        if (chamadoId > 0) {
            try {
                Log.d(TAG, "=== CARREGANDO TAGS ===");
                listaTagsChamado = tagDAO.buscarTagsDoChamado(chamadoId);

                if (listaTagsChamado == null) {
                    listaTagsChamado = new ArrayList<>();
                }

                Log.d(TAG, "Total de tags: " + listaTagsChamado.size());

                // Configurar adapter
                if (tagAdapter == null) {
                    tagAdapter = new TagAdapter(listaTagsChamado, this,
                            new TagAdapter.OnTagClickListener() {
                                @Override
                                public void onTagClick(Tag tag) {
                                    // Opcional: mostrar info da tag
                                    Toast.makeText(DetalheChamadoActivity.this,
                                            tag.getNome(), Toast.LENGTH_SHORT).show();
                                }

                                @Override
                                public void onTagRemoveClick(Tag tag) {
                                    confirmarRemoverTag(tag);
                                }
                            }, true); // true = mostrar botão remover
                    recyclerViewTags.setAdapter(tagAdapter);
                } else {
                    tagAdapter.atualizarLista(listaTagsChamado);
                }

                // Mostrar/ocultar RecyclerView
                if (listaTagsChamado.isEmpty()) {
                    recyclerViewTags.setVisibility(View.GONE);
                    tvAdicionarTag.setText("+ Adicionar Tags");
                } else {
                    recyclerViewTags.setVisibility(View.VISIBLE);
                    tvAdicionarTag.setText("+ Adicionar");
                }

            } catch (Exception e) {
                Log.e(TAG, "❌ Erro ao carregar tags: ", e);
            }
        }
    }

    private void mostrarDialogoAdicionarTag() {
        try {
            // Buscar todas as tags disponíveis
            List<Tag> todasTags = tagDAO.buscarTodasTags();

            if (todasTags.isEmpty()) {
                Toast.makeText(this, "⚠️ Nenhuma tag disponível", Toast.LENGTH_SHORT).show();
                return;
            }

            // Filtrar tags que já estão no chamado
            List<Tag> tagsDisponiveis = new ArrayList<>();
            for (Tag tag : todasTags) {
                boolean jaAdicionada = false;
                for (Tag tagChamado : listaTagsChamado) {
                    if (tag.getId() == tagChamado.getId()) {
                        jaAdicionada = true;
                        break;
                    }
                }
                if (!jaAdicionada) {
                    tagsDisponiveis.add(tag);
                }
            }

            if (tagsDisponiveis.isEmpty()) {
                Toast.makeText(this, "✅ Todas as tags já foram adicionadas",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Criar array de nomes para o diálogo
            String[] nomesTags = new String[tagsDisponiveis.size()];
            for (int i = 0; i < tagsDisponiveis.size(); i++) {
                Tag tag = tagsDisponiveis.get(i);
                nomesTags[i] = tag.getEmoji() + " " + tag.getNome();
            }

            // Mostrar diálogo
            new AlertDialog.Builder(this)
                    .setTitle("🏷️ Adicionar Tag")
                    .setItems(nomesTags, (dialog, which) -> {
                        Tag tagSelecionada = tagsDisponiveis.get(which);
                        adicionarTag(tagSelecionada);
                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao mostrar diálogo de tags: ", e);
            Toast.makeText(this, "Erro ao carregar tags", Toast.LENGTH_SHORT).show();
        }
    }

    private void adicionarTag(Tag tag) {
        try {
            boolean sucesso = tagDAO.adicionarTagAoChamado(chamadoId, tag.getId());

            if (sucesso) {
                Log.d(TAG, "✅ Tag adicionada: " + tag.getNome());
                Toast.makeText(this, "✅ Tag '" + tag.getNome() + "' adicionada!",
                        Toast.LENGTH_SHORT).show();
                carregarTags();
            } else {
                Toast.makeText(this, "❌ Erro ao adicionar tag", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao adicionar tag: ", e);
            Toast.makeText(this, "Erro ao adicionar tag", Toast.LENGTH_SHORT).show();
        }
    }

    private void confirmarRemoverTag(Tag tag) {
        new AlertDialog.Builder(this)
                .setTitle("Remover Tag")
                .setMessage("Deseja remover a tag '" + tag.getNome() + "'?")
                .setPositiveButton("Sim", (dialog, which) -> removerTag(tag))
                .setNegativeButton("Não", null)
                .show();
    }

    private void removerTag(Tag tag) {
        try {
            boolean sucesso = tagDAO.removerTagDoChamado(chamadoId, tag.getId());

            if (sucesso) {
                Log.d(TAG, "✅ Tag removida: " + tag.getNome());
                Toast.makeText(this, "✅ Tag removida", Toast.LENGTH_SHORT).show();
                carregarTags();
            } else {
                Toast.makeText(this, "❌ Erro ao remover tag", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao remover tag: ", e);
            Toast.makeText(this, "Erro ao remover tag", Toast.LENGTH_SHORT).show();
        }
    }

    // ========== LIFECYCLE METHODS ==========

    @Override
    protected void onResume() {
        super.onResume();
        carregarTags();
        Log.d(TAG, "Activity retomada");

        // Recarregar comentários quando voltar
        if (chamadoId > 0) {
            carregarComentarios();

            // Verificar novamente se pode avaliar (caso tenha acabado de avaliar)
            if (avaliacaoDAO != null) {
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    String status = extras.getString("chamado_status", "");
                    mostrarBotaoAvaliar(status);
                }
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        Log.d(TAG, "Navegação para trás");
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
}