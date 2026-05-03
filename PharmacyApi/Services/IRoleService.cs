using PharmacyApi.Models;

namespace PharmacyApi.Services;

public interface IRoleService
{
    IReadOnlyList<RoleDto> GetAll();
}
