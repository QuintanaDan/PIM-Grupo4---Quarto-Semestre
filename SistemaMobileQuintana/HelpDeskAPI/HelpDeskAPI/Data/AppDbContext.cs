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

            // Apenas mapear nomes de tabelas (snake_case)
            modelBuilder.Entity<Usuario>().ToTable("usuarios");
            modelBuilder.Entity<Chamado>().ToTable("chamados");
            modelBuilder.Entity<Comentario>().ToTable("comentarios");
            modelBuilder.Entity<Tag>().ToTable("tags");
            modelBuilder.Entity<ChamadoTag>().ToTable("chamado_tags");
            modelBuilder.Entity<Notificacao>().ToTable("notificacoes");
            modelBuilder.Entity<Auditoria>().ToTable("auditorias");

            // Configurar chave composta de ChamadoTag
            modelBuilder.Entity<ChamadoTag>()
                .HasKey(ct => new { ct.ChamadoId, ct.TagId });

            //Converter DateTime para UTC
    modelBuilder.Entity<Auditoria>()
        .Property(a => a.DataHora)
        .HasConversion(v => v.ToUniversalTime(),
                       v => DateTime.SpecifyKind(v, DateTimeKind.Utc));
        }
    }
}