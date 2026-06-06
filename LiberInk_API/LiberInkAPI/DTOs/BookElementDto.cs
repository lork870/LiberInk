using System.Text.Json.Serialization;

namespace LiberInkAPI.DTOs
{
    public class BookElementDto
    {
        [JsonPropertyName("id")]
        public int Id { get; set; }

        [JsonPropertyName("bookId")]
        public int BookId { get; set; }

        [JsonPropertyName("parentId")]
        public int? ParentId { get; set; }

        [JsonPropertyName("title")]
        public string Title { get; set; } = string.Empty;

        [JsonPropertyName("content")]
        public string Content { get; set; } = string.Empty;

        [JsonPropertyName("type")]
        public string Type { get; set; } = string.Empty;

        [JsonPropertyName("orderIndex")]
        public int OrderIndex { get; set; }

        [JsonPropertyName("children")]
        public List<BookElementDto> Children { get; set; } = new List<BookElementDto>();
    }
}