using System.Collections.Generic;
using System.ComponentModel.DataAnnotations.Schema;

namespace LiberInkAPI.Models
{
    public class BookElement
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public string? Subtitle { get; set; } // ? означає, що може бути NULL
        public int Order { get; set; }
        public string Type { get; set; } // 'Part' або 'Chapter'
        public int BookId { get; set; }
        public int? ParentId { get; set; }
        public string? Content { get; set; }

        // Ця властивість не зберігається в БД (через NotMapped), 
        // але потрібна для того, щоб React міг відмалювати дерево (Part -> Chapters)
        [NotMapped]
        public List<BookElement> Children { get; set; } = new List<BookElement>();
    }
}