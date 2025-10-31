using System;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("comentarios")]
    public class Comentario
    {
        [Key]
        [Column("Id")]
        public long Id { get; set; }

        [Required]
        [Column("ChamadoId")]
        public long ChamadoId { get; set; }

        [Required]
        [Column("UsuarioId")]
        public long UsuarioId { get; set; }

        [Required]
        [Column("Texto")]
        [MaxLength(1000)]
        public string Texto { get; set; }

        // ✅ CORREÇÃO: Mapear DataHora para DataCriacao
        [Column("DataCriacao")]
        public DateTime DataHora { get; set; } = DateTime.UtcNow;

        // Navegação
        [ForeignKey("ChamadoId")]
        public virtual Chamado? Chamado { get; set; }

        [ForeignKey("UsuarioId")]
        public virtual Usuario? Usuario { get; set; }
    }
}