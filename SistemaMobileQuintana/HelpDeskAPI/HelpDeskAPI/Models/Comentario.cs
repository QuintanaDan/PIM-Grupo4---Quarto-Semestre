using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("Comentarios")]
    public class Comentario
    {
        [Key]
        public long Id { get; set; }

        [Required]
        public long ChamadoId { get; set; }

        [Required]
        public long UsuarioId { get; set; }

        [Required]
        public string Texto { get; set; }

        public DateTime DataHora { get; set; } = DateTime.Now;

        // Navegação
        [ForeignKey("ChamadoId")]
        public Chamado Chamado { get; set; }

        [ForeignKey("UsuarioId")]
        public Usuario Usuario { get; set; }
    }
}