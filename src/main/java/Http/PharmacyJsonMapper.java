package Http;

import Class_model.*;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public final class PharmacyJsonMapper {
    private static final DateTimeFormatter OUT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PharmacyJsonMapper() {
    }

    public static Set<Role> rolesFromIds(Gson gson, BaseService http, List<Integer> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return new HashSet<>();
        }
        Type t = BaseService.listOf(ApiModels.MRole.class);
        List<ApiModels.MRole> all = (List<ApiModels.MRole>) http.getJson("/api/roles", t).orElse(List.of());
        var byId = all.stream().collect(Collectors.toMap(r -> r.roleId, r -> r));
        var set = new HashSet<Role>();
        for (int id : roleIds) {
            var r = byId.get(id);
            if (r != null) {
                set.add(new Role(String.valueOf(r.roleId), r.roleName, r.permissionsLevel, r.description));
            }
        }
        return set;
    }

    public static int roleIdByName(BaseService http, Gson gson, String roleName) {
        Type t = BaseService.listOf(ApiModels.MRole.class);
        List<ApiModels.MRole> all = (List<ApiModels.MRole>) http.getJson("/api/roles", t).orElse(List.of());
        for (var r : all) {
            if (roleName.equalsIgnoreCase(r.roleName)) {
                return r.roleId;
            }
        }
        return -1;
    }

    public static Item toItem(ApiModels.MMedicine m) {
        var b = new Item.builder()
                .setMedicineId(m.medicineId)
                .setMedicName(m.name)
                .setPrice(m.price)
                .setCategory(m.category != null ? m.category : "")
                .setExpireDate(formatExpiry(m.expiryDate))
                .setQuantity(m.stockQuantity)
                .setUsage(m.usageInstructions != null ? m.usageInstructions : "")
                .set_Refundable(m.isRefundable);
        if (m.sideEffects != null) {
            b.setSideEffects(new HashSet<>(m.sideEffects));
        }
        if (m.healingEffects != null) {
            b.setHealingEffects(new HashSet<>(m.healingEffects));
        }
        return b.build();
    }

    public static Patient toPatient(Gson gson, BaseService http, ApiModels.MPatient p) {
        Set<Role> roles = new HashSet<>();
        var u = http.getJson("/api/users/" + p.userId, ApiModels.MUser.class);
        if (u.isPresent() && u.get().roleIds != null) {
            roles = rolesFromIds(gson, http, u.get().roleIds);
        }
        Set<String> allergies = p.allergies == null ? new HashSet<>() : new HashSet<>(p.allergies);
        return new Patient(
                p.userId,
                p.username,
                "",
                p.email != null ? p.email : "",
                p.phone != null ? p.phone : "",
                p.age,
                p.address != null ? p.address : "",
                allergies,
                new ArrayList<>(),
                roles);
    }

    public static Order toOrder(ApiModels.MOrder o) {
        List<Item> items = new ArrayList<>();
        if (o.items != null) {
            for (var li : o.items) {
                String name = li.medicineName != null ? li.medicineName : ("#" + li.medicineId);
                var it = new Item.builder()
                        .setMedicineId(li.medicineId)
                        .setMedicName(name)
                        .setPrice(li.unitPrice)
                        .setQuantity(li.quantity)
                        .setExpireDate("")
                        .setCategory("")
                        .setUsage("")
                        .set_Refundable(true)
                        .build();
                items.add(it);
            }
        }
        var order = new Order.builder()
                .setOrderId(o.orderId)
                .setOrderDate(o.orderDate != null ? o.orderDate : "")
                .setCheckedBy(o.cashierId != null ? ("User#" + o.cashierId) : "")
                .setOrderItems(items)
                .setTotalPrice(o.totalPrice)
                .setStatus(o.status != null ? o.status : "Pending")
                .build();
        return order;
    }

    public static Prescription toPrescription(ApiModels.MPrescription p, Pharmacist pharmacist, String patientUsername) {
        Set<Item> items = new HashSet<>();
        if (p.items != null) {
            for (var li : p.items) {
                String name = li.medicineName != null ? li.medicineName : ("#" + li.medicineId);
                var it = new Item.builder()
                        .setMedicineId(li.medicineId)
                        .setMedicName(name)
                        .setPrice(0)
                        .setQuantity(li.prescribedQuantity)
                        .setExpireDate("")
                        .setCategory("")
                        .setUsage("")
                        .set_Refundable(true)
                        .build();
                items.add(it);
            }
        }
        String patientName = patientUsername != null && !patientUsername.isBlank()
                ? patientUsername
                : ("Patient#" + p.patientId);
        Prescription pr = new Prescription(p.prescriptionId, patientName, pharmacist, items);
        if (p.status != null) {
            pr.setStatus(p.status);
        }
        return pr;
    }

    public static User toUser(Gson gson, BaseService http, ApiModels.MUser m, String passwordPlaceholder) {
        Set<Role> roles = rolesFromIds(gson, http, m.roleIds != null ? m.roleIds : List.of());
        String kind = m.userKind != null ? m.userKind : "";
        if ("Pharmacist".equalsIgnoreCase(kind)) {
            return new Pharmacist(m.userId, m.username, passwordPlaceholder,
                    m.email != null ? m.email : "",
                    m.phone != null ? m.phone : "",
                    roles);
        }
        if ("Casher".equalsIgnoreCase(kind) || "Cashier".equalsIgnoreCase(kind)) {
            return new Casher(m.userId, m.username, passwordPlaceholder,
                    m.email != null ? m.email : "",
                    m.phone != null ? m.phone : "",
                    0.0,
                    roles);
        }
        if ("Patient".equalsIgnoreCase(kind)) {
            return new Patient(m.userId, m.username, passwordPlaceholder,
                    m.email != null ? m.email : "",
                    m.phone != null ? m.phone : "",
                    0f, "", new HashSet<>(), new ArrayList<>(), roles);
        }
        return new Pharmacist(m.userId, m.username, passwordPlaceholder,
                m.email != null ? m.email : "",
                m.phone != null ? m.phone : "",
                roles);
    }

    private static String formatExpiry(String isoOrDate) {
        if (isoOrDate == null || isoOrDate.isBlank()) {
            return "";
        }
        try {
            var dt = OffsetDateTime.parse(isoOrDate);
            return dt.format(OUT);
        } catch (DateTimeParseException ignored) {
        }
        try {
            var d = java.time.LocalDate.parse(isoOrDate);
            return d.format(OUT);
        } catch (DateTimeParseException ignored) {
        }
        return isoOrDate;
    }
}
