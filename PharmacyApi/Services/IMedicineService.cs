using PharmacyApi.Models;

namespace PharmacyApi.Services;

public interface IMedicineService
{
    MedicineDto? Get(int medicineId);
    MedicineDto? GetByName(string name);
    IReadOnlyList<MedicineDto> GetAll();
    IReadOnlyList<MedicineDto> GetByCategory(string category);
    int Create(UpsertMedicineRequest request);
    bool Update(int medicineId, UpsertMedicineRequest request);
    bool Delete(int medicineId);
    bool SetStock(int medicineId, int newQuantity);
}
