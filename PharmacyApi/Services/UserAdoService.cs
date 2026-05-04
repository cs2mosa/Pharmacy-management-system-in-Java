using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Models;

namespace PharmacyApi.Services;

public sealed class UserAdoService : IUserService
{
    private readonly ISqlConnectionFactory _db;

    public UserAdoService(ISqlConnectionFactory db) => _db = db;

    public UserDto? GetById(int id)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        const string sql = """
            SELECT UserID, Username, Email, Phone, IsActive
            FROM [dbo].[USER]
            WHERE UserID = @id
            """;
        using var cmd = new SqlCommand(sql, cn);
        cmd.Parameters.AddWithValue("@id", id);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        var dto = MapUserRow(r);
        r.Close();
        dto = dto with { RoleIds = LoadRoleIds(cn, id), UserKind = ResolveUserKind(cn, id) };
        return dto;
    }

    public UserDto? GetByUsername(string username)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        const string sql = """
            SELECT UserID, Username, Email, Phone, IsActive
            FROM [dbo].[USER]
            WHERE Username = @u
            """;
        using var cmd = new SqlCommand(sql, cn);
        cmd.Parameters.AddWithValue("@u", username);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        var id = r.GetInt32(0);
        var dto = MapUserRow(r);
        r.Close();
        return dto with { RoleIds = LoadRoleIds(cn, id), UserKind = ResolveUserKind(cn, id) };
    }

    public UserDto? Login(LoginRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        const string sql = """
            SELECT UserID, Username, Email, Phone, IsActive
            FROM [dbo].[USER]
            WHERE Username = @u AND [Password] = @p AND IsActive = 1
            """;
        using var cmd = new SqlCommand(sql, cn);
        cmd.Parameters.AddWithValue("@u", request.Username);
        cmd.Parameters.AddWithValue("@p", request.Password);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        var id = r.GetInt32(0);
        var dto = MapUserRow(r);
        r.Close();
        return dto with { RoleIds = LoadRoleIds(cn, id), UserKind = ResolveUserKind(cn, id) };
    }

    public int Create(CreateUserRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var tx = cn.BeginTransaction();
        try
        {
            const string insUser = """
                INSERT INTO [dbo].[USER] (Username, [Password], Email, Phone, IsActive)
                OUTPUT INSERTED.UserID
                VALUES (@un, @pw, @em, @ph, 1)
                """;
            using (var cmd = new SqlCommand(insUser, cn, tx))
            {
                cmd.Parameters.AddWithValue("@un", request.Username);
                cmd.Parameters.AddWithValue("@pw", request.Password);
                cmd.Parameters.AddWithValue("@em", (object?)request.Email ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@ph", (object?)request.Phone ?? DBNull.Value);
                var newIdObj = cmd.ExecuteScalar() ?? throw new InvalidOperationException("User insert failed.");
                var newId = Convert.ToInt32(newIdObj);

                foreach (var roleId in request.RoleIds.Distinct())
                {
                    using var cr = new SqlCommand(
                        "INSERT INTO [dbo].[USER_ROLE] (UserID, RoleID) VALUES (@uid, @rid)", cn, tx);
                    cr.Parameters.AddWithValue("@uid", newId);
                    cr.Parameters.AddWithValue("@rid", roleId);
                    cr.ExecuteNonQuery();
                }

                var kind = request.UserKind.Trim();
                if (string.Equals(kind, "Patient", StringComparison.OrdinalIgnoreCase))
                {
                    using var cp = new SqlCommand(
                        """
                        INSERT INTO [dbo].[PATIENT] (UserID, Age, Address, PatientBalance)
                        VALUES (@uid, @age, @addr, @bal)
                        """, cn, tx);
                    cp.Parameters.AddWithValue("@uid", newId);
                    cp.Parameters.AddWithValue("@age", request.Age ?? 0f);
                    cp.Parameters.AddWithValue("@addr", (object?)request.Address ?? DBNull.Value);
                    cp.Parameters.AddWithValue("@bal", (object?)request.PatientBalance ?? 0d);
                    cp.ExecuteNonQuery();
                }
                else if (string.Equals(kind, "Pharmacist", StringComparison.OrdinalIgnoreCase)
                         || string.Equals(kind, "Casher", StringComparison.OrdinalIgnoreCase))
                {
                    using var ce = new SqlCommand(
                        """
                        INSERT INTO [dbo].[EMPLOYEE] (UserID, Salary, JobType)
                        VALUES (@uid, @sal, @job)
                        """, cn, tx);
                    ce.Parameters.AddWithValue("@uid", newId);
                    ce.Parameters.AddWithValue("@sal", (object?)request.Salary ?? 0m);
                    ce.Parameters.AddWithValue("@job", kind.Equals("Casher", StringComparison.OrdinalIgnoreCase) ? "Casher" : "Pharmacist");
                    ce.ExecuteNonQuery();
                }

                tx.Commit();
                return newId;
            }
        }
        catch
        {
            tx.Rollback();
            throw;
        }
    }

    public bool Update(int id, UpdateUserRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        var sets = new List<string>();
        var cmd = new SqlCommand { Connection = cn };
        cmd.Parameters.AddWithValue("@id", id);
        if (request.Username is { } un)
        {
            sets.Add("Username = @un");
            cmd.Parameters.AddWithValue("@un", un);
        }
        if (request.Password is { } pw)
        {
            sets.Add("[Password] = @pw");
            cmd.Parameters.AddWithValue("@pw", pw);
        }
        if (request.Email is { } em)
        {
            sets.Add("Email = @em");
            cmd.Parameters.AddWithValue("@em", em);
        }
        if (request.Phone is { } ph)
        {
            sets.Add("Phone = @ph");
            cmd.Parameters.AddWithValue("@ph", ph);
        }
        if (request.IsActive is { } ia)
        {
            sets.Add("IsActive = @ia");
            cmd.Parameters.AddWithValue("@ia", ia);
        }
        if (sets.Count == 0) return true;
        cmd.CommandText = $"UPDATE [dbo].[USER] SET {string.Join(", ", sets)} WHERE UserID = @id";
        return cmd.ExecuteNonQuery() > 0;
    }

    public bool Delete(int id)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand("DELETE FROM [dbo].[USER] WHERE UserID = @id", cn);
        cmd.Parameters.AddWithValue("@id", id);
        return cmd.ExecuteNonQuery() > 0;
    }

    private static UserDto MapUserRow(SqlDataReader r) => new(
        r.GetInt32(0),
        r.GetString(1),
        r.IsDBNull(2) ? null : r.GetString(2),
        r.IsDBNull(3) ? null : r.GetString(3),
        !r.IsDBNull(4) && r.GetBoolean(4),
        "Unknown",
        Array.Empty<int>());

    private static IReadOnlyList<int> LoadRoleIds(SqlConnection cn, int userId)
    {
        using var cmd = new SqlCommand(
            "SELECT RoleID FROM [dbo].[USER_ROLE] WHERE UserID = @id ORDER BY RoleID", cn);
        cmd.Parameters.AddWithValue("@id", userId);
        using var r = cmd.ExecuteReader();
        var list = new List<int>();
        while (r.Read()) list.Add(r.GetInt32(0));
        return list;
    }

    private static string ResolveUserKind(SqlConnection cn, int userId)
    {
        using var p = new SqlCommand("SELECT 1 FROM [dbo].[PATIENT] WHERE UserID = @id", cn);
        p.Parameters.AddWithValue("@id", userId);
        if (p.ExecuteScalar() is not null) return "Patient";

        using var e = new SqlCommand("SELECT JobType FROM [dbo].[EMPLOYEE] WHERE UserID = @id", cn);
        e.Parameters.AddWithValue("@id", userId);
        var o = e.ExecuteScalar();
        if (o is string jt) return jt;
        return "User";
    }
}
