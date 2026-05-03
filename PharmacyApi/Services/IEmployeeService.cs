namespace PharmacyApi.Services;

public sealed record EmployeeDto(int UserId, decimal Salary, string JobType);

public interface IEmployeeService
{
    EmployeeDto? Get(int userId);
}
