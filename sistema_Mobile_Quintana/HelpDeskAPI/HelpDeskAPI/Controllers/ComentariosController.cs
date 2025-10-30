using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using HelpDeskAPI.Data;
using HelpDeskAPI.DTOs;
using HelpDeskAPI.Models;

namespace HelpDeskAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class ComentariosController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ComentariosController(AppDbContext context)
        {
            _context = context;
        }


        /// Listar comentários de um chamado
        [HttpGet("chamado/{chamadoId}")]
        public async Task<ActionResult<IEnumerable<ComentarioResponseDto>>> GetByChamado(long chamadoId)
        {
            var comentarios = await _context.Comentarios
                .Include(c => c.Usuario)
                .Where(c => c.ChamadoId == chamadoId)
                .OrderBy(c => c.DataHora)
                .Select(c => new ComentarioResponseDto
                {
                    Id = c.Id,
                    ChamadoId = c.ChamadoId,
                    UsuarioId = c.UsuarioId,
                    NomeUsuario = c.Usuario.Nome,
                    Texto = c.Texto,
                    DataHora = c.DataHora
                })
                .ToListAsync();

            return Ok(comentarios);
        }


        /// Criar novo comentário

        [HttpPost]
        public async Task<ActionResult<ComentarioResponseDto>> Create([FromBody] ComentarioCreateDto dto)
        {
            // Verificar se chamado existe
            var chamado = await _context.Chamados.FindAsync(dto.ChamadoId);
            if (chamado == null)
            {
                return NotFound(new { message = "❌ Chamado não encontrado" });
            }

            var comentario = new Comentario
            {
                ChamadoId = dto.ChamadoId,
                UsuarioId = dto.UsuarioId,
                Texto = dto.Texto
            };

            _context.Comentarios.Add(comentario);
            await _context.SaveChangesAsync();

            // Notificar cliente (se comentário foi feito por admin)
            var usuario = await _context.Usuarios.FindAsync(dto.UsuarioId);
            if (usuario.Tipo == 1 && chamado.UsuarioId != dto.UsuarioId)
            {
                var notificacao = new Notificacao
                {
                    UsuarioId = chamado.UsuarioId,
                    ChamadoId = chamado.Id,
                    Titulo = "Novo Comentário",
                    Mensagem = $"Novo comentário no chamado #{chamado.Protocolo}",
                    Tipo = "NOVO_COMENTARIO"
                };

                _context.Notificacoes.Add(notificacao);
                await _context.SaveChangesAsync();
            }

            // Carregar dados do usuário
            await _context.Entry(comentario).Reference(c => c.Usuario).LoadAsync();

            var response = new ComentarioResponseDto
            {
                Id = comentario.Id,
                ChamadoId = comentario.ChamadoId,
                UsuarioId = comentario.UsuarioId,
                NomeUsuario = comentario.Usuario.Nome,
                Texto = comentario.Texto,
                DataHora = comentario.DataHora
            };

            return CreatedAtAction(nameof(GetByChamado), new { chamadoId = dto.ChamadoId }, response);
        }
    }
}
