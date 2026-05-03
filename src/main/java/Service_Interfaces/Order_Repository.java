package Service_Interfaces;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import Class_model.Order;

import Http.ApiModels;
import Http.BaseService;
import Http.PharmacyJsonMapper;
/**
 * orders memory interface should be a queue.
 * The OrderRepository interface defines the contract for managing orders in the system.
 * It provides methods for adding, deleting, updating, and retrieving orders, as well as
 * accessing order history.
 * when creating Orders, User should be infromed with quantity of Items, and preveted from adding a non existed Item.
 */
abstract interface OrderRepository {

    /**
     * Adds a new order to the repository.
     * @param order The order to be added.
     * @return order id on success , -1 else.
     * @throws IllegalArgumentException if the order is null or invalid.
     */
    int AddOrder(int patientId, Order order) throws IllegalArgumentException; // Adds an order to the repository.

    /**
     * Deletes an existing order from the repository.
     * @param orderId The ID of the order to be deleted.
     * @param patientId The ID of the patient associated with the order.
     * @return 0 on success, -1 if order not found.
     * @throws IllegalArgumentException if the order ID is invalid or the patient ID is not found.
     */
    int DeleteOrder(int patientId,int orderId) throws IllegalArgumentException; // Removes an order from the repository.

    /**
     * Updates an existing order in the repository.
     * @param Neworder The field or property to be updated.
     * @return 0 on success, -1 if order not found.
     * @throws IllegalArgumentException if the order ID is invalid or the new order is null.
     */
    int UpdateOrder(int patientId, Order Neworder) throws IllegalArgumentException; // Updates an order's details.

    /**
     * Orders for a patient from persistent storage.
     */
    List<Order> GetOrdersForPatient(int patientId);

    /**
     * Retrieves a list of orders by the patient's name.
     * @param PatientName The name of the patient associated with the orders.
     * @return A list of orders matching the given patient name.
     * @throws IllegalArgumentException if the patient name is invalid or not found.
     */
    List<Order> GetByName(String PatientName) throws IllegalArgumentException; // Fetches orders by patient name.

    /**
     * Retrieves an order by its unique ID.
     * @param orderId The ID of the order to retrieve.
     * @return The order with the specified ID or null if not found.
     * @throws IllegalArgumentException if the order ID is invalid or not found.
     */
    Order GetById(int orderId) throws IllegalArgumentException; // Fetches an order by its ID.

    /**
     * Retrieves the history of all orders.
     * @return A list of all past orders.
     */
    List<Order> GetHistory(); // Retrieves the order history.
}
class Order_Repository extends BaseService implements OrderRepository{
    private static Order_Repository instance = null;

    private Order_Repository() {
    }

    public static Order_Repository getInstance() {
        if (instance == null) {
            instance = new Order_Repository();
        }
        return instance;
    }

    @Override
    public int AddOrder(int patientId, Order order) throws IllegalArgumentException{
        if(Patient_Repository.getInstance().GetPatient(patientId) == null) {
            throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
        }
        if(order == null ||!(order instanceof Order)) {
            throw new IllegalArgumentException("Order should be of type Order");
        }
        List<Map<String, Object>> lines = new ArrayList<>();
        for (var it : order.getOrderItems()) {
            if (it.getMedicineId() <= 0) {
                throw new IllegalArgumentException("Each order line must include a medicineId from the API inventory.");
            }
            Map<String, Object> line = new LinkedHashMap<>();
            line.put("medicineId", it.getMedicineId());
            line.put("quantity", it.getQuantity());
            line.put("unitPrice", it.getPrice());
            lines.add(line);
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("patientId", patientId);
        body.put("cashierId", null);
        body.put("status", order.getStatus());
        body.put("orderDate", null);
        body.put("items", lines);
        OptionalInt newId = postForInt("/api/orders", body);
        if (newId.isEmpty()) {
            return -1;
        }
        order.assignOrderIdFromApi(newId.getAsInt());
        return newId.getAsInt();
    }

    @Override
    public int DeleteOrder(int patientId,int orderId) throws IllegalArgumentException {
        if(Patient_Repository.getInstance().GetPatient(patientId) == null) {
            throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
        }
        if(GetById(orderId) == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        return delete("/api/orders/patient/" + patientId + "/" + orderId) ? 0 : -1;
    }

    @Override
    public int UpdateOrder(int patientId, Order Neworder)  throws IllegalArgumentException{
        Map<String, Object> patch = new LinkedHashMap<>();
        patch.put("status", Neworder.getStatus());
        patch.put("totalPrice", Neworder.getTotalPrice());
        return putJson("/api/orders/" + Neworder.getOrderId(), patch) ? 0 : -1;
    }

    @Override
    public List<Order> GetByName(String PatientName) throws IllegalArgumentException {
        Type t = BaseService.listOf(ApiModels.MPatient.class);
        List<ApiModels.MPatient> patients = (List<ApiModels.MPatient>) getJson("/api/patients", t).orElse(List.of());
        for (var p : patients) {
            if (p.username != null && p.username.equals(PatientName)) {
                return GetOrdersForPatient(p.userId);
            }
        }
        return null;
    }

    @Override
    public Order GetById(int orderId)  throws IllegalArgumentException {
        return getJson("/api/orders/" + orderId, ApiModels.MOrder.class)
                .map(PharmacyJsonMapper::toOrder)
                .orElse(null);
    }

    @Override
    public List<Order> GetHistory() {
        Type t = BaseService.listOf(ApiModels.MOrder.class);
        List<ApiModels.MOrder> list = (List<ApiModels.MOrder>) getJson("/api/orders/history", t).orElse(List.of());
        List<Order> out = new ArrayList<>();
        for (var o : list) {
            out.add(PharmacyJsonMapper.toOrder(o));
        }
        return out;
    }

    @Override
    public List<Order> GetOrdersForPatient(int patientId) {
        Type t = BaseService.listOf(ApiModels.MOrder.class);
        List<ApiModels.MOrder> list = (List<ApiModels.MOrder>) getJson("/api/orders/patient/" + patientId, t).orElse(List.of());
        List<Order> out = new ArrayList<>();
        for (var o : list) {
            out.add(PharmacyJsonMapper.toOrder(o));
        }
        return out;
    }
}