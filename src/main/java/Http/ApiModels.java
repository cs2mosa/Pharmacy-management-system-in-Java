package Http;

import java.util.List;

/**
 * Gson-friendly DTOs mirroring the C# API JSON (camelCase).
 */
public final class ApiModels {
    private ApiModels() {
    }

    public static final class MLogin {
        public String username;
        public String password;
    }

    public static final class MRole {
        public int roleId;
        public String roleName;
        public int permissionsLevel;
        public String description;
    }

    public static final class MUser {
        public int userId;
        public String username;
        public String email;
        public String phone;
        public boolean isActive;
        public String userKind;
        public List<Integer> roleIds;
    }

    public static final class MPatient {
        public int userId;
        public String username;
        public String email;
        public String phone;
        public boolean isActive;
        public float age;
        public String address;
        public double patientBalance;
        public List<String> allergies;
    }

    public static final class MMedicine {
        public int medicineId;
        public String name;
        public double price;
        public String category;
        public String expiryDate;
        public int stockQuantity;
        public String usageInstructions;
        public boolean isRefundable;
        public List<String> sideEffects;
        public List<String> healingEffects;
    }

    public static final class MOrderItem {
        public int medicineId;
        public int quantity;
        public double unitPrice;
        public String medicineName;
    }

    public static final class MOrder {
        public int orderId;
        public String orderDate;
        public double totalPrice;
        public String status;
        public int patientId;
        public Integer cashierId;
        public List<MOrderItem> items;
    }

    public static final class MPrescriptionItem {
        public int medicineId;
        public int prescribedQuantity;
        public String medicineName;
    }

    public static final class MPrescription {
        public int prescriptionId;
        public String issueDate;
        public String status;
        public int patientId;
        public Integer pharmacistId;
        public List<MPrescriptionItem> items;
    }

    public static final class MPayment {
        public int paymentId;
        public double amount;
        public String paymentDate;
        public String paymentMethod;
        public String status;
        public int orderId;
    }
}
