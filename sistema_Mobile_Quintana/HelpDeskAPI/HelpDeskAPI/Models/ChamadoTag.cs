using System.ComponentModel.DataAnnotations.Schema;

namespace HelpDeskAPI.Models
{
    [Table("ChamadoTags")]
    public class ChamadoTag
    {
        public long ChamadoId { get; set; }
        public long TagId { get; set; }

        // Navegação
        [ForeignKey("ChamadoId")]
        public Chamado Chamado { get; set; }

        [ForeignKey("TagId")]
        public Tag Tag { get; set; }
    }
}