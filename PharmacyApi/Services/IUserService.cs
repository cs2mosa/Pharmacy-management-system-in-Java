using PharmacyApi.Models;

namespace PharmacyApi.Services;

public interface IUserService
{
    UserDto? GetById(int id);
    UserDto? GetByUsername(string username);
    UserDto? Login(LoginRequest request);
    int Create(CreateUserRequest request);
    bool Update(int id, UpdateUserRequest request);
    bool Delete(int id);
}
