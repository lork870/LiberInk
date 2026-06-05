namespace LiberInkAPI.Models
{
    public class BookElement
    {
        public int Id { get; set; }
        public int BookId { get; set; }
        
        // ParentId дозволяє робити вкладеність (папка в папці, сцена в розділі)
        public int? ParentId { get; set; }
        
        public string Title { get; set; } = string.Empty;
        public string Content { get; set; } = string.Empty;
        
        // Тип елемента: наприклад, "Folder", "Chapter", "Document"
        public string ElementType { get; set; } = "Document";
        
        // Для збереження порядку при перетягуванні
        public int OrderIndex { get; set; } 
    }
}