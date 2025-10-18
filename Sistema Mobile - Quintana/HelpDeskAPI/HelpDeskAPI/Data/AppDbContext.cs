using HelpDeskAPI.Controllers;
using HelpDeskAPI.Models;
using Microsoft.EntityFrameworkCore;

namespace HelpDeskAPI.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
        {
        }

        // DbSets
        public DbSet<Usuario> Usuarios { get; set; }
        public DbSet<Chamado> Chamados { get; set; }
        public DbSet<Comentario> Comentarios { get; set; }
        public DbSet<Notificacao> Notificacoes { get; set; }
        public DbSet<Tag> Tags { get; set; }
        public DbSet<ChamadoTag> ChamadoTags { get; set; }
        public DbSet<Auditoria> Auditorias { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Configurar chave composta para ChamadoTag
            modelBuilder.Entity<ChamadoTag>()
                .HasKey(ct => new { ct.ChamadoId, ct.TagId });

            // Configurar relacionamentos
            modelBuilder.Entity<ChamadoTag>()
                .HasOne(ct => ct.Chamado)
                .WithMany(c => c.ChamadoTags)
                .HasForeignKey(ct => ct.ChamadoId);

            modelBuilder.Entity<ChamadoTag>()
                .HasOne(ct => ct.Tag)
                .WithMany(t => t.ChamadoTags)
                .HasForeignKey(ct => ct.TagId);

            // Índices únicos
            modelBuilder.Entity<Usuario>()
                .HasIndex(u => u.Email)
                .IsUnique();

            modelBuilder.Entity<Chamado>()
                .HasIndex(c => c.Protocolo)
                .IsUnique();
        }
    }
}