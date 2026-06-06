using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace LiberInkAPI.Models
{
    public class BookElement
    {
        [Key]
        public int Id { get; set; }
        
        public int BookId { get; set; }
        
        public int? ParentId { get; set; }
        
        [Required]
        public string Title { get; set; } = string.Empty;
        
        public string Content { get; set; } = string.Empty;
        
        // Використовуємо просту стрінгу: "Part" або "Chapter"
        [Required]
        public string ElementType { get; set; } = "Chapter"; // <-- Змінив на ElementType, щоб не ламати БД
        
        public int OrderIndex { get; set; } 
        
        // Навігаційні властивості
        [ForeignKey("BookId")]
        public Book? Book { get; set; }
    }
}