using PharmacyApi.Models;

namespace PharmacyApi.Services;

public interface IPrescriptionService
{
    PrescriptionDto? Get(int prescriptionId);
    IReadOnlyList<PrescriptionDto> GetByPatient(int patientId);
    IReadOnlyList<PrescriptionDto> GetAll();
    int Create(CreatePrescriptionRequest request);
    bool Update(int prescriptionId, UpdatePrescriptionRequest request);
    bool Delete(int patientId, int prescriptionId);
}
