using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using PharmacyApi.Models;
using PharmacyApi.Services;

namespace PharmacyApi.Controllers;

[ApiController]
[Route("api/payments")]
public sealed class PaymentsController : ControllerBase
{
    private readonly IPaymentService _payments;

    public PaymentsController(IPaymentService payments) => _payments = payments;

    [HttpGet("{id:int}")]
    public ActionResult<PaymentDto> Get(int id)
    {
        var p = _payments.Get(id);
        return p is null ? NotFound() : Ok(p);
    }

    [HttpGet("patient/{patientId:int}")]
    public ActionResult<IReadOnlyList<PaymentDto>> ByPatient(int patientId) =>
        Ok(_payments.GetByPatient(patientId));

    [HttpGet]
    public ActionResult<IReadOnlyList<PaymentDto>> GetAll() => Ok(_payments.GetAll());

    [HttpPost]
    public ActionResult<int> Create([FromBody] CreatePaymentRequest request)
    {
        try
        {
            var id = _payments.Create(request);
            return CreatedAtAction(nameof(Get), new { id }, id);
        }
        catch (SqlException ex) when (ex.Number is 2627 or 2601)
        {
            return Conflict(new { message = ex.Message });
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return BadRequest(new { message = "Invalid order or reference.", detail = ex.Message });
        }
    }

    [HttpPut("{id:int}")]
    public IActionResult Update(int id, [FromBody] UpdatePaymentRequest request) =>
        _payments.Update(id, request) ? NoContent() : NotFound();

    [HttpDelete("{id:int}")]
    public IActionResult Delete(int id) => _payments.Delete(id) ? NoContent() : NotFound();
}
