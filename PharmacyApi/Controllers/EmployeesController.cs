using Microsoft.AspNetCore.Mvc;
using PharmacyApi.Services;

namespace PharmacyApi.Controllers;

[ApiController]
[Route("api/employees")]
public sealed class EmployeesController : ControllerBase
{
    private readonly IEmployeeService _employees;

    public EmployeesController(IEmployeeService employees) => _employees = employees;

    [HttpGet("{userId:int}")]
    public ActionResult<EmployeeDto> Get(int userId)
    {
        var e = _employees.Get(userId);
        return e is null ? NotFound() : Ok(e);
    }
}
