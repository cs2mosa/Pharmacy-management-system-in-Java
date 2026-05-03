using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Models;

namespace PharmacyApi.Services;

public sealed class MedicineAdoService : IMedicineService
{
    private readonly ISqlConnectionFactory _db;

    public MedicineAdoService(ISqlConnectionFactory db) => _db = db;

    public MedicineDto? Get(int medicineId) => LoadOne(
        "SELECT MedicineID, Name, Price, Category, ExpiryDate, StockQuantity, UsageInstructions, IsRefundable FROM [dbo].[MEDICINE] WHERE MedicineID = @id",
        new SqlParameter("@id", medicineId));

    public MedicineDto? GetByName(string name) => LoadOne(
        "SELECT MedicineID, Name, Price, Category, ExpiryDate, StockQuantity, UsageInstructions, IsRefundable FROM [dbo].[MEDICINE] WHERE Name = @n",
        new SqlParameter("@n", name));

    public IReadOnlyList<MedicineDto> GetAll() => LoadMany(
        "SELECT MedicineID, Name, Price, Category, ExpiryDate, StockQuantity, UsageInstructions, IsRefundable FROM [dbo].[MEDICINE] ORDER BY Name");

    public IReadOnlyList<MedicineDto> GetByCategory(string category) => LoadMany(
        "SELECT MedicineID, Name, Price, Category, ExpiryDate, StockQuantity, UsageInstructions, IsRefundable FROM [dbo].[MEDICINE] WHERE Category = @c ORDER BY Name",
        new SqlParameter("@c", category));

    public int Create(UpsertMedicineRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var tx = cn.BeginTransaction();
        try
        {
            const string ins = """
                INSERT INTO [dbo].[MEDICINE] (Name, Price, Category, ExpiryDate, StockQuantity, UsageInstructions, IsRefundable)
                OUTPUT INSERTED.MedicineID
                VALUES (@n, @p, @cat, @ex, @sq, @us, @ir)
                """;
            int id;
            using (var cmd = new SqlCommand(ins, cn, tx))
            {
                cmd.Parameters.AddWithValue("@n", request.Name);
                cmd.Parameters.AddWithValue("@p", request.Price);
                cmd.Parameters.AddWithValue("@cat", (object?)request.Category ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@ex", request.ExpiryDate.Date);
                cmd.Parameters.AddWithValue("@sq", request.StockQuantity);
                cmd.Parameters.AddWithValue("@us", (object?)request.UsageInstructions ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@ir", request.IsRefundable);
                id = Convert.ToInt32(cmd.ExecuteScalar() ?? throw new InvalidOperationException("Insert failed."));
            }
            ReplaceEffects(cn, tx, id, request.SideEffects, request.HealingEffects);
            tx.Commit();
            return id;
        }
        catch
        {
            tx.Rollback();
            throw;
        }
    }

    public bool Update(int medicineId, UpsertMedicineRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var tx = cn.BeginTransaction();
        try
        {
            using (var cmd = new SqlCommand(
                       """
                       UPDATE [dbo].[MEDICINE]
                       SET Name=@n, Price=@p, Category=@cat, ExpiryDate=@ex, StockQuantity=@sq, UsageInstructions=@us, IsRefundable=@ir
                       WHERE MedicineID=@id
                       """, cn, tx))
            {
                cmd.Parameters.AddWithValue("@id", medicineId);
                cmd.Parameters.AddWithValue("@n", request.Name);
                cmd.Parameters.AddWithValue("@p", request.Price);
                cmd.Parameters.AddWithValue("@cat", (object?)request.Category ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@ex", request.ExpiryDate.Date);
                cmd.Parameters.AddWithValue("@sq", request.StockQuantity);
                cmd.Parameters.AddWithValue("@us", (object?)request.UsageInstructions ?? DBNull.Value);
                cmd.Parameters.AddWithValue("@ir", request.IsRefundable);
                if (cmd.ExecuteNonQuery() == 0)
                {
                    tx.Rollback();
                    return false;
                }
            }
            using (var del = new SqlCommand(
                       "DELETE FROM [dbo].[MED_SIDE_EFFECT] WHERE MedicineID=@id; DELETE FROM [dbo].[MED_HEALING_EFFECT] WHERE MedicineID=@id;",
                       cn, tx))
            {
                del.Parameters.AddWithValue("@id", medicineId);
                del.ExecuteNonQuery();
            }
            ReplaceEffects(cn, tx, medicineId, request.SideEffects, request.HealingEffects);
            tx.Commit();
            return true;
        }
        catch
        {
            tx.Rollback();
            throw;
        }
    }

    public bool Delete(int medicineId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand("DELETE FROM [dbo].[MEDICINE] WHERE MedicineID=@id", cn);
        cmd.Parameters.AddWithValue("@id", medicineId);
        return cmd.ExecuteNonQuery() > 0;
    }

    public bool SetStock(int medicineId, int newQuantity)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "UPDATE [dbo].[MEDICINE] SET StockQuantity=@q WHERE MedicineID=@id", cn);
        cmd.Parameters.AddWithValue("@q", newQuantity);
        cmd.Parameters.AddWithValue("@id", medicineId);
        return cmd.ExecuteNonQuery() > 0;
    }

    private MedicineDto? LoadOne(string sql, params SqlParameter[] parameters)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(sql, cn);
        foreach (var p in parameters)
            cmd.Parameters.Add(p);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        var dto = MapRow(r, cn);
        return dto;
    }

    private IReadOnlyList<MedicineDto> LoadMany(string sql, params SqlParameter[] parameters)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(sql, cn);
        foreach (var p in parameters)
            cmd.Parameters.Add(p);
        using var r = cmd.ExecuteReader();
        var list = new List<MedicineDto>();
        var ids = new List<int>();
        while (r.Read())
        {
            ids.Add(r.GetInt32(0));
            list.Add(new MedicineDto(
                r.GetInt32(0),
                r.GetString(1),
                r.GetDecimal(2),
                r.IsDBNull(3) ? null : r.GetString(3),
                r.GetDateTime(4),
                r.GetInt32(5),
                r.IsDBNull(6) ? null : r.GetString(6),
                !r.IsDBNull(7) && r.GetBoolean(7),
                Array.Empty<string>(),
                Array.Empty<string>()));
        }
        r.Close();
        for (var i = 0; i < list.Count; i++)
        {
            list[i] = list[i] with
            {
                SideEffects = LoadSide(cn, ids[i]),
                HealingEffects = LoadHeal(cn, ids[i])
            };
        }
        return list;
    }

    private MedicineDto MapRow(SqlDataReader r, SqlConnection cn)
    {
        var id = r.GetInt32(0);
        var dto = new MedicineDto(
            id,
            r.GetString(1),
            r.GetDecimal(2),
            r.IsDBNull(3) ? null : r.GetString(3),
            r.GetDateTime(4),
            r.GetInt32(5),
            r.IsDBNull(6) ? null : r.GetString(6),
            !r.IsDBNull(7) && r.GetBoolean(7),
            Array.Empty<string>(),
            Array.Empty<string>());
        r.Close();
        return dto with { SideEffects = LoadSide(cn, id), HealingEffects = LoadHeal(cn, id) };
    }

    private static IReadOnlyList<string> LoadSide(SqlConnection cn, int medicineId)
    {
        using var cmd = new SqlCommand(
            "SELECT SideEffectName FROM [dbo].[MED_SIDE_EFFECT] WHERE MedicineID=@id ORDER BY SideEffectName", cn);
        cmd.Parameters.AddWithValue("@id", medicineId);
        using var r = cmd.ExecuteReader();
        var list = new List<string>();
        while (r.Read()) list.Add(r.GetString(0));
        return list;
    }

    private static IReadOnlyList<string> LoadHeal(SqlConnection cn, int medicineId)
    {
        using var cmd = new SqlCommand(
            "SELECT HealingEffectName FROM [dbo].[MED_HEALING_EFFECT] WHERE MedicineID=@id ORDER BY HealingEffectName", cn);
        cmd.Parameters.AddWithValue("@id", medicineId);
        using var r = cmd.ExecuteReader();
        var list = new List<string>();
        while (r.Read()) list.Add(r.GetString(0));
        return list;
    }

    private static void ReplaceEffects(SqlConnection cn, SqlTransaction tx, int medicineId,
        IReadOnlyList<string>? sides, IReadOnlyList<string>? heals)
    {
        if (sides is not null)
        {
            foreach (var s in sides.Where(x => !string.IsNullOrWhiteSpace(x)).Distinct(StringComparer.OrdinalIgnoreCase))
            {
                using var cmd = new SqlCommand(
                    "INSERT INTO [dbo].[MED_SIDE_EFFECT] (MedicineID, SideEffectName) VALUES (@id, @n)", cn, tx);
                cmd.Parameters.AddWithValue("@id", medicineId);
                cmd.Parameters.AddWithValue("@n", s.Trim());
                cmd.ExecuteNonQuery();
            }
        }
        if (heals is not null)
        {
            foreach (var h in heals.Where(x => !string.IsNullOrWhiteSpace(x)).Distinct(StringComparer.OrdinalIgnoreCase))
            {
                using var cmd = new SqlCommand(
                    "INSERT INTO [dbo].[MED_HEALING_EFFECT] (MedicineID, HealingEffectName) VALUES (@id, @n)", cn, tx);
                cmd.Parameters.AddWithValue("@id", medicineId);
                cmd.Parameters.AddWithValue("@n", h.Trim());
                cmd.ExecuteNonQuery();
            }
        }
    }
}
