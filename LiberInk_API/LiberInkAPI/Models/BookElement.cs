using System.Text.Json.Serialization;

namespace LiberInkAPI.Models
{
    public class BookElement
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public string? Subtitle { get; set; }

        public int Order { get; set; }
        public string Type { get; set; }

        public int BookId { get; set; }

        [JsonIgnore] // <--- ДОДАЙТЕ ЦЕ, щоб сервер не чекав об'єкт книги в JSON
        public Book? Book { get; set; }

        public int? ParentId { get; set; }

        [JsonIgnore] // <--- ДОДАЙТЕ ЦЕ
        public BookElement? Parent { get; set; }

        [JsonIgnore] // <--- І ЦЕ, щоб не було проблем з циклічністю
        public List<BookElement> Children { get; set; } = new();

        public string? Content { get; set; }
    }
}