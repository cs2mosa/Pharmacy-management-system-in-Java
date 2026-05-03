using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using PharmacyApi.Models;
using PharmacyApi.Services;

namespace PharmacyApi.Controllers;

[ApiController]
[Route("api/users")]
public sealed class UsersController : ControllerBase
{
    private readonly IUserService _users;

    public UsersController(IUserService users) => _users = users;

    [HttpGet("{id:int}")]
    public ActionResult<UserDto> GetById(int id)
    {
        var u = _users.GetById(id);
        return u is null ? NotFound() : Ok(u);
    }

    [HttpGet("by-username")]
    public ActionResult<UserDto> GetByUsername([FromQuery] string username)
    {
        if (string.IsNullOrWhiteSpace(username)) return BadRequest("username is required.");
        var u = _users.GetByUsername(username);
        return u is null ? NotFound() : Ok(u);
    }

    [HttpPost("login")]
    public ActionResult<UserDto> Login([FromBody] LoginRequest request)
    {
        if (string.IsNullOrWhiteSpace(request.Username) || string.IsNullOrWhiteSpace(request.Password))
            return BadRequest("Username and password are required.");
        var u = _users.Login(request);
        return u is null ? Unauthorized() : Ok(u);
    }

    [HttpPost]
    public ActionResult<int> Create([FromBody] CreateUserRequest request)
    {
        try
        {
            if (string.IsNullOrWhiteSpace(request.Username) || string.IsNullOrWhiteSpace(request.Password))
                return BadRequest("Username and password are required.");
            var id = _users.Create(request);
            return CreatedAtAction(nameof(GetById), new { id }, id);
        }
        catch (SqlException ex) when (ex.Number is 2627 or 2601)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    [HttpPut("{id:int}")]
    public IActionResult Update(int id, [FromBody] UpdateUserRequest request)
    {
        try
        {
            return _users.Update(id, request) ? NoContent() : NotFound();
        }
        catch (SqlException ex) when (ex.Number is 2627 or 2601)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    [HttpDelete("{id:int}")]
    public IActionResult Delete(int id)
    {
        try
        {
            return _users.Delete(id) ? NoContent() : NotFound();
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return Conflict(new { message = "Cannot delete user due to related records.", detail = ex.Message });
        }
    }
}
