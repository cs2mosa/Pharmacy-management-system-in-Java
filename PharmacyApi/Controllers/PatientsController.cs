using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using PharmacyApi.Models;
using PharmacyApi.Services;

namespace PharmacyApi.Controllers;

[ApiController]
[Route("api/patients")]
public sealed class PatientsController : ControllerBase
{
    private readonly IPatientService _patients;

    public PatientsController(IPatientService patients) => _patients = patients;

    [HttpGet("{id:int}")]
    public ActionResult<PatientDto> Get(int id)
    {
        var p = _patients.Get(id);
        return p is null ? NotFound() : Ok(p);
    }

    [HttpGet]
    public ActionResult<IReadOnlyList<PatientDto>> GetAll() => Ok(_patients.GetAll());

    [HttpPost]
    public ActionResult<int> Create([FromBody] CreatePatientRequest request)
    {
        try
        {
            if (string.IsNullOrWhiteSpace(request.Username) || string.IsNullOrWhiteSpace(request.Password))
                return BadRequest("Username and password are required.");
            var id = _patients.Create(request);
            return CreatedAtAction(nameof(Get), new { id }, id);
        }
        catch (SqlException ex) when (ex.Number is 2627 or 2601)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    [HttpPut("{id:int}")]
    public IActionResult Update(int id, [FromBody] UpdatePatientRequest request)
    {
        try
        {
            return _patients.Update(id, request) ? NoContent() : NotFound();
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
            return _patients.Delete(id) ? NoContent() : NotFound();
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return Conflict(new { message = "Cannot delete patient due to related records.", detail = ex.Message });
        }
    }

    [HttpPost("{id:int}/allergies")]
    public IActionResult AddAllergy(int id, [FromBody] PatientAllergyDto body)
    {
        if (string.IsNullOrWhiteSpace(body.AllergyName)) return BadRequest("AllergyName is required.");
        try
        {
            return _patients.AddAllergy(id, body.AllergyName.Trim()) ? NoContent() : Conflict("Allergy already exists.");
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return NotFound(new { message = "Patient not found.", detail = ex.Message });
        }
    }

    [HttpDelete("{id:int}/allergies/{allergyName}")]
    public IActionResult RemoveAllergy(int id, string allergyName)
    {
        return _patients.RemoveAllergy(id, Uri.UnescapeDataString(allergyName)) ? NoContent() : NotFound();
    }
}
