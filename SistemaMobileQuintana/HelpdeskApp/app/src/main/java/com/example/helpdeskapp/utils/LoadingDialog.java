package com.example.helpdeskapp.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import com.airbnb.lottie.LottieAnimationView;
import com.example.helpdeskapp.R;

public class LoadingDialog {

    private Dialog dialog;
    private TextView tvLoadingMessage;
    private TextView tvLoadingSubMessage;
    private LottieAnimationView lottieLoading;

    public LoadingDialog(Context context) {
        // Criar dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        // Inflar o layout customizado
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_loading, null);
        dialog.setContentView(view);

        // Configurar dialog
        dialog.setCancelable(false); // Não fecha ao clicar fora
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Referenciar views
        tvLoadingMessage = view.findViewById(R.id.tvLoadingMessage);
        tvLoadingSubMessage = view.findViewById(R.id.tvLoadingSubMessage);
        lottieLoading = view.findViewById(R.id.lottieLoading);
    }

    /**
     * Mostrar o dialog com mensagem padrão
     */
    public void show() {
        show("Carregando...", "Aguarde um momento");
    }

    /**
     * Mostrar o dialog com mensagem customizada
     */
    public void show(String message) {
        show(message, "Aguarde um momento");
    }

    /**
     * Mostrar o dialog com mensagens customizadas
     */
    public void show(String message, String subMessage) {
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
        }
        if (tvLoadingSubMessage != null) {
            tvLoadingSubMessage.setText(subMessage);
        }
        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    /**
     * Atualizar a mensagem enquanto está mostrando
     */
    public void updateMessage(String message) {
        if (tvLoadingMessage != null) {
            tvLoadingMessage.setText(message);
        }
    }

    /**
     * Atualizar a sub-mensagem
     */
    public void updateSubMessage(String subMessage) {
        if (tvLoadingSubMessage != null) {
            tvLoadingSubMessage.setText(subMessage);
        }
    }

    /**
     * Esconder o dialog
     */
    public void dismiss() {
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    /**
     * Verificar se está mostrando
     */
    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
