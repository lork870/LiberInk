using Microsoft.EntityFrameworkCore;
using LiberInkAPI.Models;

namespace LiberInkAPI.Data
{
    public class AppDbContext : DbContext
    {
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<Book> Books { get; set; }

        public DbSet<BookElement> BookElements { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            base.OnModelCreating(modelBuilder);

            // Налаштування зв'язку: Книга -> Елементи (при видаленні книги видаляються елементи)
            modelBuilder.Entity<BookElement>()
                .HasOne<Book>()
                .WithMany()
                .HasForeignKey(e => e.BookId)
                .OnDelete(DeleteBehavior.Cascade);

            // Налаштування ієрархії: Елемент -> Дочірні елементи
            modelBuilder.Entity<BookElement>()
                .HasOne<BookElement>()
                .WithMany()
                .HasForeignKey(e => e.ParentId)
                .OnDelete(DeleteBehavior.Cascade);
        }
    
    }
}