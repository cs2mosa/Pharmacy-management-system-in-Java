using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using PharmacyApi.Models;
using PharmacyApi.Services;

namespace PharmacyApi.Controllers;

[ApiController]
[Route("api/medicines")]
public sealed class MedicinesController : ControllerBase
{
    private readonly IMedicineService _medicines;

    public MedicinesController(IMedicineService medicines) => _medicines = medicines;

    [HttpGet("{id:int}")]
    public ActionResult<MedicineDto> Get(int id)
    {
        var m = _medicines.Get(id);
        return m is null ? NotFound() : Ok(m);
    }

    [HttpGet("by-name/{name}")]
    public ActionResult<MedicineDto> GetByName(string name)
    {
        var m = _medicines.GetByName(Uri.UnescapeDataString(name));
        return m is null ? NotFound() : Ok(m);
    }

    [HttpGet]
    public ActionResult<IReadOnlyList<MedicineDto>> GetAll() => Ok(_medicines.GetAll());

    [HttpGet("by-category")]
    public ActionResult<IReadOnlyList<MedicineDto>> GetByCategory([FromQuery] string category)
    {
        if (string.IsNullOrWhiteSpace(category)) return BadRequest("category is required.");
        return Ok(_medicines.GetByCategory(category));
    }

    [HttpPost]
    public ActionResult<int> Create([FromBody] UpsertMedicineRequest request)
    {
        try
        {
            if (string.IsNullOrWhiteSpace(request.Name)) return BadRequest("Name is required.");
            var id = _medicines.Create(request);
            return CreatedAtAction(nameof(Get), new { id }, id);
        }
        catch (SqlException ex) when (ex.Number is 2627 or 2601)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    [HttpPut("{id:int}")]
    public IActionResult Update(int id, [FromBody] UpsertMedicineRequest request)
    {
        try
        {
            return _medicines.Update(id, request) ? NoContent() : NotFound();
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
            return _medicines.Delete(id) ? NoContent() : NotFound();
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return Conflict(new { message = "Cannot delete medicine due to related records.", detail = ex.Message });
        }
    }

    [HttpPatch("{id:int}/stock")]
    public IActionResult SetStock(int id, [FromBody] StockBody body)
    {
        if (body.Quantity < 0) return BadRequest("Quantity must be non-negative.");
        return _medicines.SetStock(id, body.Quantity) ? NoContent() : NotFound();
    }

    public sealed record StockBody(int Quantity);
}
