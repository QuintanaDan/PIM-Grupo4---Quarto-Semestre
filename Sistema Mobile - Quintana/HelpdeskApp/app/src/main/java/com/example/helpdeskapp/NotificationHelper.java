package com.example.helpdeskapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.example.helpdeskapp.MainActivity;
import com.example.helpdeskapp.R;

public class NotificationHelper {
    private static final String CHANNEL_ID = "helpdesk_channel";
    private static final String CHANNEL_NAME = "Helpdesk Notifications";
    private static final String CHANNEL_DESC = "Notificações do sistema de helpdesk";
    private static final int NOTIFICATION_ID = 1001;

    private Context context;
    private NotificationManager notificationManager;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.setLightColor(Color.BLUE);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});

            notificationManager.createNotificationChannel(channel);
        }
    }

    public void enviarNotificacaoNovoComentario(String tituloChamado, String autor) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("💬 Novo Comentário")
                .setContentText(autor + " comentou em: " + tituloChamado)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(autor + " adicionou um novo comentário no chamado: " + tituloChamado));

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public void enviarNotificacaoMudancaStatus(String tituloChamado, String novoStatus) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String emoji = getEmojiPorStatus(novoStatus);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(emoji + " Status Atualizado")
                .setContentText("Chamado: " + tituloChamado)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("O status do chamado '" + tituloChamado + "' foi alterado para: " + novoStatus));

        notificationManager.notify(NOTIFICATION_ID + 1, builder.build());
    }

    public void enviarNotificacaoNovoChamado(String titulo, String prioridade) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        String emoji = "🎫";
        if (prioridade.equalsIgnoreCase("alta") || prioridade.equalsIgnoreCase("crítica")) {
            emoji = "🚨";
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(emoji + " Novo Chamado Criado")
                .setContentText(titulo)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText("Novo chamado de prioridade " + prioridade + ": " + titulo));

        notificationManager.notify(NOTIFICATION_ID + 2, builder.build());
    }

    private String getEmojiPorStatus(String status) {
        if (status == null) return "📋";

        switch (status.toLowerCase()) {
            case "aberto":
                return "🟢";
            case "em andamento":
                return "🔄";
            case "resolvido":
                return "✅";
            case "fechado":
                return "🔒";
            default:
                return "📋";
        }
    }

    public void cancelarTodasNotificacoes() {
        notificationManager.cancelAll();
    }
}