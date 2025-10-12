package com.example.helpdeskapp.utils;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.example.helpdeskapp.dao.AuditoriaDAO;
import com.example.helpdeskapp.models.Auditoria;

public class AuditoriaHelper {
    private static final String TAG = "AuditoriaHelper";

    // Registrar ação de login
    public static void registrarLogin(Context context, long usuarioId, String nomeUsuario) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("LOGIN");
            auditoria.setEntidade("Usuario");
            auditoria.setEntidadeId(usuarioId);
            auditoria.setDescricao(nomeUsuario + " fez login no sistema");
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Login registrado para: " + nomeUsuario);
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar login: ", e);
        }
    }

    // Registrar ação de logout
    public static void registrarLogout(Context context, long usuarioId, String nomeUsuario) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("LOGOUT");
            auditoria.setEntidade("Usuario");
            auditoria.setEntidadeId(usuarioId);
            auditoria.setDescricao(nomeUsuario + " fez logout do sistema");
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Logout registrado para: " + nomeUsuario);
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar logout: ", e);
        }
    }

    // Registrar criação de chamado
    public static void registrarCriacaoChamado(Context context, long usuarioId, long chamadoId, String titulo) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("CRIOU");
            auditoria.setEntidade("Chamado");
            auditoria.setEntidadeId(chamadoId);
            auditoria.setDescricao("Criou o chamado: " + titulo);
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Criação de chamado registrada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar criação de chamado: ", e);
        }
    }

    // Registrar alteração de status
    public static void registrarAlteracaoStatus(Context context, long usuarioId, long chamadoId,
                                                String statusAntigo, String statusNovo) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("ALTERAR STATUS");
            auditoria.setEntidade("Chamado");
            auditoria.setEntidadeId(chamadoId);
            auditoria.setDescricao("Alterou status de '" + statusAntigo + "' para '" + statusNovo + "'");
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Alteração de status registrada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar alteração de status: ", e);
        }
    }

    // Registrar comentário
    public static void registrarComentario(Context context, long usuarioId, long chamadoId) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("COMENTOU");
            auditoria.setEntidade("Chamado");
            auditoria.setEntidadeId(chamadoId);
            auditoria.setDescricao("Adicionou um comentário");
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Comentário registrado");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar comentário: ", e);
        }
    }

    // Registrar avaliação
    public static void registrarAvaliacao(Context context, long usuarioId, long chamadoId, int nota) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("AVALIOU");
            auditoria.setEntidade("Chamado");
            auditoria.setEntidadeId(chamadoId);
            auditoria.setDescricao("Avaliou com " + nota + " estrelas");
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Avaliação registrada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar avaliação: ", e);
        }
    }

    // Registrar anexo
    public static void registrarAnexo(Context context, long usuarioId, long chamadoId, String nomeArquivo) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("ANEXOU");
            auditoria.setEntidade("Chamado");
            auditoria.setEntidadeId(chamadoId);
            auditoria.setDescricao("Anexou arquivo: " + nomeArquivo);
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Anexo registrado");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar anexo: ", e);
        }
    }

    // Registrar criação de tag
    public static void registrarCriacaoTag(Context context, long usuarioId, String nomeTag) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao("CRIOU");
            auditoria.setEntidade("Tag");
            auditoria.setEntidadeId(0);
            auditoria.setDescricao("Criou a tag: " + nomeTag);
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Criação de tag registrada");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar criação de tag: ", e);
        }
    }

    // Registrar ação genérica
    public static void registrarAcao(Context context, long usuarioId, String acao,
                                     String entidade, long entidadeId, String descricao) {
        try {
            AuditoriaDAO auditoriaDAO = new AuditoriaDAO(context);
            Auditoria auditoria = new Auditoria();
            auditoria.setUsuarioId(usuarioId);
            auditoria.setAcao(acao);
            auditoria.setEntidade(entidade);
            auditoria.setEntidadeId(entidadeId);
            auditoria.setDescricao(descricao);
            auditoria.setDispositivo(obterInfoDispositivo());

            auditoriaDAO.registrarAcao(auditoria);
            Log.d(TAG, "✅ Ação registrada: " + acao);
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao registrar ação: ", e);
        }
    }

    // Obter informações do dispositivo
    private static String obterInfoDispositivo() {
        return Build.MANUFACTURER + " " + Build.MODEL + " (Android " + Build.VERSION.RELEASE + ")";
    }
}