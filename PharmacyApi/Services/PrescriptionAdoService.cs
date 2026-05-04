using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Models;

namespace PharmacyApi.Services;

public sealed class PrescriptionAdoService : IPrescriptionService
{
    private readonly ISqlConnectionFactory _db;

    public PrescriptionAdoService(ISqlConnectionFactory db) => _db = db;

    public PrescriptionDto? Get(int prescriptionId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            """
            SELECT PrescriptionID, IssueDate, Status, PatientID, PharmacistID
            FROM [dbo].[PRESCRIPTION]
            WHERE PrescriptionID=@id
            """, cn);
        cmd.Parameters.AddWithValue("@id", prescriptionId);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        var dto = new PrescriptionDto(
            r.GetInt32(0),
            r.GetDateTime(1),
            r.GetString(2),
            r.GetInt32(3),
            r.IsDBNull(4) ? null : r.GetInt32(4),
            Array.Empty<PrescriptionItemLineDto>());
        r.Close();
        return dto with { Items = LoadItems(cn, prescriptionId) };
    }

    public IReadOnlyList<PrescriptionDto> GetByPatient(int patientId) => LoadMany(
        "SELECT PrescriptionID, IssueDate, Status, PatientID, PharmacistID FROM [dbo].[PRESCRIPTION] WHERE PatientID=@pid ORDER BY IssueDate DESC",
        new SqlParameter("@pid", patientId));

    public IReadOnlyList<PrescriptionDto> GetAll() => LoadMany(
        "SELECT PrescriptionID, IssueDate, Status, PatientID, PharmacistID FROM [dbo].[PRESCRIPTION] ORDER BY IssueDate DESC");

    public int Create(CreatePrescriptionRequest request)
    {
        if (request.Items.Count == 0)
            throw new ArgumentException("Prescription must contain at least one item.");

        using var cn = _db.CreateConnection();
        cn.Open();
        using var tx = cn.BeginTransaction();
        try
        {
            const string ins = """
                INSERT INTO [dbo].[PRESCRIPTION] (IssueDate, Status, PatientID, PharmacistID)
                OUTPUT INSERTED.PrescriptionID
                VALUES (@d, @st, @pid, @phid)
                """;
            int id;
            using (var cmd = new SqlCommand(ins, cn, tx))
            {
                cmd.Parameters.AddWithValue("@d", request.IssueDate ?? DateTime.UtcNow);
                cmd.Parameters.AddWithValue("@st", string.IsNullOrWhiteSpace(request.Status) ? "Pending" : request.Status!);
                cmd.Parameters.AddWithValue("@pid", request.PatientId);
                cmd.Parameters.AddWithValue("@phid", (object?)request.PharmacistId ?? DBNull.Value);
                id = Convert.ToInt32(cmd.ExecuteScalar() ?? throw new InvalidOperationException("Insert failed."));
            }
            foreach (var line in request.Items)
            {
                using var pi = new SqlCommand(
                    """
                    INSERT INTO [dbo].[PRESCRIPTION_ITEM] (PrescriptionID, MedicineID, PrescribedQuantity)
                    VALUES (@pid, @mid, @q)
                    """, cn, tx);
                pi.Parameters.AddWithValue("@pid", id);
                pi.Parameters.AddWithValue("@mid", line.MedicineId);
                pi.Parameters.AddWithValue("@q", line.PrescribedQuantity);
                pi.ExecuteNonQuery();
            }
            tx.Commit();
            return id;
        }
        catch
        {
            tx.Rollback();
            throw;
        }
    }

    public bool Update(int prescriptionId, UpdatePrescriptionRequest request)
    {
        if (request.Status is null) return true;
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "UPDATE [dbo].[PRESCRIPTION] SET Status=@st WHERE PrescriptionID=@id", cn);
        cmd.Parameters.AddWithValue("@st", request.Status);
        cmd.Parameters.AddWithValue("@id", prescriptionId);
        return cmd.ExecuteNonQuery() > 0;
    }

    public bool Delete(int patientId, int prescriptionId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "DELETE FROM [dbo].[PRESCRIPTION] WHERE PrescriptionID=@id AND PatientID=@pid", cn);
        cmd.Parameters.AddWithValue("@id", prescriptionId);
        cmd.Parameters.AddWithValue("@pid", patientId);
        return cmd.ExecuteNonQuery() > 0;
    }

    private IReadOnlyList<PrescriptionDto> LoadMany(string sql, params SqlParameter[] parameters)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(sql, cn);
        foreach (var p in parameters) cmd.Parameters.Add(p);
        using var r = cmd.ExecuteReader();
        var list = new List<PrescriptionDto>();
        var ids = new List<int>();
        while (r.Read())
        {
            var id = r.GetInt32(0);
            ids.Add(id);
            list.Add(new PrescriptionDto(
                id,
                r.GetDateTime(1),
                r.GetString(2),
                r.GetInt32(3),
                r.IsDBNull(4) ? null : r.GetInt32(4),
                Array.Empty<PrescriptionItemLineDto>()));
        }
        r.Close();
        for (var i = 0; i < list.Count; i++)
            list[i] = list[i] with { Items = LoadItems(cn, ids[i]) };
        return list;
    }

    private static IReadOnlyList<PrescriptionItemLineDto> LoadItems(SqlConnection cn, int prescriptionId)
    {
        using var cmd = new SqlCommand(
            """
            SELECT pi.MedicineID, pi.PrescribedQuantity, m.Name
            FROM [dbo].[PRESCRIPTION_ITEM] pi
            INNER JOIN [dbo].[MEDICINE] m ON m.MedicineID = pi.MedicineID
            WHERE pi.PrescriptionID=@id
            ORDER BY pi.MedicineID
            """, cn);
        cmd.Parameters.AddWithValue("@id", prescriptionId);
        using var r = cmd.ExecuteReader();
        var list = new List<PrescriptionItemLineDto>();
        while (r.Read())
            list.Add(new PrescriptionItemLineDto(
                r.GetInt32(0),
                r.GetInt32(1),
                r.IsDBNull(2) ? null : r.GetString(2)));
        return list;
    }
}
