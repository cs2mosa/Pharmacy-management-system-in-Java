using System.Text.Json;
using System.Text.Json.Serialization;
using Microsoft.Data.SqlClient;
using PharmacyApi.Infrastructure;
using PharmacyApi.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.ConfigureHttpJsonOptions(o =>
{
    o.SerializerOptions.PropertyNamingPolicy = JsonNamingPolicy.CamelCase;
    o.SerializerOptions.DefaultIgnoreCondition = JsonIgnoreCondition.WhenWritingNull;
    o.SerializerOptions.Converters.Add(new JsonStringEnumConverter());
});

builder.Services.AddSingleton<ISqlConnectionFactory>(_ =>
    new SqlConnectionFactory(builder.Configuration.GetConnectionString("PharmacyDb")
        ?? throw new InvalidOperationException("Connection string 'PharmacyDb' is not configured.")));

builder.Services.AddScoped<IUserService, UserAdoService>();
builder.Services.AddScoped<IRoleService, RoleAdoService>();
builder.Services.AddScoped<IPatientService, PatientAdoService>();
builder.Services.AddScoped<IMedicineService, MedicineAdoService>();
builder.Services.AddScoped<IOrderService, OrderAdoService>();
builder.Services.AddScoped<IPrescriptionService, PrescriptionAdoService>();
builder.Services.AddScoped<IPaymentService, PaymentAdoService>();
builder.Services.AddScoped<IEmployeeService, EmployeeAdoService>();

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();
builder.Services.AddCors(o =>
{
    o.AddDefaultPolicy(p => p.AllowAnyOrigin().AllowAnyHeader().AllowAnyMethod());
});

var app = builder.Build();
app.UseCors();
app.UseSwagger();
app.UseSwaggerUI();
app.MapControllers();
app.Run();
