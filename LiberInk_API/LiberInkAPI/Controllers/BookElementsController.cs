using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using LiberInkAPI.Data;
using LiberInkAPI.Models;

namespace LiberInkAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class BookElementsController : ControllerBase
    {
        private readonly AppDbContext _context;

        public BookElementsController(AppDbContext context)
        {
            _context = context;
        }

        // Отримати всю структуру конкретної книги
        [HttpGet("book/{bookId}")]
        public async Task<ActionResult<IEnumerable<BookElement>>> GetElementsByBook(int bookId)
        {
            return await _context.BookElements
                .Where(e => e.BookId == bookId)
                .OrderBy(e => e.OrderIndex)
                .ToListAsync();
        }

        // Створити новий розділ чи документ
        [HttpPost]
        public async Task<ActionResult<BookElement>> CreateElement(BookElement element)
        {
            _context.BookElements.Add(element);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetElementsByBook), new { bookId = element.BookId }, element);
        }

        // Оновити назву, контент або перемістити (змінити ParentId/OrderIndex)
        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateElement(int id, BookElement element)
        {
            if (id != element.Id) return BadRequest();

            _context.Entry(element).State = EntityState.Modified;
            await _context.SaveChangesAsync();
            return NoContent();
        }

        // Видалити елемент
        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteElement(int id)
        {
            var element = await _context.BookElements.FindAsync(id);
            if (element == null) return NotFound();

            _context.BookElements.Remove(element);
            await _context.SaveChangesAsync();
            return NoContent();
        }
    }
}