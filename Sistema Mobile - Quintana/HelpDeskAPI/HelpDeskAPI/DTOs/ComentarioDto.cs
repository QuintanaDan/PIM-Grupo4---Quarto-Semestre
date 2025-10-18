using System.ComponentModel.DataAnnotations;

namespace HelpDeskAPI.DTOs
{
    public class ComentarioCreateDto
    {
        [Required]
        public long ChamadoId { get; set; }

        [Required]
        public long UsuarioId { get; set; }

        [Required]
        public string Texto { get; set; }
    }

    public class ComentarioResponseDto
    {
        public long Id { get; set; }
        public long ChamadoId { get; set; }
        public long UsuarioId { get; set; }
        public string NomeUsuario { get; set; }
        public string Texto { get; set; }
        public DateTime DataHora { get; set; }
    }
}