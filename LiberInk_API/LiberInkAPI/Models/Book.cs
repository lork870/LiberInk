using System.ComponentModel.DataAnnotations;

namespace LiberInkAPI.Models
{
    public class Book
    {
        [Key]
        public int Id { get; set; }

        [Required]
        [MaxLength(200)]
        public string Title { get; set; } = string.Empty;

        public string Description { get; set; } = string.Empty;

        [Required]
        public string UserId { get; set; } = string.Empty;

        [Required]
        public string AuthorPseudonym { get; set; } = string.Empty;

        public DateTime LastEdited { get; set; } = DateTime.UtcNow;

        // Навігаційна властивість (опціонально, для зв'язку з елементами)
        public ICollection<BookElement> Elements { get; set; } = new List<BookElement>();
    }
}