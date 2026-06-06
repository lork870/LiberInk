using Microsoft.AspNetCore.Mvc;
using LiberInkAPI.DTOs;
using LiberInkAPI.Services;
using LiberInkAPI.Models;

namespace LiberInkAPI.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class BookElementsController : ControllerBase
    {
        private readonly IBookElementService _service;

        public BookElementsController(IBookElementService service)
        {
            _service = service;
        }

        [HttpGet("book/{bookId}")]
        public async Task<ActionResult<IEnumerable<BookElementDto>>> GetElementsByBook(int bookId)
        {
            return Ok(await _service.GetBookHierarchyAsync(bookId));
        }

        [HttpGet("{id}")]
        public async Task<ActionResult<BookElement>> GetElement(int id)
        {
            var element = await _service.GetElementByIdAsync(id);
            return element == null ? NotFound() : Ok(element);
        }

        [HttpPost]
        public async Task<ActionResult<BookElement>> CreateElement(BookElement element)
        {
            await _service.CreateElementAsync(element);
            return Ok(element);
        }

        [HttpPut("{id}")]
        public async Task<IActionResult> UpdateElement(int id, BookElement element)
        {
            if (id != element.Id) return BadRequest();
            await _service.UpdateElementAsync(element);
            return NoContent();
        }

        [HttpDelete("{id}")]
        public async Task<IActionResult> DeleteElement(int id)
        {
            await _service.DeleteElementAsync(id);
            return NoContent();
        }

        [HttpPatch("{id}/move")]
        public async Task<IActionResult> MoveElement(int id, [FromBody] MoveRequest request)
        {
            await _service.MoveElementAsync(id, request);
            return NoContent();
        }
    }
}