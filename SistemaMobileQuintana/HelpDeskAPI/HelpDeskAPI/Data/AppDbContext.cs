using Microsoft.EntityFrameworkCore;
using HelpDeskAPI.Models;

namespace HelpDeskAPI.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<Usuario> Usuarios { get; set; }
        public DbSet<Chamado> Chamados { get; set; }
        public DbSet<Comentario> Comentarios { get; set; }
        public DbSet<Tag> Tags { get; set; }
        public DbSet<ChamadoTag> ChamadoTags { get; set; }
        public DbSet<Notificacao> Notificacoes { get; set; }
        public DbSet<Auditoria> Auditorias { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // ✅ Mapear nomes de tabelas para snake_case (minúsculo)
            modelBuilder.Entity<Usuario>().ToTable("usuarios");
            modelBuilder.Entity<Chamado>().ToTable("chamados");
            modelBuilder.Entity<Comentario>().ToTable("comentarios");
            modelBuilder.Entity<Tag>().ToTable("tags");
            modelBuilder.Entity<ChamadoTag>().ToTable("chamado_tags");
            modelBuilder.Entity<Notificacao>().ToTable("notificacoes");
            modelBuilder.Entity<Auditoria>().ToTable("auditorias");

            // ✅ Mapear nomes de colunas para snake_case
            modelBuilder.Entity<Usuario>(entity =>
            {
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Nome).HasColumnName("nome");
                entity.Property(e => e.Email).HasColumnName("email");
                entity.Property(e => e.Senha).HasColumnName("senha");
                entity.Property(e => e.Tipo).HasColumnName("tipo");
                entity.Property(e => e.Contato).HasColumnName("contato");
                entity.Property(e => e.Ativo).HasColumnName("ativo");
                entity.Property(e => e.DataCriacao).HasColumnName("data_criacao");
            });

            modelBuilder.Entity<Chamado>(entity =>
            {
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Protocolo).HasColumnName("protocolo");
                entity.Property(e => e.Titulo).HasColumnName("titulo");
                entity.Property(e => e.Descricao).HasColumnName("descricao");
                entity.Property(e => e.DataCriacao).HasColumnName("data_criacao");
                entity.Property(e => e.Categoria).HasColumnName("categoria");
                entity.Property(e => e.Prioridade).HasColumnName("prioridade");
                entity.Property(e => e.Status).HasColumnName("status");
                entity.Property(e => e.DataFechamento).HasColumnName("data_fechamento");
                entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
                entity.Property(e => e.TecnicoId).HasColumnName("tecnico_id");
            });

            modelBuilder.Entity<Comentario>(entity =>
            {
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Texto).HasColumnName("texto");
                entity.Property(e => e.DataCriacao).HasColumnName("data_criacao");
                entity.Property(e => e.ChamadoId).HasColumnName("chamado_id");
                entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
            });

            modelBuilder.Entity<Tag>(entity =>
            {
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Nome).HasColumnName("nome");
                entity.Property(e => e.Cor).HasColumnName("cor");
            });

            modelBuilder.Entity<ChamadoTag>(entity =>
            {
                entity.HasKey(ct => new { ct.ChamadoId, ct.TagId });
                entity.Property(e => e.ChamadoId).HasColumnName("chamado_id");
                entity.Property(e => e.TagId).HasColumnName("tag_id");
            });

            modelBuilder.Entity<Notificacao>(entity =>
            {
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Titulo).HasColumnName("titulo");
                entity.Property(e => e.Mensagem).HasColumnName("mensagem");
                entity.Property(e => e.Tipo).HasColumnName("tipo");
                entity.Property(e => e.Lida).HasColumnName("lida");
                entity.Property(e => e.DataCriacao).HasColumnName("data_criacao");
                entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
                entity.Property(e => e.ChamadoId).HasColumnName("chamado_id");
            });

            modelBuilder.Entity<Auditoria>(entity =>
            {
                entity.Property(e => e.Id).HasColumnName("id");
                entity.Property(e => e.Acao).HasColumnName("acao");
                entity.Property(e => e.DataHora).HasColumnName("data_hora");
                entity.Property(e => e.UsuarioId).HasColumnName("usuario_id");
                entity.Property(e => e.ChamadoId).HasColumnName("chamado_id");
            });
        }
    }
}