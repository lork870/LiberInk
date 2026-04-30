namespace LiberInkAPI.Models
{
    public class BookElementDto
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public string? Subtitle { get; set; }
        public string Type { get; set; } // "Part" або "Chapter"
        public int Order { get; set; }

        // Список дочірніх елементів для побудови дерева
        public List<BookElementDto> Children { get; set; } = new();
    }
}