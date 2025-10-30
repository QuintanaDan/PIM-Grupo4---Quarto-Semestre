package com.example.helpdeskapp.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Log;
import com.example.helpdeskapp.models.Anexo;
import com.example.helpdeskapp.models.Chamado;
import com.example.helpdeskapp.models.Comentario;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFHelper {
    private static final String TAG = "PDFHelper";
    private static final String FOLDER_NAME = "HelpdeskRelatorios";

    private static final int PAGE_WIDTH = 595; // A4 width in points
    private static final int PAGE_HEIGHT = 842; // A4 height in points
    private static final int MARGIN = 50;
    private static final int LINE_HEIGHT = 20;

    // Criar diretório para relatórios
    public static File criarDiretorioRelatorios(Context context) {
        File directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), FOLDER_NAME);

        if (!directory.exists()) {
            boolean created = directory.mkdirs();
            Log.d(TAG, created ? "✅ Diretório criado: " + directory.getAbsolutePath()
                    : "❌ Falha ao criar diretório");
        }

        return directory;
    }

    // Gerar PDF do chamado
    public static File gerarPDFChamado(Context context, Chamado chamado,
                                       List<Comentario> comentarios,
                                       List<Anexo> anexos) {
        Log.d(TAG, "=== GERANDO PDF DO CHAMADO ===");

        try {
            // Criar documento PDF
            PdfDocument document = new PdfDocument();

            int pageNumber = 1;
            int yPosition = MARGIN;

            // Criar primeira página
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // ========== CABEÇALHO ==========
            paint.setTextSize(24);
            paint.setFakeBoldText(true);
            canvas.drawText("RELATÓRIO DE CHAMADO", MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT * 2;

            paint.setTextSize(10);
            paint.setFakeBoldText(false);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            canvas.drawText("Gerado em: " + sdf.format(new Date()), MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT * 2;

            // Linha separadora
            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT;

            // ========== DADOS DO CHAMADO ==========
            paint.setTextSize(16);
            paint.setFakeBoldText(true);
            canvas.drawText("📋 Informações do Chamado", MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT * 1.5f;

            paint.setTextSize(12);
            paint.setFakeBoldText(false);

            // Protocolo
            canvas.drawText("Protocolo:", MARGIN, yPosition, paint);
            paint.setFakeBoldText(true);
            canvas.drawText(chamado.getProtocoloFormatado(), MARGIN + 150, yPosition, paint);
            yPosition += LINE_HEIGHT;
            paint.setFakeBoldText(false);

            // Título
            canvas.drawText("Título:", MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT;
            String titulo = chamado.getTitulo();
            if (titulo.length() > 60) {
                canvas.drawText(titulo.substring(0, 60), MARGIN + 20, yPosition, paint);
                yPosition += LINE_HEIGHT;
                canvas.drawText(titulo.substring(60), MARGIN + 20, yPosition, paint);
            } else {
                canvas.drawText(titulo, MARGIN + 20, yPosition, paint);
            }
            yPosition += LINE_HEIGHT * 1.5f;

            // Descrição
            canvas.drawText("Descrição:", MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT;
            String descricao = chamado.getDescricao();
            yPosition = desenharTextoMultilinha(canvas, paint, descricao,
                    MARGIN + 20, yPosition, PAGE_WIDTH - MARGIN - 20);
            yPosition += LINE_HEIGHT;

            // Categoria
            canvas.drawText("Categoria:", MARGIN, yPosition, paint);
            canvas.drawText(chamado.getCategoria(), MARGIN + 150, yPosition, paint);
            yPosition += LINE_HEIGHT;

            // Prioridade
            canvas.drawText("Prioridade:", MARGIN, yPosition, paint);
            canvas.drawText(chamado.getPrioridade(), MARGIN + 150, yPosition, paint);
            yPosition += LINE_HEIGHT;

            // Status
            canvas.drawText("Status:", MARGIN, yPosition, paint);
            canvas.drawText(chamado.getStatus(), MARGIN + 150, yPosition, paint);
            yPosition += LINE_HEIGHT;

            // Data
            canvas.drawText("Data de Abertura:", MARGIN, yPosition, paint);
            canvas.drawText(chamado.getDataCriacaoFormatada(), MARGIN + 150, yPosition, paint);
            yPosition += LINE_HEIGHT * 2;

            // ========== COMENTÁRIOS ==========
            if (comentarios != null && !comentarios.isEmpty()) {
                // Verificar se precisa de nova página
                if (yPosition > PAGE_HEIGHT - 200) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(
                            PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPosition = MARGIN;
                }

                paint.setTextSize(16);
                paint.setFakeBoldText(true);
                canvas.drawText("💬 Comentários (" + comentarios.size() + ")", MARGIN, yPosition, paint);
                yPosition += LINE_HEIGHT * 1.5f;

                paint.setTextSize(11);
                paint.setFakeBoldText(false);

                for (Comentario comentario : comentarios) {
                    // Verificar se precisa de nova página
                    if (yPosition > PAGE_HEIGHT - 150) {
                        document.finishPage(page);
                        pageNumber++;
                        pageInfo = new PdfDocument.PageInfo.Builder(
                                PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                        page = document.startPage(pageInfo);
                        canvas = page.getCanvas();
                        yPosition = MARGIN;
                    }

                    // Autor e data
                    paint.setFakeBoldText(true);
                    String autor = comentario.getNomeUsuario() != null ?
                            comentario.getNomeUsuario() : "Usuário";
                    canvas.drawText(autor + " - " + comentario.getDataCriacaoFormatada(),
                            MARGIN + 20, yPosition, paint);
                    yPosition += LINE_HEIGHT;

                    // Texto do comentário
                    paint.setFakeBoldText(false);
                    yPosition = desenharTextoMultilinha(canvas, paint, comentario.getTexto(),
                            MARGIN + 20, yPosition, PAGE_WIDTH - MARGIN - 20);
                    yPosition += LINE_HEIGHT * 1.5f;
                }
            }

            // ========== ANEXOS ==========
            if (anexos != null && !anexos.isEmpty()) {
                // Verificar se precisa de nova página
                if (yPosition > PAGE_HEIGHT - 150) {
                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(
                            PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPosition = MARGIN;
                }

                paint.setTextSize(16);
                paint.setFakeBoldText(true);
                canvas.drawText("📎 Anexos (" + anexos.size() + ")", MARGIN, yPosition, paint);
                yPosition += LINE_HEIGHT * 1.5f;

                paint.setTextSize(11);
                paint.setFakeBoldText(false);

                for (Anexo anexo : anexos) {
                    canvas.drawText("• " + anexo.getNomeArquivo() +
                                    " (" + anexo.getTamanhoFormatado() + ")",
                            MARGIN + 20, yPosition, paint);
                    yPosition += LINE_HEIGHT;
                }
            }

            // ========== RODAPÉ ==========
            paint.setTextSize(10);
            canvas.drawText("Página " + pageNumber,
                    PAGE_WIDTH / 2 - 30, PAGE_HEIGHT - 30, paint);
            canvas.drawText("Helpdesk App - Relatório Gerado Automaticamente",
                    MARGIN, PAGE_HEIGHT - 30, paint);

            document.finishPage(page);

            // Salvar arquivo
            File directory = criarDiretorioRelatorios(context);
            String fileName = "Relatorio_" + chamado.getProtocoloFormatado() + "_" +
                    System.currentTimeMillis() + ".pdf";
            File file = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Log.d(TAG, "✅ PDF gerado com sucesso: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao gerar PDF: ", e);
            return null;
        }
    }

    // Desenhar texto multilinha
    private static int desenharTextoMultilinha(Canvas canvas, Paint paint,
                                               String texto, int x, int y, int maxWidth) {
        if (texto == null || texto.isEmpty()) {
            canvas.drawText("(vazio)", x, y, paint);
            return y + LINE_HEIGHT;
        }

        String[] palavras = texto.split(" ");
        StringBuilder linha = new StringBuilder();
        int yPosition = y;

        for (String palavra : palavras) {
            String testeLinha = linha + palavra + " ";
            float largura = paint.measureText(testeLinha);

            if (largura > maxWidth - x) {
                canvas.drawText(linha.toString().trim(), x, yPosition, paint);
                yPosition += LINE_HEIGHT;
                linha = new StringBuilder(palavra + " ");
            } else {
                linha.append(palavra).append(" ");
            }
        }

        if (linha.length() > 0) {
            canvas.drawText(linha.toString().trim(), x, yPosition, paint);
            yPosition += LINE_HEIGHT;
        }

        return yPosition;
    }

    // Gerar PDF de múltiplos chamados (Relatório Geral)
    public static File gerarRelatorioGeral(Context context, List<Chamado> chamados,
                                           String titulo) {
        Log.d(TAG, "=== GERANDO RELATÓRIO GERAL ===");

        try {
            PdfDocument document = new PdfDocument();
            int pageNumber = 1;
            int yPosition = MARGIN;

            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            // Cabeçalho
            paint.setTextSize(24);
            paint.setFakeBoldText(true);
            canvas.drawText(titulo, MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT * 2;

            paint.setTextSize(10);
            paint.setFakeBoldText(false);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            canvas.drawText("Gerado em: " + sdf.format(new Date()), MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT;
            canvas.drawText("Total de Chamados: " + chamados.size(), MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT * 2;

            canvas.drawLine(MARGIN, yPosition, PAGE_WIDTH - MARGIN, yPosition, paint);
            yPosition += LINE_HEIGHT * 1.5f;

            // Lista de chamados
            paint.setTextSize(12);

            for (int i = 0; i < chamados.size(); i++) {
                Chamado chamado = chamados.get(i);

                // Verificar se precisa de nova página
                if (yPosition > PAGE_HEIGHT - 100) {
                    // Rodapé
                    paint.setTextSize(10);
                    canvas.drawText("Página " + pageNumber,
                            PAGE_WIDTH / 2 - 30, PAGE_HEIGHT - 30, paint);

                    document.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(
                            PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                    page = document.startPage(pageInfo);
                    canvas = page.getCanvas();
                    yPosition = MARGIN;
                    paint.setTextSize(12);
                }

                // Número do chamado
                paint.setFakeBoldText(true);
                canvas.drawText((i + 1) + ". " + chamado.getProtocoloFormatado(),
                        MARGIN, yPosition, paint);
                yPosition += LINE_HEIGHT;

                paint.setFakeBoldText(false);

                // Título
                canvas.drawText("   Título: " + chamado.getTitulo(), MARGIN, yPosition, paint);
                yPosition += LINE_HEIGHT;

                // Status e Prioridade
                canvas.drawText("   Status: " + chamado.getStatus() +
                        " | Prioridade: " + chamado.getPrioridade(), MARGIN, yPosition, paint);
                yPosition += LINE_HEIGHT;

                // Data
                canvas.drawText("   Data: " + chamado.getDataCriacaoFormatada(),
                        MARGIN, yPosition, paint);
                yPosition += LINE_HEIGHT * 1.5f;
            }

            // Rodapé final
            paint.setTextSize(10);
            canvas.drawText("Página " + pageNumber,
                    PAGE_WIDTH / 2 - 30, PAGE_HEIGHT - 30, paint);
            canvas.drawText("Helpdesk App - Relatório Gerado Automaticamente",
                    MARGIN, PAGE_HEIGHT - 30, paint);

            document.finishPage(page);

            // Salvar arquivo
            File directory = criarDiretorioRelatorios(context);
            String fileName = "Relatorio_Geral_" + System.currentTimeMillis() + ".pdf";
            File file = new File(directory, fileName);

            FileOutputStream fos = new FileOutputStream(file);
            document.writeTo(fos);
            document.close();
            fos.close();

            Log.d(TAG, "✅ Relatório geral gerado: " + file.getAbsolutePath());
            return file;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao gerar relatório geral: ", e);
            return null;
        }
    }

    // Compartilhar PDF
    public static void compartilharPDF(Context context, File pdfFile) {
        try {
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".fileprovider",
                    pdfFile
            );

            android.content.Intent intent = new android.content.Intent(
                    android.content.Intent.ACTION_SEND);
            intent.setType("application/pdf");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(android.content.Intent.createChooser(
                    intent, "Compartilhar Relatório"));

            Log.d(TAG, "✅ Compartilhamento iniciado");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao compartilhar PDF: ", e);
        }
    }

    // Abrir PDF
    public static void abrirPDF(Context context, File pdfFile) {
        try {
            android.net.Uri uri = androidx.core.content.FileProvider.getUriForFile(
                    context,
                    context.getApplicationContext().getPackageName() + ".fileprovider",
                    pdfFile
            );

            android.content.Intent intent = new android.content.Intent(
                    android.content.Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "application/pdf");
            intent.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(android.content.Intent.createChooser(
                    intent, "Abrir PDF"));

            Log.d(TAG, "✅ PDF aberto");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao abrir PDF: ", e);
        }
    }
}