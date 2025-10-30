package com.example.helpdeskapp.utils;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileHelper {
    private static final String TAG = "FileHelper";
    private static final String FOLDER_NAME = "HelpdeskAnexos";

    // Criar diretório para anexos
    public static File criarDiretorioAnexos(Context context) {
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), FOLDER_NAME);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            Log.d(TAG, created ? "✅ Diretório criado: " + directory.getAbsolutePath()
                    : "❌ Falha ao criar diretório");
        }

        return directory;
    }

    // Criar arquivo temporário para foto
    public static File criarArquivoFoto(Context context) {
        File directory = criarDiretorioAnexos(context);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "FOTO_" + timeStamp + ".jpg";

        return new File(directory, fileName);
    }

    // Copiar arquivo de Uri para diretório interno
    public static File copiarArquivo(Context context, Uri sourceUri, String nomeArquivo) {
        File destino = new File(criarDiretorioAnexos(context), nomeArquivo);

        try (InputStream inputStream = context.getContentResolver().openInputStream(sourceUri);
             FileOutputStream outputStream = new FileOutputStream(destino)) {

            if (inputStream == null) {
                Log.e(TAG, "❌ InputStream é null");
                return null;
            }

            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            Log.d(TAG, "✅ Arquivo copiado: " + destino.getAbsolutePath());
            return destino;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao copiar arquivo: ", e);
            return null;
        }
    }

    // Obter tamanho do arquivo
    public static long obterTamanhoArquivo(File file) {
        if (file != null && file.exists()) {
            return file.length();
        }
        return 0;
    }

    // Obter tipo MIME
    public static String obterTipoMime(String nomeArquivo) {
        if (nomeArquivo == null) return "application/octet-stream";

        String extensao = nomeArquivo.substring(nomeArquivo.lastIndexOf(".") + 1).toLowerCase();

        switch (extensao) {
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "png":
                return "image/png";
            case "pdf":
                return "application/pdf";
            default:
                return "application/octet-stream";
        }
    }

    // Deletar arquivo
    public static boolean deletarArquivo(String caminho) {
        if (caminho == null || caminho.isEmpty()) return false;

        File file = new File(caminho);
        if (file.exists()) {
            boolean deleted = file.delete();
            Log.d(TAG, deleted ? "✅ Arquivo deletado: " + caminho
                    : "❌ Falha ao deletar: " + caminho);
            return deleted;
        }

        return false;
    }

    // Verificar se arquivo existe
    public static boolean arquivoExiste(String caminho) {
        if (caminho == null || caminho.isEmpty()) return false;
        File file = new File(caminho);
        return file.exists();
    }
}