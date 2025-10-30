using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("Chamados")]
    public class Chamado
    {
        [Key]
        public long Id { get; set; }

        [Required]
        [MaxLength(20)]
        public string Protocolo { get; set; }

        [Required]
        [MaxLength(200)]
        public string Titulo { get; set; }

        [Required]
        public string Descricao { get; set; }

        [Required]
        public long UsuarioId { get; set; }

        [Required]
        [MaxLength(50)]
        public string Categoria { get; set; }

        [Required]
        [MaxLength(20)]
        public string Prioridade { get; set; }

        [Required]
        [MaxLength(20)]
        public string Status { get; set; } = "Aberto";

        public DateTime DataAbertura { get; set; } = DateTime.Now;

        public DateTime? DataFechamento { get; set; }

        // Navegação
        [ForeignKey("UsuarioId")]
        public Usuario Usuario { get; set; }

        public ICollection<Comentario> Comentarios { get; set; }
        public ICollection<ChamadoTag> ChamadoTags { get; set; }
    }
}