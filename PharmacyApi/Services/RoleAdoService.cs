using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Models;

namespace PharmacyApi.Services;

public sealed class RoleAdoService : IRoleService
{
    private readonly ISqlConnectionFactory _db;

    public RoleAdoService(ISqlConnectionFactory db) => _db = db;

    public IReadOnlyList<RoleDto> GetAll()
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "SELECT RoleID, RoleName, PermissionsLevel, Description FROM [dbo].[ROLE] ORDER BY RoleID", cn);
        using var r = cmd.ExecuteReader();
        var list = new List<RoleDto>();
        while (r.Read())
        {
            list.Add(new RoleDto(
                r.GetInt32(0),
                r.GetString(1),
                r.GetInt32(2),
                r.IsDBNull(3) ? null : r.GetString(3)));
        }
        return list;
    }
}
