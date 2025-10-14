# 🎫 HelpDesk App - Sistema de Gestão de Chamados

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Java](https://img.shields.io/badge/Language-Java-orange.svg)
![SQLite](https://img.shields.io/badge/Database-SQLite-blue.svg)
![License](https://img.shields.io/badge/License-MIT-yellow.svg)

Sistema completo de gestão de chamados de suporte técnico para Android, com foco em diversidade, inclusão e representatividade étnico-racial.

---

## 📋 Índice

- [Sobre o Projeto](#sobre-o-projeto)
- [Funcionalidades](#funcionalidades)
- [Tecnologias Utilizadas](#tecnologias-utilizadas)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Uso](#uso)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Temas](#temas)
- [Screenshots](#screenshots)
- [Contribuindo](#contribuindo)
- [Licença](#licença)
- [Contato](#contato)

---

## 🎯 Sobre o Projeto

O **HelpDesk App** é uma solução mobile completa para gerenciamento de chamados de suporte técnico, desenvolvida com foco em:

- ✅ **Funcionalidade**: Sistema robusto e completo
- 🎨 **Design Moderno**: Interface intuitiva com Material Design
- 🌍 **Diversidade**: Valorização da representatividade étnico-racial
- 👑 **Inclusão**: Tema Ancestral dedicado à cultura afrodescendente

### 🌟 Diferenciais

- **3 Temas Personalizados** (Amanhecer, Anoitecer e Ancestral)
- **Sistema de Auditoria Completo** (rastreamento de todas as ações)
- **Dashboard com Gráficos Interativos** (análise visual de dados)
- **Mensagens de Consciência Racial** (promovendo diversidade)
- **Navigation Drawer** (navegação moderna e intuitiva)

---

## 🚀 Funcionalidades

### 👤 Autenticação e Usuários
- [x] Cadastro de usuários (Cliente/Admin)
- [x] Login com validação
- [x] Sessão persistente
- [x] Controle de permissões por tipo de usuário
- [x] Logout com auditoria

### 🎫 Gestão de Chamados
- [x] Abrir chamado (título, descrição, categoria, prioridade)
- [x] Listar chamados do usuário
- [x] Listar todos os chamados (Admin)
- [x] Buscar chamados por protocolo/título
- [x] Detalhes completos do chamado
- [x] Alterar status (Aberto → Em Andamento → Resolvido → Fechado)
- [x] Protocolo automático

### 💬 Sistema de Comentários
- [x] Adicionar comentários em chamados
- [x] Visualizar histórico de comentários
- [x] Identificação do autor
- [x] Data/hora dos comentários

### ⭐ Sistema de Avaliações
- [x] Avaliar chamados resolvidos
- [x] Rating com estrelas (1-5)
- [x] Comentário opcional
- [x] Visualização da média de avaliações

### 📎 Sistema de Anexos
- [x] Upload de fotos/documentos
- [x] Armazenamento no diretório do app
- [x] Listar e visualizar anexos
- [x] Gerenciar múltiplos arquivos

### 🏷️ Sistema de Tags
- [x] Criar tags personalizadas (Admin)
- [x] Adicionar tags aos chamados
- [x] Cores personalizadas para tags
- [x] Gerenciar tags (CRUD completo)

### 📜 Sistema de Auditoria
- [x] Registro automático de ações:
  - Login/Logout
  - Criação de chamados
  - Comentários
  - Avaliações
  - Anexos
  - Alteração de status
  - Criação de tags
- [x] Histórico completo de ações
- [x] Filtros (Todas/Minhas ações/Por tipo)
- [x] Informações do dispositivo
- [x] Timeline visual

### 👨‍💼 Painel Administrativo
- [x] Dashboard com estatísticas em tempo real
- [x] Contadores por status
- [x] Filtros avançados
- [x] Gerenciamento completo de chamados
- [x] Gráficos interativos (Pizza, Barras, Linha)

### 📄 Relatórios em PDF
- [x] Gerar relatórios gerais
- [x] Relatórios filtrados por status
- [x] Exportar para PDF
- [x] Compartilhar relatórios
- [x] Formatação profissional

### 🔔 Notificações Push
- [x] Notificação ao criar chamado
- [x] Notificação ao receber comentário
- [x] Notificação de mudança de status
- [x] Canal de notificações configurado

### 🎨 Sistema de Temas
- [x] **Tema Amanhecer** (Claro e suave)
- [x] **Tema Anoitecer** (Escuro moderno)
- [x] **Tema Ancestral** (Afrocêntrico - cores da terra e ancestralidade)
- [x] Tela de seleção visual
- [x] Persistência da escolha
- [x] Mensagem sobre representatividade

### 🌍 Diversidade e Inclusão
- [x] Card MOVER (diversidade racial na tech)
- [x] Tela "Consciência e Diversidade"
- [x] Tema Ancestral afrocêntrico
- [x] Mensagens de consciência racial
- [x] Link para organização MOVER

---

## 🛠️ Tecnologias Utilizadas

### Linguagem e Framework
- **Java** - Linguagem principal
- **Android SDK** - Framework Android
- **Material Design Components** - UI/UX moderna

### Banco de Dados
- **SQLite** - Banco de dados local
- **DatabaseHelper** - Gerenciamento de banco

### Bibliotecas Principais
- **RecyclerView** - Listas eficientes
- **CardView** - Cards visuais
- **iTextPDF** - Geração de PDF
- **MPAndroidChart** - Gráficos interativos
- **Material Components** - Componentes de UI

### Arquitetura
- **MVC** (Model-View-Controller)
- **DAO** (Data Access Object)
- **Singleton** (SessionManager, ThemeManager)

---

## 📋 Pré-requisitos

Antes de começar, você precisa ter instalado:

- [Android Studio](https://developer.android.com/studio) (versão 2023.1 ou superior)
- JDK 11 ou superior
- Android SDK (API 24 ou superior)
- Emulador Android ou dispositivo físico

---

## 🔧 Instalação

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/helpdesk-app.git
cd helpdesk-app