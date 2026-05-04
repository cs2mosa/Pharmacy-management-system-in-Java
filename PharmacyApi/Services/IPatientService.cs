using PharmacyApi.Models;

namespace PharmacyApi.Services;

public interface IPatientService
{
    PatientDto? Get(int userId);
    IReadOnlyList<PatientDto> GetAll();
    int Create(CreatePatientRequest request);
    bool Update(int userId, UpdatePatientRequest request);
    bool Delete(int userId);
    bool AddAllergy(int userId, string allergyName);
    bool RemoveAllergy(int userId, string allergyName);
}
