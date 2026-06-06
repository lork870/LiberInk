using LiberInkAPI.Data;
using LiberInkAPI.DTOs;
using LiberInkAPI.Models;
using Microsoft.EntityFrameworkCore;

namespace LiberInkAPI.Services
{
    public class MoveRequest { public int? NewParentId { get; set; } public int NewOrder { get; set; } }

    public interface IBookElementService
    {
        Task<List<BookElementDto>> GetBookHierarchyAsync(int bookId);
        Task<BookElement?> GetElementByIdAsync(int id);
        Task CreateElementAsync(BookElement element);
        Task UpdateElementAsync(BookElement element);
        Task DeleteElementAsync(int id);
        Task MoveElementAsync(int id, MoveRequest request);
    }

    public class BookElementService : IBookElementService
    {
        private readonly AppDbContext _context;
        public BookElementService(AppDbContext context) => _context = context;

        public async Task<List<BookElementDto>> GetBookHierarchyAsync(int bookId)
        {
            var elements = await _context.BookElements
                .Where(e => e.BookId == bookId)
                .OrderBy(e => e.OrderIndex)
                .ToListAsync();

            var dtos = elements.Select(e => new BookElementDto {
                Id = e.Id, 
                BookId = e.BookId,
                Title = e.Title, 
                Content = e.Content,
                ParentId = e.ParentId, 
                Type = e.ElementType, 
                OrderIndex = e.OrderIndex
            }).ToList();

            var lookup = dtos.ToLookup(e => e.ParentId);
            foreach (var dto in dtos) dto.Children = lookup[dto.Id].ToList();
            return lookup[null].ToList();
        }

        public async Task<BookElement?> GetElementByIdAsync(int id) 
            => await _context.BookElements.FindAsync(id);

        public async Task CreateElementAsync(BookElement element) {
            _context.BookElements.Add(element);
            await _context.SaveChangesAsync();
        }

        public async Task UpdateElementAsync(BookElement element) {
            _context.Entry(element).State = EntityState.Modified;
            await _context.SaveChangesAsync();
        }

        public async Task DeleteElementAsync(int id) {
            var el = await _context.BookElements.FindAsync(id);
            if (el != null) {
                // Видаляємо дітей (якщо це папка)
                var children = await _context.BookElements.Where(e => e.ParentId == id).ToListAsync();
                _context.BookElements.RemoveRange(children);
                
                _context.BookElements.Remove(el);
                await _context.SaveChangesAsync();
            }
        }

        public async Task MoveElementAsync(int id, MoveRequest request) {
            var el = await _context.BookElements.FindAsync(id);
            if (el != null) {
                el.ParentId = request.NewParentId;
                el.OrderIndex = request.NewOrder;
                await _context.SaveChangesAsync();
            }
        }
    }
}