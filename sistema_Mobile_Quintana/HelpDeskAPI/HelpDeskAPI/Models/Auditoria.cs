using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("Auditoria")]
    public class Auditoria
    {
        [Key]
        public long Id { get; set; }

        public long? UsuarioId { get; set; }

        [Required]
        [MaxLength(100)]
        public string Acao { get; set; }

        [MaxLength(500)]
        public string? Descricao { get; set; }

        public DateTime DataHora { get; set; } = DateTime.Now;

        [MaxLength(45)]
        public string? EnderecoIP { get; set; }

        // Navegação
        [ForeignKey("UsuarioId")]
        public Usuario? Usuario { get; set; }
    }
}