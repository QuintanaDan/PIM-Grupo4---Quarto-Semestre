using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("Usuarios")]
    public class Usuario
    {
        [Key]
        public long Id { get; set; }

        [Required]
        [MaxLength(100)]
        public string Nome { get; set; }

        [Required]
        [MaxLength(100)]
        public string Email { get; set; }

        [Required]
        [MaxLength(255)]
        public string Senha { get; set; }

        [MaxLength(20)]
        public string? Contato { get; set; }

        [Required]
        public int Tipo { get; set; } // 0=Cliente, 1=Admin

        public DateTime DataCriacao { get; set; } = DateTime.Now;

        public bool Ativo { get; set; } = true;

        // Navegação
        public ICollection<Chamado> Chamados { get; set; }
        public ICollection<Comentario> Comentarios { get; set; }
    }
}