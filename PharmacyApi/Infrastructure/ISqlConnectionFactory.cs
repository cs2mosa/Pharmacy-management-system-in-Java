using Microsoft.Data.SqlClient;

namespace PharmacyApi.Infrastructure;

public interface ISqlConnectionFactory
{
    SqlConnection CreateConnection();
}
