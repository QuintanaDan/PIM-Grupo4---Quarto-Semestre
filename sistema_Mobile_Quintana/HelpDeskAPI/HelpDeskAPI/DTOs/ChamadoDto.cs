using System.ComponentModel.DataAnnotations;

namespace HelpDeskAPI.DTOs
{
    public class ChamadoCreateDto
    {
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
    }

    public class ChamadoUpdateDto
    {
        [MaxLength(200)]
        public string? Titulo { get; set; }

        public string? Descricao { get; set; }

        [MaxLength(50)]
        public string? Categoria { get; set; }

        [MaxLength(20)]
        public string? Prioridade { get; set; }

        [MaxLength(20)]
        public string? Status { get; set; }
    }

    public class ChamadoResponseDto
    {
        public long Id { get; set; }
        public string Protocolo { get; set; }
        public string Titulo { get; set; }
        public string Descricao { get; set; }
        public long UsuarioId { get; set; }
        public string NomeUsuario { get; set; }
        public string EmailUsuario { get; set; }
        public string Categoria { get; set; }
        public string Prioridade { get; set; }
        public string Status { get; set; }
        public DateTime DataAbertura { get; set; }
        public DateTime? DataFechamento { get; set; }
        public int TotalComentarios { get; set; }
    }
}