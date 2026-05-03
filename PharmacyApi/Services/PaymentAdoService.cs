using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Models;

namespace PharmacyApi.Services;

public sealed class PaymentAdoService : IPaymentService
{
    private readonly ISqlConnectionFactory _db;

    public PaymentAdoService(ISqlConnectionFactory db) => _db = db;

    public PaymentDto? Get(int paymentId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            """
            SELECT p.PaymentID, p.Amount, p.PaymentDate, p.PaymentMethod, p.Status, p.OrderID
            FROM [dbo].[PAYMENT] p
            WHERE p.PaymentID=@id
            """, cn);
        cmd.Parameters.AddWithValue("@id", paymentId);
        using var r = cmd.ExecuteReader();
        return r.Read() ? Map(r) : null;
    }

    public IReadOnlyList<PaymentDto> GetByPatient(int patientId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            """
            SELECT p.PaymentID, p.Amount, p.PaymentDate, p.PaymentMethod, p.Status, p.OrderID
            FROM [dbo].[PAYMENT] p
            INNER JOIN [dbo].[ORDER] o ON o.OrderID = p.OrderID
            WHERE o.PatientID=@pid
            ORDER BY p.PaymentDate DESC
            """, cn);
        cmd.Parameters.AddWithValue("@pid", patientId);
        return ReadList(cmd);
    }

    public IReadOnlyList<PaymentDto> GetAll()
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            """
            SELECT PaymentID, Amount, PaymentDate, PaymentMethod, Status, OrderID
            FROM [dbo].[PAYMENT]
            ORDER BY PaymentDate DESC
            """, cn);
        return ReadList(cmd);
    }

    public int Create(CreatePaymentRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        const string sql = """
            INSERT INTO [dbo].[PAYMENT] (Amount, PaymentDate, PaymentMethod, Status, OrderID)
            OUTPUT INSERTED.PaymentID
            VALUES (@a, @d, @m, @st, @oid)
            """;
        using var cmd = new SqlCommand(sql, cn);
        cmd.Parameters.AddWithValue("@a", request.Amount);
        cmd.Parameters.AddWithValue("@d", request.PaymentDate ?? DateTime.UtcNow);
        cmd.Parameters.AddWithValue("@m", (object?)request.PaymentMethod ?? DBNull.Value);
        cmd.Parameters.AddWithValue("@st", string.IsNullOrWhiteSpace(request.Status) ? "Pending" : request.Status!);
        cmd.Parameters.AddWithValue("@oid", request.OrderId);
        return Convert.ToInt32(cmd.ExecuteScalar() ?? throw new InvalidOperationException("Payment insert failed."));
    }

    public bool Update(int paymentId, UpdatePaymentRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        var sets = new List<string>();
        using var cmd = new SqlCommand { Connection = cn };
        cmd.Parameters.AddWithValue("@id", paymentId);
        if (request.Amount is { } a)
        {
            sets.Add("Amount=@a");
            cmd.Parameters.AddWithValue("@a", a);
        }
        if (request.PaymentDate is { } d)
        {
            sets.Add("PaymentDate=@d");
            cmd.Parameters.AddWithValue("@d", d);
        }
        if (request.PaymentMethod is { } m)
        {
            sets.Add("PaymentMethod=@m");
            cmd.Parameters.AddWithValue("@m", m);
        }
        if (request.Status is { } s)
        {
            sets.Add("Status=@s");
            cmd.Parameters.AddWithValue("@s", s);
        }
        if (sets.Count == 0) return true;
        cmd.CommandText = $"UPDATE [dbo].[PAYMENT] SET {string.Join(", ", sets)} WHERE PaymentID=@id";
        return cmd.ExecuteNonQuery() > 0;
    }

    public bool Delete(int paymentId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand("DELETE FROM [dbo].[PAYMENT] WHERE PaymentID=@id", cn);
        cmd.Parameters.AddWithValue("@id", paymentId);
        return cmd.ExecuteNonQuery() > 0;
    }

    private static IReadOnlyList<PaymentDto> ReadList(SqlCommand cmd)
    {
        using var r = cmd.ExecuteReader();
        var list = new List<PaymentDto>();
        while (r.Read()) list.Add(Map(r));
        return list;
    }

    private static PaymentDto Map(SqlDataReader r) => new(
        r.GetInt32(0),
        r.GetDecimal(1),
        r.GetDateTime(2),
        r.IsDBNull(3) ? null : r.GetString(3),
        r.GetString(4),
        r.GetInt32(5));
}
