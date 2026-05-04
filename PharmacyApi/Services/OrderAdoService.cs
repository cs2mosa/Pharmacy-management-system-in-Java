using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Models;

namespace PharmacyApi.Services;

public sealed class OrderAdoService : IOrderService
{
    private readonly ISqlConnectionFactory _db;

    public OrderAdoService(ISqlConnectionFactory db) => _db = db;

    public OrderDto? Get(int orderId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        const string sql = """
            SELECT OrderID, OrderDate, TotalPrice, Status, PatientID, CashierID
            FROM [dbo].[ORDER]
            WHERE OrderID = @id
            """;
        using var cmd = new SqlCommand(sql, cn);
        cmd.Parameters.AddWithValue("@id", orderId);
        using var r = cmd.ExecuteReader();
        if (!r.Read()) return null;
        var dto = new OrderDto(
            r.GetInt32(0),
            r.GetDateTime(1),
            r.GetDecimal(2),
            r.GetString(3),
            r.GetInt32(4),
            r.IsDBNull(5) ? null : r.GetInt32(5),
            Array.Empty<OrderItemLineDto>());
        r.Close();
        return dto with { Items = LoadItems(cn, orderId) };
    }

    public IReadOnlyList<OrderDto> GetByPatient(int patientId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            """
            SELECT OrderID, OrderDate, TotalPrice, Status, PatientID, CashierID
            FROM [dbo].[ORDER]
            WHERE PatientID = @pid
            ORDER BY OrderDate DESC
            """, cn);
        cmd.Parameters.AddWithValue("@pid", patientId);
        return ReadOrderList(cn, cmd);
    }

    public IReadOnlyList<OrderDto> GetHistory()
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            """
            SELECT OrderID, OrderDate, TotalPrice, Status, PatientID, CashierID
            FROM [dbo].[ORDER]
            ORDER BY OrderDate DESC
            """, cn);
        return ReadOrderList(cn, cmd);
    }

    public int Create(CreateOrderRequest request)
    {
        if (request.Items.Count == 0)
            throw new ArgumentException("Order must contain at least one line item.");

        using var cn = _db.CreateConnection();
        cn.Open();
        using var tx = cn.BeginTransaction();
        try
        {
            decimal total = 0;
            foreach (var line in request.Items)
                total += line.UnitPrice * line.Quantity;

            const string insOrder = """
                INSERT INTO [dbo].[ORDER] (OrderDate, TotalPrice, Status, PatientID, CashierID)
                OUTPUT INSERTED.OrderID
                VALUES (@od, @tp, @st, @pid, @cid)
                """;
            int orderId;
            using (var cmd = new SqlCommand(insOrder, cn, tx))
            {
                cmd.Parameters.AddWithValue("@od", request.OrderDate ?? DateTime.UtcNow);
                cmd.Parameters.AddWithValue("@tp", total);
                cmd.Parameters.AddWithValue("@st", string.IsNullOrWhiteSpace(request.Status) ? "Pending" : request.Status!);
                cmd.Parameters.AddWithValue("@pid", request.PatientId);
                cmd.Parameters.AddWithValue("@cid", (object?)request.CashierId ?? DBNull.Value);
                orderId = Convert.ToInt32(cmd.ExecuteScalar() ?? throw new InvalidOperationException("Order insert failed."));
            }

            foreach (var line in request.Items)
            {
                using var stockCmd = new SqlCommand(
                    "SELECT StockQuantity FROM [dbo].[MEDICINE] WHERE MedicineID=@mid", cn, tx);
                stockCmd.Parameters.AddWithValue("@mid", line.MedicineId);
                using var sr = stockCmd.ExecuteReader();
                if (!sr.Read())
                {
                    sr.Close();
                    throw new InvalidOperationException($"Medicine {line.MedicineId} not found.");
                }
                var stock = sr.GetInt32(0);
                sr.Close();
                if (stock < line.Quantity)
                    throw new InvalidOperationException($"Insufficient stock for medicine {line.MedicineId}.");

                using var oi = new SqlCommand(
                    """
                    INSERT INTO [dbo].[ORDER_ITEM] (OrderID, MedicineID, Quantity, UnitPrice)
                    VALUES (@oid, @mid, @q, @up)
                    """, cn, tx);
                oi.Parameters.AddWithValue("@oid", orderId);
                oi.Parameters.AddWithValue("@mid", line.MedicineId);
                oi.Parameters.AddWithValue("@q", line.Quantity);
                oi.Parameters.AddWithValue("@up", line.UnitPrice);
                oi.ExecuteNonQuery();

                using var up = new SqlCommand(
                    "UPDATE [dbo].[MEDICINE] SET StockQuantity = StockQuantity - @q WHERE MedicineID=@mid", cn, tx);
                up.Parameters.AddWithValue("@q", line.Quantity);
                up.Parameters.AddWithValue("@mid", line.MedicineId);
                up.ExecuteNonQuery();
            }

            tx.Commit();
            return orderId;
        }
        catch
        {
            tx.Rollback();
            throw;
        }
    }

    public bool Update(int orderId, UpdateOrderRequest request)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        var sets = new List<string>();
        using var cmd = new SqlCommand { Connection = cn };
        cmd.Parameters.AddWithValue("@id", orderId);
        if (request.Status is { } st)
        {
            sets.Add("Status = @st");
            cmd.Parameters.AddWithValue("@st", st);
        }
        if (request.CashierId is { } cid)
        {
            sets.Add("CashierID = @cid");
            cmd.Parameters.AddWithValue("@cid", cid);
        }
        if (request.TotalPrice is { } tp)
        {
            sets.Add("TotalPrice = @tp");
            cmd.Parameters.AddWithValue("@tp", tp);
        }
        if (sets.Count == 0) return true;
        cmd.CommandText = $"UPDATE [dbo].[ORDER] SET {string.Join(", ", sets)} WHERE OrderID=@id";
        return cmd.ExecuteNonQuery() > 0;
    }

    public bool Delete(int patientId, int orderId)
    {
        using var cn = _db.CreateConnection();
        cn.Open();
        using var cmd = new SqlCommand(
            "DELETE FROM [dbo].[ORDER] WHERE OrderID=@oid AND PatientID=@pid", cn);
        cmd.Parameters.AddWithValue("@oid", orderId);
        cmd.Parameters.AddWithValue("@pid", patientId);
        return cmd.ExecuteNonQuery() > 0;
    }

    private static IReadOnlyList<OrderDto> ReadOrderList(SqlConnection cn, SqlCommand cmd)
    {
        using var r = cmd.ExecuteReader();
        var heads = new List<OrderDto>();
        var ids = new List<int>();
        while (r.Read())
        {
            var id = r.GetInt32(0);
            ids.Add(id);
            heads.Add(new OrderDto(
                id,
                r.GetDateTime(1),
                r.GetDecimal(2),
                r.GetString(3),
                r.GetInt32(4),
                r.IsDBNull(5) ? null : r.GetInt32(5),
                Array.Empty<OrderItemLineDto>()));
        }
        r.Close();
        for (var i = 0; i < heads.Count; i++)
            heads[i] = heads[i] with { Items = LoadItems(cn, ids[i]) };
        return heads;
    }

    private static IReadOnlyList<OrderItemLineDto> LoadItems(SqlConnection cn, int orderId)
    {
        using var cmd = new SqlCommand(
            """
            SELECT oi.MedicineID, oi.Quantity, oi.UnitPrice, m.Name
            FROM [dbo].[ORDER_ITEM] oi
            INNER JOIN [dbo].[MEDICINE] m ON m.MedicineID = oi.MedicineID
            WHERE oi.OrderID=@id
            ORDER BY oi.MedicineID
            """, cn);
        cmd.Parameters.AddWithValue("@id", orderId);
        using var r = cmd.ExecuteReader();
        var list = new List<OrderItemLineDto>();
        while (r.Read())
            list.Add(new OrderItemLineDto(
                r.GetInt32(0),
                r.GetInt32(1),
                r.GetDecimal(2),
                r.IsDBNull(3) ? null : r.GetString(3)));
        return list;
    }
}
