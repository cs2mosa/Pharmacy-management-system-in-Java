using PharmacyApi.Models;

namespace PharmacyApi.Services;

public interface IOrderService
{
    OrderDto? Get(int orderId);
    IReadOnlyList<OrderDto> GetByPatient(int patientId);
    IReadOnlyList<OrderDto> GetHistory();
    int Create(CreateOrderRequest request);
    bool Update(int orderId, UpdateOrderRequest request);
    bool Delete(int patientId, int orderId);
}
