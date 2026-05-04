namespace PharmacyApi.Models;

public sealed record CreatePatientRequest(
    string Username,
    string Password,
    string? Email,
    string? Phone,
    IReadOnlyList<int> RoleIds,
    float Age,
    string? Address,
    double PatientBalance,
    IReadOnlyList<string>? Allergies);

public sealed record UpdatePatientRequest(
    string? Username,
    string? Password,
    string? Email,
    string? Phone,
    bool? IsActive,
    float? Age,
    string? Address,
    double? PatientBalance);
