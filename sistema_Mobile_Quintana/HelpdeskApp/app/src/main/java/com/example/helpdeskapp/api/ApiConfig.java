package com.example.helpdeskapp.api;

public class ApiConfig {

    // ========================================
    // CONFIGURAÇÃO PARA APRESENTAÇÃO
    // ========================================

    // OPÇÃO 1: Para celular físico via USB (ADB Reverse)
    // RECOMENDADO PARA APRESENTAÇÃO!
    public static final String BASE_URL = "http://localhost:7170/";

    // OPÇÃO 2: Para emulador Android Studio
    // public static final String BASE_URL = "http://10.0.2.2:7170/";

    // OPÇÃO 3: Para celular via Wi-Fi (BACKUP)
    // Substituir 192.168.X.X pelo IP do notebook no dia
    // public static final String BASE_URL = "http://192.168.1.220:7170/";

    // Endpoints
    public static final String LOGIN = "api/Auth/login";
    public static final String REGISTER = "api/Auth/register";
    public static final String CHAMADOS = "api/Chamados";
    public static final String COMENTARIOS = "api/Comentarios";
}