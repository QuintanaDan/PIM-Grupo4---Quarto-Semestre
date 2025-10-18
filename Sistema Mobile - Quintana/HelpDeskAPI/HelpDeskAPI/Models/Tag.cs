using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("Tags")]
    public class Tag
    {
        [Key]
        public long Id { get; set; }

        [Required]
        [MaxLength(50)]
        public string Nome { get; set; }

        [MaxLength(7)]
        public string Cor { get; set; } = "#2196F3";

        // Navegação
        public ICollection<ChamadoTag> ChamadoTags { get; set; }
    }
}