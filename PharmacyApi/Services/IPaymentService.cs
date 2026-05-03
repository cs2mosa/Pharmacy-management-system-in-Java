using PharmacyApi.Models;

namespace PharmacyApi.Services;

public interface IPaymentService
{
    PaymentDto? Get(int paymentId);
    IReadOnlyList<PaymentDto> GetByPatient(int patientId);
    IReadOnlyList<PaymentDto> GetAll();
    int Create(CreatePaymentRequest request);
    bool Update(int paymentId, UpdatePaymentRequest request);
    bool Delete(int paymentId);
}
