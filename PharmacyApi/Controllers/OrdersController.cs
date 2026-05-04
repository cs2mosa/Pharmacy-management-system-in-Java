using Microsoft.AspNetCore.Mvc;
using Microsoft.Data.SqlClient;
using PharmacyApi.Models;
using PharmacyApi.Services;

namespace PharmacyApi.Controllers;

[ApiController]
[Route("api/orders")]
public sealed class OrdersController : ControllerBase
{
    private readonly IOrderService _orders;

    public OrdersController(IOrderService orders) => _orders = orders;

    [HttpGet("{id:int}")]
    public ActionResult<OrderDto> Get(int id)
    {
        var o = _orders.Get(id);
        return o is null ? NotFound() : Ok(o);
    }

    [HttpGet("patient/{patientId:int}")]
    public ActionResult<IReadOnlyList<OrderDto>> GetByPatient(int patientId) =>
        Ok(_orders.GetByPatient(patientId));

    [HttpGet("history")]
    public ActionResult<IReadOnlyList<OrderDto>> History() => Ok(_orders.GetHistory());

    [HttpPost]
    public ActionResult<int> Create([FromBody] CreateOrderRequest request)
    {
        try
        {
            var id = _orders.Create(request);
            return CreatedAtAction(nameof(Get), new { id }, id);
        }
        catch (ArgumentException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        catch (InvalidOperationException ex)
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
    public IActionResult Update(int id, [FromBody] UpdateOrderRequest request) =>
        _orders.Update(id, request) ? NoContent() : NotFound();

    [HttpDelete("patient/{patientId:int}/{orderId:int}")]
    public IActionResult Delete(int patientId, int orderId)
    {
        try
        {
            return _orders.Delete(patientId, orderId) ? NoContent() : NotFound();
        }
        catch (SqlException ex) when (ex.Number == 547)
        {
            return Conflict(new { message = "Cannot delete order due to related records.", detail = ex.Message });
        }
    }
}
