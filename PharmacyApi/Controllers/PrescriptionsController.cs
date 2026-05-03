using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using PharmacyApi.Models;
using PharmacyApi.Services;

namespace PharmacyApi.Controllers;

[ApiController]
[Route("api/prescriptions")]
public sealed class PrescriptionsController : ControllerBase
{
    private readonly IPrescriptionService _prescriptions;

    public PrescriptionsController(IPrescriptionService prescriptions) => _prescriptions = prescriptions;

    [HttpGet("{id:int}")]
    public ActionResult<PrescriptionDto> Get(int id)
    {
        var p = _prescriptions.Get(id);
        return p is null ? NotFound() : Ok(p);
    }

    [HttpGet("patient/{patientId:int}")]
    public ActionResult<IReadOnlyList<PrescriptionDto>> ByPatient(int patientId) =>
        Ok(_prescriptions.GetByPatient(patientId));

    [HttpGet]
    public ActionResult<IReadOnlyList<PrescriptionDto>> GetAll() => Ok(_prescriptions.GetAll());

    [HttpPost]
    public ActionResult<int> Create([FromBody] CreatePrescriptionRequest request)
    {
        try
        {
            var id = _prescriptions.Create(request);
            return CreatedAtAction(nameof(Get), new { id }, id);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (SqlException ex) when (ex.Number is 2627 or 2601)
        {
            return Conflict(new { message = ex.Message });
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return BadRequest(new { message = "Invalid foreign key reference.", detail = ex.Message });
        }
    }

    [HttpPut("{id:int}")]
    public IActionResult Update(int id, [FromBody] UpdatePrescriptionRequest request) =>
        _prescriptions.Update(id, request) ? NoContent() : NotFound();

    [HttpDelete("patient/{patientId:int}/{prescriptionId:int}")]
    public IActionResult Delete(int patientId, int prescriptionId)
    {
        try
        {
            return _prescriptions.Delete(patientId, prescriptionId) ? NoContent() : NotFound();
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return Conflict(new { message = "Cannot delete prescription.", detail = ex.Message });
        }
    }
}
