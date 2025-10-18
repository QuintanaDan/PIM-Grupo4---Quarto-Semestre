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
    public class ChamadosController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ChamadosController(AppDbContext context)
        {
            _context = context;
        }

        /// Listar todos os chamados (Admin) ou do usuário (Cliente)
        [HttpGet]
        public async Task<ActionResult<IEnumerable<ChamadoResponseDto>>> GetAll([FromQuery] long? usuarioId = null)
        {
            var isAdmin = User.IsInRole("Admin");
            var currentUserId = long.Parse(User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value ?? "0");

            IQueryable<Chamado> query = _context.Chamados
                .Include(c => c.Usuario)
                .Include(c => c.Comentarios);

            // Se não for admin, filtrar apenas chamados do usuário
            if (!isAdmin)
            {
                query = query.Where(c => c.UsuarioId == currentUserId);
            }
            else if (usuarioId.HasValue)
            {
                query = query.Where(c => c.UsuarioId == usuarioId.Value);
            }

            var chamados = await query
                .OrderByDescending(c => c.DataAbertura)
                .Select(c => new ChamadoResponseDto
                {
                    Id = c.Id,
                    Protocolo = c.Protocolo,
                    Titulo = c.Titulo,
                    Descricao = c.Descricao,
                    UsuarioId = c.UsuarioId,
                    NomeUsuario = c.Usuario.Nome,
                    EmailUsuario = c.Usuario.Email,
                    Categoria = c.Categoria,
                    Prioridade = c.Prioridade,
                    Status = c.Status,
                    DataAbertura = c.DataAbertura,
                    DataFechamento = c.DataFechamento,
                    TotalComentarios = c.Comentarios.Count
                })
                .ToListAsync();

            return Ok(chamados);
        }

        /// Buscar chamado por ID
        [HttpGet("{id}")]
        public async Task<ActionResult<ChamadoResponseDto>> GetById(long id)
        {
            var isAdmin = User.IsInRole("Admin");
            var currentUserId = long.Parse(User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value ?? "0");

            var chamado = await _context.Chamados
                .Include(c => c.Usuario)
                .Include(c => c.Comentarios)
                .FirstOrDefaultAsync(c => c.Id == id);

            if (chamado == null)
            {
                return NotFound(new { message = "❌ Chamado não encontrado" });
            }

            // Verificar permissão
            if (!isAdmin && chamado.UsuarioId != currentUserId)
            {
                return Forbid();
            }

            var response = new ChamadoResponseDto
            {
                Id = chamado.Id,
                Protocolo = chamado.Protocolo,
                Titulo = chamado.Titulo,
                Descricao = chamado.Descricao,
                UsuarioId = chamado.UsuarioId,
                NomeUsuario = chamado.Usuario.Nome,
                EmailUsuario = chamado.Usuario.Email,
                Categoria = chamado.Categoria,
                Prioridade = chamado.Prioridade,
                Status = chamado.Status,
                DataAbertura = chamado.DataAbertura,
                DataFechamento = chamado.DataFechamento,
                TotalComentarios = chamado.Comentarios.Count
            };

            return Ok(response);
        }

        /// Criar novo chamado
        [HttpPost]
        public async Task<ActionResult<ChamadoResponseDto>> Create([FromBody] ChamadoCreateDto dto)
        {
            // Gerar protocolo único
            var protocolo = "CH" + new Random().Next(100000, 999999).ToString();
            while (await _context.Chamados.AnyAsync(c => c.Protocolo == protocolo))
            {
                protocolo = "CH" + new Random().Next(100000, 999999).ToString();
            }

            var chamado = new Chamado
            {
                Protocolo = protocolo,
                Titulo = dto.Titulo,
                Descricao = dto.Descricao,
                UsuarioId = dto.UsuarioId,
                Categoria = dto.Categoria,
                Prioridade = dto.Prioridade,
                Status = "Aberto"
            };

            _context.Chamados.Add(chamado);
            await _context.SaveChangesAsync();

            // Notificar admins
            await NotificarAdmins(chamado);

            // Carregar dados do usuário
            await _context.Entry(chamado).Reference(c => c.Usuario).LoadAsync();

            var response = new ChamadoResponseDto
            {
                Id = chamado.Id,
                Protocolo = chamado.Protocolo,
                Titulo = chamado.Titulo,
                Descricao = chamado.Descricao,
                UsuarioId = chamado.UsuarioId,
                NomeUsuario = chamado.Usuario.Nome,
                EmailUsuario = chamado.Usuario.Email,
                Categoria = chamado.Categoria,
                Prioridade = chamado.Prioridade,
                Status = chamado.Status,
                DataAbertura = chamado.DataAbertura,
                TotalComentarios = 0
            };

            return CreatedAtAction(nameof(GetById), new { id = chamado.Id }, response);
        }

        /// Atualizar chamado
        [HttpPut("{id}")]
        public async Task<IActionResult> Update(long id, [FromBody] ChamadoUpdateDto dto)
        {
            var isAdmin = User.IsInRole("Admin");
            var currentUserId = long.Parse(User.FindFirst(System.Security.Claims.ClaimTypes.NameIdentifier)?.Value ?? "0");

            var chamado = await _context.Chamados.FindAsync(id);

            if (chamado == null)
            {
                return NotFound(new { message = "❌ Chamado não encontrado" });
            }

            // Verificar permissão
            if (!isAdmin && chamado.UsuarioId != currentUserId)
            {
                return Forbid();
            }

            // Atualizar campos se fornecidos
            if (!string.IsNullOrEmpty(dto.Titulo))
                chamado.Titulo = dto.Titulo;

            if (!string.IsNullOrEmpty(dto.Descricao))
                chamado.Descricao = dto.Descricao;

            if (!string.IsNullOrEmpty(dto.Categoria))
                chamado.Categoria = dto.Categoria;

            if (!string.IsNullOrEmpty(dto.Prioridade))
                chamado.Prioridade = dto.Prioridade;

            if (!string.IsNullOrEmpty(dto.Status))
            {
                var statusAntigo = chamado.Status;
                chamado.Status = dto.Status;

                // Se fechou o chamado, registrar data
                if (dto.Status == "Fechado" && statusAntigo != "Fechado")
                {
                    chamado.DataFechamento = DateTime.Now;
                }

                // Notificar cliente sobre mudança de status
                if (statusAntigo != dto.Status)
                {
                    await NotificarCliente(chamado, $"Status alterado de '{statusAntigo}' para '{dto.Status}'");
                }
            }

            _context.Entry(chamado).State = EntityState.Modified;
            await _context.SaveChangesAsync();

            return NoContent();
        }

        /// Deletar chamado (Admin apenas)
        [HttpDelete("{id}")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> Delete(long id)
        {
            var chamado = await _context.Chamados.FindAsync(id);

            if (chamado == null)
            {
                return NotFound(new { message = "❌ Chamado não encontrado" });
            }

            _context.Chamados.Remove(chamado);
            await _context.SaveChangesAsync();

            return NoContent();
        }

        // Métodos auxiliares
        private async Task NotificarAdmins(Chamado chamado)
        {
            var admins = await _context.Usuarios
                .Where(u => u.Tipo == 1 && u.Ativo)
                .ToListAsync();

            var usuario = await _context.Usuarios.FindAsync(chamado.UsuarioId);

            foreach (var admin in admins)
            {
                var notificacao = new Notificacao
                {
                    UsuarioId = admin.Id,
                    ChamadoId = chamado.Id,
                    Titulo = "Novo Chamado Aberto",
                    Mensagem = $"{usuario.Nome} abriu o chamado #{chamado.Protocolo}: {chamado.Titulo}",
                    Tipo = "NOVO_CHAMADO"
                };

                _context.Notificacoes.Add(notificacao);
            }

            await _context.SaveChangesAsync();
        }

        private async Task NotificarCliente(Chamado chamado, string mensagem)
        {
            var notificacao = new Notificacao
            {
                UsuarioId = chamado.UsuarioId,
                ChamadoId = chamado.Id,
                Titulo = "Atualização no Chamado",
                Mensagem = mensagem,
                Tipo = "STATUS_ALTERADO"
            };

            _context.Notificacoes.Add(notificacao);
            await _context.SaveChangesAsync();
        }
    }
}