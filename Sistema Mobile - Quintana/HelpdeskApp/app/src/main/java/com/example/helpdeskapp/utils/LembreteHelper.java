package com.example.helpdeskapp.utils;

import android.content.Context;
import android.util.Log;
import com.example.helpdeskapp.dao.ChamadoDAO;
import com.example.helpdeskapp.models.Chamado;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class LembreteHelper {
    private static final String TAG = "LembreteHelper";
    private static final int HORAS_SEM_RESPOSTA = 24;

    public static void verificarChamadosSemResposta(Context context) {
        try {
            ChamadoDAO chamadoDAO = new ChamadoDAO(context);
            NotificationHelper notificationHelper = new NotificationHelper(context);

            // Buscar todos os chamados
            List<Chamado> todosChamados = chamadoDAO.buscarTodosChamados();

            for (Chamado chamado : todosChamados) {
                if (chamado.getStatus().equalsIgnoreCase("Aberto") &&
                        chamadoSemRespostaHaMaisDe24Horas(chamado)) {

                    notificationHelper.enviarNotificacaoLembrete(
                            chamado.getClienteId(),
                            chamado.getId(),
                            chamado.getTitulo(),
                            "Seu chamado está aberto há mais de 24 horas sem resposta"
                    );

                    Log.d(TAG, "✅ Lembrete enviado para chamado: " + chamado.getId());
                }
            }

            Log.d(TAG, "✅ Verificação de lembretes concluída");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar lembretes: ", e);
        }
    }

    public static void enviarResumoDiario(Context context, long usuarioId) {
        try {
            ChamadoDAO chamadoDAO = new ChamadoDAO(context);
            NotificationHelper notificationHelper = new NotificationHelper(context);

            List<Chamado> todosChamados = chamadoDAO.buscarTodosChamados();

            int totalAbertos = 0;
            int totalAndamento = 0;

            for (Chamado chamado : todosChamados) {
                if (chamado.getClienteId() == usuarioId) {
                    if (chamado.getStatus().equals("Aberto")) {
                        totalAbertos++;
                    } else if (chamado.getStatus().equals("Em Andamento")) {
                        totalAndamento++;
                    }
                }
            }

            if (totalAbertos > 0 || totalAndamento > 0) {
                notificationHelper.enviarNotificacaoResumoDiario(
                        usuarioId,
                        totalAbertos,
                        totalAndamento
                );

                Log.d(TAG, "✅ Resumo diário enviado para usuário: " + usuarioId);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao enviar resumo diário: ", e);
        }
    }

    private static boolean chamadoSemRespostaHaMaisDe24Horas(Chamado chamado) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

            // CORRIGIDO: Assume que getUpdatedAt() e getCreatedAt() existem e retornam String.
            String dataString = chamado.getUpdatedAt();
            if (dataString == null || dataString.isEmpty()) {
                dataString = chamado.getCreatedAt();
            }

            if (dataString == null || dataString.isEmpty()) {
                Log.w(TAG, "Data de criação ou atualização ausente para chamado: " + chamado.getId());
                return false;
            }

            Date data = sdf.parse(dataString);

            if (data == null) return false;

            Calendar agora = Calendar.getInstance();
            Calendar criacao = Calendar.getInstance();
            criacao.setTime(data);

            long diffMillis = agora.getTimeInMillis() - criacao.getTimeInMillis();
            long diffHoras = diffMillis / (1000 * 60 * 60);

            return diffHoras >= HORAS_SEM_RESPOSTA;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao calcular tempo: ", e);
            return false;
        }
    }

    public static void limparNotificacoesAntigas(Context context) {
        NotificationHelper notificationHelper = new NotificationHelper(context);
        int deletadas = notificationHelper.limparNotificacoesAntigas(30);
        Log.d(TAG, "✅ Notificações antigas limpas: " + deletadas);
    }
}