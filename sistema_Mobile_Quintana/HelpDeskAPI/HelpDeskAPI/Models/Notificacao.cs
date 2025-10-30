using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("Notificacoes")]
    public class Notificacao
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public long UsuarioId { get; set; }

        public long? ChamadoId { get; set; }

        [Required]
        [MaxLength(100)]
        public string Titulo { get; set; }

        [Required]
        [MaxLength(500)]
        public string Mensagem { get; set; }

        [Required]
        [MaxLength(50)]
        public string Tipo { get; set; }

        public bool Lida { get; set; } = false;

        public DateTime DataHora { get; set; } = DateTime.Now;

        // Navegação
        [ForeignKey("UsuarioId")]
        public Usuario Usuario { get; set; }

        [ForeignKey("ChamadoId")]
        public Chamado? Chamado { get; set; }
    }
}