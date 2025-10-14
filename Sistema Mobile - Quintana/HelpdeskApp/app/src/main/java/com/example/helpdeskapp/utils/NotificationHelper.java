package com.example.helpdeskapp.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.example.helpdeskapp.DetalheChamadoActivity;
import com.example.helpdeskapp.NotificacoesActivity;
import com.example.helpdeskapp.R;
import com.example.helpdeskapp.dao.NotificacaoDAO;
import com.example.helpdeskapp.models.Notificacao;
import com.example.helpdeskapp.dao.UsuarioDAO;
import com.example.helpdeskapp.models.Usuario;
import java.util.List;
import android.util.Log;


public class NotificationHelper {
    private static final String TAG = "NotificationHelper";

    // Canais de Notificação
    private static final String CHANNEL_CHAMADOS_ID = "helpdesk_chamados";
    private static final String CHANNEL_COMENTARIOS_ID = "helpdesk_comentarios";
    private static final String CHANNEL_STATUS_ID = "helpdesk_status";
    private static final String CHANNEL_LEMBRETES_ID = "helpdesk_lembretes";
    private static final String CHANNEL_GERAL_ID = "helpdesk_geral";

    private Context context;
    private NotificationManagerCompat notificationManager;
    private NotificacaoDAO notificacaoDAO;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = NotificationManagerCompat.from(context);
        this.notificacaoDAO = new NotificacaoDAO(context);
        criarCanaisNotificacao();
    }

    // ========== CRIAR CANAIS DE NOTIFICAÇÃO ==========
    private void criarCanaisNotificacao() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Canal: Chamados
            NotificationChannel channelChamados = new NotificationChannel(
                    CHANNEL_CHAMADOS_ID,
                    "Chamados",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelChamados.setDescription("Notificações sobre novos chamados");
            channelChamados.enableLights(true);
            channelChamados.setLightColor(Color.BLUE);
            channelChamados.enableVibration(true);
            channelChamados.setVibrationPattern(new long[]{0, 500, 250, 500});

            // Canal: Comentários
            NotificationChannel channelComentarios = new NotificationChannel(
                    CHANNEL_COMENTARIOS_ID,
                    "Comentários",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelComentarios.setDescription("Notificações sobre novos comentários");
            channelComentarios.enableLights(true);
            channelComentarios.setLightColor(Color.GREEN);

            // Canal: Status
            NotificationChannel channelStatus = new NotificationChannel(
                    CHANNEL_STATUS_ID,
                    "Alterações de Status",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelStatus.setDescription("Notificações sobre mudanças de status");
            channelStatus.enableLights(true);
            channelStatus.setLightColor(Color.YELLOW);

            // Canal: Lembretes
            NotificationChannel channelLembretes = new NotificationChannel(
                    CHANNEL_LEMBRETES_ID,
                    "Lembretes",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channelLembretes.setDescription("Lembretes sobre chamados pendentes");
            channelLembretes.enableLights(true);
            channelLembretes.setLightColor(Color.RED);
            channelLembretes.enableVibration(true);
            channelLembretes.setVibrationPattern(new long[]{0, 1000, 500, 1000});

            // Canal: Geral
            NotificationChannel channelGeral = new NotificationChannel(
                    CHANNEL_GERAL_ID,
                    "Notificações Gerais",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channelGeral.setDescription("Outras notificações do sistema");

            // Registrar canais
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channelChamados);
                manager.createNotificationChannel(channelComentarios);
                manager.createNotificationChannel(channelStatus);
                manager.createNotificationChannel(channelLembretes);
                manager.createNotificationChannel(channelGeral);
            }
        }
    }

    // ========== NOTIFICAÇÃO: NOVO CHAMADO ==========
    public void enviarNotificacaoNovoChamado(long usuarioId, long chamadoId, String titulo, String prioridade) {
        String mensagem = "Novo chamado criado - Prioridade: " + prioridade;

        // Salvar no banco
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo("CHAMADO_CRIADO");
        notificacao.setTitulo("🎫 " + titulo);
        notificacao.setMensagem(mensagem);
        notificacao.setChamadoId(chamadoId);
        notificacaoDAO.criarNotificacao(notificacao);

        // Intent para abrir o chamado
        Intent intent = new Intent(context, DetalheChamadoActivity.class);
        intent.putExtra("chamado_id", chamadoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) chamadoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Construir notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_CHAMADOS_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("🎫 Novo Chamado")
                .setContentText(titulo)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(titulo + "\n" + mensagem))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 500, 250, 500})
                .setLights(Color.BLUE, 3000, 3000)
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.primary))
                .setCategory(NotificationCompat.CATEGORY_MESSAGE);

        // Adicionar ação
        builder.addAction(R.drawable.ic_visibility, "Ver Detalhes", pendingIntent);

        // Enviar notificação
        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ========== NOTIFICAÇÃO: NOVO COMENTÁRIO ==========
    public void enviarNotificacaoNovoComentario(long usuarioId, long chamadoId, String chamadoTitulo, String autor) {
        String mensagem = autor + " comentou no seu chamado";

        // Salvar no banco
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo("COMENTARIO");
        notificacao.setTitulo("💬 " + chamadoTitulo);
        notificacao.setMensagem(mensagem);
        notificacao.setChamadoId(chamadoId);
        notificacaoDAO.criarNotificacao(notificacao);

        // Intent
        Intent intent = new Intent(context, DetalheChamadoActivity.class);
        intent.putExtra("chamado_id", chamadoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) chamadoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_COMENTARIOS_ID)
                .setSmallIcon(R.drawable.ic_comment)
                .setContentTitle("💬 Novo Comentário")
                .setContentText(chamadoTitulo)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mensagem + "\n\n" + chamadoTitulo))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.success))
                .addAction(R.drawable.ic_reply, "Responder", pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ========== NOTIFICAÇÃO: MUDANÇA DE STATUS ==========
    public void enviarNotificacaoMudancaStatus(long usuarioId, long chamadoId, String chamadoTitulo, String novoStatus) {
        String mensagem = "Status alterado para: " + novoStatus;

        // Salvar no banco
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo("STATUS_ALTERADO");
        notificacao.setTitulo("🔄 " + chamadoTitulo);
        notificacao.setMensagem(mensagem);
        notificacao.setChamadoId(chamadoId);
        notificacaoDAO.criarNotificacao(notificacao);

        // Intent
        Intent intent = new Intent(context, DetalheChamadoActivity.class);
        intent.putExtra("chamado_id", chamadoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) chamadoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STATUS_ID)
                .setSmallIcon(R.drawable.ic_sync)
                .setContentTitle("🔄 Status Alterado")
                .setContentText(chamadoTitulo + " - " + novoStatus)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(chamadoTitulo + "\n" + mensagem))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.warning));

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ========== NOTIFICAÇÃO: LEMBRETE ==========
    public void enviarNotificacaoLembrete(long usuarioId, long chamadoId, String chamadoTitulo, String mensagem) {
        // Salvar no banco
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo("LEMBRETE");
        notificacao.setTitulo("⏰ Lembrete: " + chamadoTitulo);
        notificacao.setMensagem(mensagem);
        notificacao.setChamadoId(chamadoId);
        notificacaoDAO.criarNotificacao(notificacao);

        // Intent
        Intent intent = new Intent(context, DetalheChamadoActivity.class);
        intent.putExtra("chamado_id", chamadoId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) chamadoId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_LEMBRETES_ID)
                .setSmallIcon(R.drawable.ic_alarm)
                .setContentTitle("⏰ Lembrete")
                .setContentText(chamadoTitulo)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(mensagem + "\n\n" + chamadoTitulo))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
                .setVibrate(new long[]{0, 1000, 500, 1000})
                .setLights(Color.RED, 3000, 3000)
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.error))
                .addAction(R.drawable.ic_check, "Marcar como Visto", pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ========== NOTIFICAÇÃO: RESUMO DIÁRIO ==========
    public void enviarNotificacaoResumoDiario(long usuarioId, int totalAbertos, int totalAndamento) {
        String mensagem = "Você tem " + totalAbertos + " chamados abertos e " +
                totalAndamento + " em andamento";

        // Salvar no banco
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo("RESUMO_DIARIO");
        notificacao.setTitulo("📊 Resumo Diário");
        notificacao.setMensagem(mensagem);
        notificacaoDAO.criarNotificacao(notificacao);

        // Intent para NotificacoesActivity
        Intent intent = new Intent(context, NotificacoesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GERAL_ID)
                .setSmallIcon(R.drawable.ic_dashboard)
                .setContentTitle("📊 Resumo Diário")
                .setContentText(mensagem)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(mensagem))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setColor(context.getResources().getColor(R.color.info));

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ========== NOTIFICAÇÃO CUSTOMIZADA ==========
    public void enviarNotificacaoCustomizada(long usuarioId, String tipo, String titulo, String mensagem, int icone) {
        // Salvar no banco
        Notificacao notificacao = new Notificacao();
        notificacao.setUsuarioId(usuarioId);
        notificacao.setTipo(tipo);
        notificacao.setTitulo(titulo);
        notificacao.setMensagem(mensagem);
        notificacaoDAO.criarNotificacao(notificacao);

        // Intent
        Intent intent = new Intent(context, NotificacoesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_GERAL_ID)
                .setSmallIcon(icone)
                .setContentTitle(titulo)
                .setContentText(mensagem)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ========== OBTER CONTAGEM DE NOTIFICAÇÕES NÃO LIDAS ==========
    public int getContagemNaoLidas(long usuarioId) {
        return notificacaoDAO.contarNaoLidas(usuarioId);
    }

    // ========== LIMPAR NOTIFICAÇÕES ANTIGAS ==========
    public int limparNotificacoesAntigas(int diasAtras) {
        return notificacaoDAO.deletarNotificacoesAntigas(diasAtras);
    }

    /// ========== NOTIFICAR TODOS OS ADMINISTRADORES ==========
    public void notificarAdministradores(long chamadoId, String titulo, String prioridade, String nomeCliente) {
        Log.d(TAG, "📤 Iniciando notificação para administradores...");

        UsuarioDAO usuarioDAO = new UsuarioDAO(context);
        // NÃO precisa chamar open() - o método já abre e fecha internamente

        List<Usuario> admins = usuarioDAO.buscarTodosAdministradores();

        Log.d(TAG, "📊 Total de admins encontrados: " + admins.size());

        if (admins.isEmpty()) {
            Log.w(TAG, "⚠️ Nenhum administrador encontrado no banco!");
            return;
        }

        for (Usuario admin : admins) {
            Log.d(TAG, "📤 Notificando admin: " + admin.getNome() + " (ID: " + admin.getId() + ")");

            enviarNotificacaoNovoChamado(
                    admin.getId(),
                    chamadoId,
                    titulo,
                    prioridade
            );
        }

        Log.d(TAG, "✅ " + admins.size() + " administradores notificados com sucesso!");
    }
}