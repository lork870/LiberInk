using LiberInkAPI.Data;
using LiberInkAPI.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;

namespace LiberInkAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class BooksController : ControllerBase
    {
        private readonly AppDbContext _context;

        public BooksController(AppDbContext context)
        {
            _context = context;
        }

        // --- РОБОТА З КНИГАМИ ---

        [HttpGet("user/{userId}")]
        public async Task<ActionResult<IEnumerable<Book>>> GetUserBooks(string userId)
        {
            return await _context.Books
                .Where(b => b.UserId == userId)
                .ToListAsync();
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<Book>> GetBook(int id)
        {
            var book = await _context.Books.FindAsync(id);
            if (book == null) return NotFound();
            return book;
        }

        [HttpPost]
        public async Task<ActionResult<Book>> PostBook(Book book)
        {
            _context.Books.Add(book);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetBook), new { id = book.Id }, book);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> PutBook(int id, Book book)
        {
            if (id != book.Id) return BadRequest();
            _context.Entry(book).State = EntityState.Modified;
            await _context.SaveChangesAsync();
            return NoContent();
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteBook(int id)
        {
            var book = await _context.Books.FindAsync(id);
            if (book == null) return NotFound();
            _context.Books.Remove(book);
            await _context.SaveChangesAsync();
            return NoContent();
        }

        // --- РОБОТА ЗІ СТРУКТУРОЮ КНИГИ (ЧАСТИНИ ТА ГЛАВИ) ---

        [HttpGet("structure/{bookId}")]
        public async Task<ActionResult<IEnumerable<BookElementDto>>> GetBookStructure(int bookId)
        {
            var elements = await _context.BookElements
                .Where(e => e.BookId == bookId)
                .OrderBy(e => e.Order)
                .ToListAsync();

            var nodes = elements.Select(e => new BookElementDto
            {
                Id = e.Id,
                Title = e.Title,
                Subtitle = e.Subtitle,
                Type = e.Type,
                Order = e.Order,
                Children = new List<BookElementDto>()
            }).ToDictionary(e => e.Id);

            var rootNodes = new List<BookElementDto>();

            foreach (var element in elements)
            {
                var node = nodes[element.Id];
                if (element.ParentId == null)
                {
                    rootNodes.Add(node);
                }
                else if (nodes.ContainsKey(element.ParentId.Value))
                {
                    nodes[element.ParentId.Value].Children.Add(node);
                }
            }

            return Ok(rootNodes);
        }

        [HttpGet("elements/{id}")]
        public async Task<ActionResult<BookElement>> GetElement(int id)
        {
            var element = await _context.BookElements.FindAsync(id);
            if (element == null) return NotFound();
            return element;
        }

        [HttpPost("elements")]
        public async Task<ActionResult<BookElement>> PostElement(BookElement element)
        {
            _context.BookElements.Add(element);
            await _context.SaveChangesAsync();
            return CreatedAtAction(nameof(GetElement), new { id = element.Id }, element);
        }

        [HttpPut("elements/{id}")]
        public async Task<IActionResult> PutElement(int id, BookElement element)
        {
            if (id != element.Id) return BadRequest();

            // Щоб уникнути помилок з відстеженням сутностей
            _context.Entry(element).State = EntityState.Modified;

            try
            {
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!_context.BookElements.Any(e => e.Id == id)) return NotFound();
                throw;
            }
            return NoContent();
        }

        // --- НОВИЙ МЕТОД ДЛЯ ПЕРЕМІЩЕННЯ ---
        [HttpPatch("elements/{id}/move")]
        public async Task<IActionResult> MoveElement(int id, [FromBody] MoveElementRequest request)
        {
            var element = await _context.BookElements.FindAsync(id);
            if (element == null) return NotFound();

            // Оновлюємо батьківський елемент та порядок
            element.ParentId = request.NewParentId;
            element.Order = request.NewOrder;

            await _context.SaveChangesAsync();
            return NoContent();
        }

        [HttpDelete("elements/{id}")]
        public async Task<IActionResult> DeleteElement(int id)
        {
            var element = await _context.BookElements.FindAsync(id);
            if (element == null) return NotFound();
            _context.BookElements.Remove(element);
            await _context.SaveChangesAsync();
            return NoContent();
        }
    }

    // Допоміжний клас для запиту переміщення
    public class MoveElementRequest
    {
        public int? NewParentId { get; set; }
        public int NewOrder { get; set; }
    }
}