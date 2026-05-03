namespace PharmacyApi.Models;

public sealed record RoleDto(int RoleId, string RoleName, int PermissionsLevel, string? Description);

public sealed record UserDto(
    int UserId,
    string Username,
    string? Email,
    string? Phone,
    bool IsActive,
    string UserKind,
    IReadOnlyList<int> RoleIds);

public sealed record LoginRequest(string Username, string Password);

public sealed record CreateUserRequest(
    string Username,
    string Password,
    string? Email,
    string? Phone,
    string UserKind,
    IReadOnlyList<int> RoleIds,
    float? Age,
    string? Address,
    double? PatientBalance,
    double? Salary,
    string? JobType);

public sealed record UpdateUserRequest(
    string? Username,
    string? Password,
    string? Email,
    string? Phone,
    bool? IsActive);

public sealed record PatientAllergyDto(string AllergyName);

public sealed record PatientDto(
    int UserId,
    string Username,
    string? Email,
    string? Phone,
    bool IsActive,
    float Age,
    string? Address,
    double PatientBalance,
    IReadOnlyList<string> Allergies);

public sealed record MedicineDto(
    int MedicineId,
    string Name,
    decimal Price,
    string? Category,
    DateTime ExpiryDate,
    int StockQuantity,
    string? UsageInstructions,
    bool IsRefundable,
    IReadOnlyList<string> SideEffects,
    IReadOnlyList<string> HealingEffects);

public sealed record UpsertMedicineRequest(
    string Name,
    decimal Price,
    string? Category,
    DateTime ExpiryDate,
    int StockQuantity,
    string? UsageInstructions,
    bool IsRefundable,
    IReadOnlyList<string>? SideEffects,
    IReadOnlyList<string>? HealingEffects);

public sealed record OrderItemLineDto(int MedicineId, int Quantity, decimal UnitPrice, string? MedicineName);

public sealed record OrderDto(
    int OrderId,
    DateTime OrderDate,
    decimal TotalPrice,
    string Status,
    int PatientId,
    int? CashierId,
    IReadOnlyList<OrderItemLineDto> Items);

public sealed record CreateOrderRequest(
    int PatientId,
    int? CashierId,
    string? Status,
    DateTime? OrderDate,
    IReadOnlyList<OrderItemLineDto> Items);

public sealed record UpdateOrderRequest(string? Status, int? CashierId, decimal? TotalPrice);

public sealed record PrescriptionItemLineDto(int MedicineId, int PrescribedQuantity, string? MedicineName);

public sealed record PrescriptionDto(
    int PrescriptionId,
    DateTime IssueDate,
    string Status,
    int PatientId,
    int? PharmacistId,
    IReadOnlyList<PrescriptionItemLineDto> Items);

public sealed record CreatePrescriptionRequest(
    int PatientId,
    int? PharmacistId,
    string? Status,
    DateTime? IssueDate,
    IReadOnlyList<PrescriptionItemLineDto> Items);

public sealed record UpdatePrescriptionRequest(string? Status);

public sealed record PaymentDto(
    int PaymentId,
    decimal Amount,
    DateTime PaymentDate,
    string? PaymentMethod,
    string Status,
    int OrderId);

public sealed record CreatePaymentRequest(
    int OrderId,
    decimal Amount,
    DateTime? PaymentDate,
    string? PaymentMethod,
    string? Status);

public sealed record UpdatePaymentRequest(
    decimal? Amount,
    DateTime? PaymentDate,
    string? PaymentMethod,
    string? Status);
