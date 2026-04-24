namespace LiberInkAPI.Models
{
    public partial class Book
    {
        public int Id { get; set; }
        public string Title { get; set; }
        public string Description { get; set; }
        public string UserId { get; set; }
        public string AuthorPseudonym { get; set; }
        public DateTime LastEdited { get; set; } = DateTime.Now;
    }
}