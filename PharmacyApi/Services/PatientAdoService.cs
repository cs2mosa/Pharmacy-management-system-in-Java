using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Models;

namespace PharmacyApi.Services;

public sealed class PatientAdoService : IPatientService
{
    private readonly ISqlConnectionFactory _db;

    public PatientAdoService(ISqlConnectionFactory db) => _db = db;

    public PatientDto? Get(int userId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        const string sql = """
            SELECT u.UserID, u.Username, u.Email, u.Phone, u.IsActive, p.Age, p.Address, p.PatientBalance
            FROM [dbo].[USER] u
            INNER JOIN [dbo].[PATIENT] p ON p.UserID = u.UserID
            WHERE u.UserID = @id
            """;
        using var cmd = new SqlCommand(sql, cn);
        cmd.Parameters.AddWithValue("@id", userId);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        var dto = new PatientDto(
            r.GetInt32(0),
            r.GetString(1),
            r.IsDBNull(2) ? null : r.GetString(2),
            r.IsDBNull(3) ? null : r.GetString(3),
            !r.IsDBNull(4) && r.GetBoolean(4),
            r.GetFloat(5),
            r.IsDBNull(6) ? null : r.GetString(6),
            (double)r.GetDecimal(7),
            Array.Empty<string>());
        r.Close();
        return dto with { Allergies = LoadAllergies(cn, userId) };
    }

    public IReadOnlyList<PatientDto> GetAll()
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        const string sql = """
            SELECT u.UserID, u.Username, u.Email, u.Phone, u.IsActive, p.Age, p.Address, p.PatientBalance
            FROM [dbo].[USER] u
            INNER JOIN [dbo].[PATIENT] p ON p.UserID = u.UserID
            ORDER BY u.UserID
            """;
        using var cmd = new SqlCommand(sql, cn);
        using var r = cmd.ExecuteReader();
        var ids = new List<int>();
        var list = new List<PatientDto>();
        while (r.Read())
        {
            var id = r.GetInt32(0);
            ids.Add(id);
            list.Add(new PatientDto(
                id,
                r.GetString(1),
                r.IsDBNull(2) ? null : r.GetString(2),
                r.IsDBNull(3) ? null : r.GetString(3),
                !r.IsDBNull(4) && r.GetBoolean(4),
                r.GetFloat(5),
                r.IsDBNull(6) ? null : r.GetString(6),
                (double)r.GetDecimal(7),
                Array.Empty<string>()));
        }
        r.Close();
        for (var i = 0; i < list.Count; i++)
            list[i] = list[i] with { Allergies = LoadAllergies(cn, ids[i]) };
        return list;
    }

    public int Create(CreatePatientRequest request)
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
            int newId;
            using (var cmd = new SqlCommand(insUser, cn, tx))
            {
                cmd.Parameters.AddWithValue("@un", request.Username);
                cmd.Parameters.AddWithValue("@pw", request.Password);
                cmd.Parameters.AddWithValue("@em", (object?)request.Email ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@ph", (object?)request.Phone ?? DBNull.Value);
                newId = Convert.ToInt32(cmd.ExecuteScalar() ?? throw new InvalidOperationException("Insert user failed."));
            }

            foreach (var roleId in request.RoleIds.Distinct())
            {
                using var cr = new SqlCommand(
                    "INSERT INTO [dbo].[USER_ROLE] (UserID, RoleID) VALUES (@uid, @rid)", cn, tx);
                cr.Parameters.AddWithValue("@uid", newId);
                cr.Parameters.AddWithValue("@rid", roleId);
                cr.ExecuteNonQuery();
            }

            using (var cp = new SqlCommand(
                       """
                       INSERT INTO [dbo].[PATIENT] (UserID, Age, Address, PatientBalance)
                       VALUES (@uid, @age, @addr, @bal)
                       """, cn, tx))
            {
                cp.Parameters.AddWithValue("@uid", newId);
                cp.Parameters.AddWithValue("@age", request.Age);
                cp.Parameters.AddWithValue("@addr", (object?)request.Address ?? DBNull.Value);
                cp.Parameters.AddWithValue("@bal", request.PatientBalance);
                cp.ExecuteNonQuery();
            }

            if (request.Allergies is not null)
            {
                foreach (var a in request.Allergies.Where(x => !string.IsNullOrWhiteSpace(x)).Distinct(StringComparer.OrdinalIgnoreCase))
                {
                    using var ca = new SqlCommand(
                        "INSERT INTO [dbo].[PATIENT_ALLERGY] (UserID, AllergyName) VALUES (@uid, @an)", cn, tx);
                    ca.Parameters.AddWithValue("@uid", newId);
                    ca.Parameters.AddWithValue("@an", a.Trim());
                    ca.ExecuteNonQuery();
                }
            }

            tx.Commit();
            return newId;
        }
        catch
        {
            tx.Rollback();
            throw;
        }
    }

    public bool Update(int userId, UpdatePatientRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var tx = cn.BeginTransaction();
        try
        {
            var userSets = new List<string>();
            using (var cmd = new SqlCommand { Connection = cn, Transaction = tx })
            {
                cmd.Parameters.AddWithValue("@id", userId);
                if (request.Username is { } un)
                {
                    userSets.Add("Username = @un");
                    cmd.Parameters.AddWithValue("@un", un);
                }
                if (request.Password is { } pw)
                {
                    userSets.Add("[Password] = @pw");
                    cmd.Parameters.AddWithValue("@pw", pw);
                }
                if (request.Email is { } em)
                {
                    userSets.Add("Email = @em");
                    cmd.Parameters.AddWithValue("@em", em);
                }
                if (request.Phone is { } ph)
                {
                    userSets.Add("Phone = @ph");
                    cmd.Parameters.AddWithValue("@ph", ph);
                }
                if (request.IsActive is { } ia)
                {
                    userSets.Add("IsActive = @ia");
                    cmd.Parameters.AddWithValue("@ia", ia);
                }
                if (userSets.Count > 0)
                {
                    cmd.CommandText = $"UPDATE [dbo].[USER] SET {string.Join(", ", userSets)} WHERE UserID = @id";
                    cmd.ExecuteNonQuery();
                }
            }

            var patSets = new List<string>();
            using (var cmd = new SqlCommand { Connection = cn, Transaction = tx })
            {
                cmd.Parameters.AddWithValue("@id", userId);
                if (request.Age is { } ag)
                {
                    patSets.Add("Age = @age");
                    cmd.Parameters.AddWithValue("@age", ag);
                }
                if (request.Address is { } ad)
                {
                    patSets.Add("Address = @addr");
                    cmd.Parameters.AddWithValue("@addr", ad);
                }
                if (request.PatientBalance is { } bal)
                {
                    patSets.Add("PatientBalance = @bal");
                    cmd.Parameters.AddWithValue("@bal", bal);
                }
                if (patSets.Count > 0)
                {
                    cmd.CommandText = $"UPDATE [dbo].[PATIENT] SET {string.Join(", ", patSets)} WHERE UserID = @id";
                    cmd.ExecuteNonQuery();
                }
            }

            tx.Commit();
            return true;
        }
        catch
        {
            tx.Rollback();
            throw;
        }
    }

    public bool Delete(int userId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand("DELETE FROM [dbo].[USER] WHERE UserID = @id", cn);
        cmd.Parameters.AddWithValue("@id", userId);
        return cmd.ExecuteNonQuery() > 0;
    }

    public bool AddAllergy(int userId, string allergyName)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "INSERT INTO [dbo].[PATIENT_ALLERGY] (UserID, AllergyName) VALUES (@id, @a)", cn);
        cmd.Parameters.AddWithValue("@id", userId);
        cmd.Parameters.AddWithValue("@a", allergyName);
        try
        {
            return cmd.ExecuteNonQuery() > 0;
        }
        catch (SqlException ex) when (ex.Number == 2627)
        {
            return false;
        }
    }

    public bool RemoveAllergy(int userId, string allergyName)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "DELETE FROM [dbo].[PATIENT_ALLERGY] WHERE UserID = @id AND AllergyName = @a", cn);
        cmd.Parameters.AddWithValue("@id", userId);
        cmd.Parameters.AddWithValue("@a", allergyName);
        return cmd.ExecuteNonQuery() > 0;
    }

    private static IReadOnlyList<string> LoadAllergies(SqlConnection cn, int userId)
    {
        using var cmd = new SqlCommand(
            "SELECT AllergyName FROM [dbo].[PATIENT_ALLERGY] WHERE UserID = @id ORDER BY AllergyName", cn);
        cmd.Parameters.AddWithValue("@id", userId);
        using var r = cmd.ExecuteReader();
        var list = new List<string>();
        while (r.Read()) list.Add(r.GetString(0));
        return list;
    }
}
