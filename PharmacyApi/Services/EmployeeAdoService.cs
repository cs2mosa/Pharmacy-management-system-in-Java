using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;

namespace PharmacyApi.Services;

public sealed class EmployeeAdoService : IEmployeeService
{
    private readonly ISqlConnectionFactory _db;

    public EmployeeAdoService(ISqlConnectionFactory db) => _db = db;

    public EmployeeDto? Get(int userId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "SELECT UserID, Salary, JobType FROM [dbo].[EMPLOYEE] WHERE UserID=@id", cn);
        cmd.Parameters.AddWithValue("@id", userId);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        return new EmployeeDto(r.GetInt32(0), r.GetDecimal(1), r.GetString(2));
    }
}
