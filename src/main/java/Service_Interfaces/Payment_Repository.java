package Service_Interfaces;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.OptionalInt;

import Class_model.*;

import Http.ApiModels;
import Http.BaseService;

/**
 * PaymentRepository is an abstract interface that defines the contract for managing payment operations.
 * It provides methods to add, withdraw, update, and retrieve payment details.
 * NOTE : Payment Id should be the same as Order Id.
 */
abstract interface PaymentRepository {

    /**
     * Adds a new payment to the repository.
     * @param payment The Payment object to be added.
     * @return payment id on successs, -1 else
     */
    int AddPayment(int PatientId, Payment payment); 

    /**
     * Withdraws a payment from the repository using its unique identifier.
     * @param PatientId The unique identifier of the patient associated with the payment.
     * @param PaymentId The unique identifier of the payment to be withdrawn.
     * @return The status of the withdrawal operation. -1 if failed mostly because patient not found, 0 if successful.
     */
    int DeletePayment(int PatientId, int PaymentId); 

    /**
     * Updates a specific field of a payment record in the repository.
     * 
     * @param PatientId The unique identifier of the patient associated with the payment.
     * @param Newpayment The new payment to be updated.
     * @return status code of payment id on success, and -1 else.
     */
    int UpdatePayment(int PatientId, Payment Newpayment); 

    /**
     * Retrieves a payment from the repository by its unique identifier.
     * 
     * @return The Payment object corresponding to the given PaymentId.
     */
    List<Payment> GetById(int PatientId); 

    /**
     * Retrieves a list of all payments in the repository.
     * 
     * @return A list of all Payment objects.
     */
    List<Payment> GetAllPayments();
    
    /**
     * Retrieves a payment from the repository by its unique identifier.
     * 
     * @param PaymentId The unique identifier of the payment to be retrieved.
     * @return The Payment object corresponding to the given PaymentId. null if not found.
     */
    Payment GetPayment(int PaymentId); 
}

class Payment_Repository extends BaseService implements PaymentRepository {
    private static Payment_Repository instance = null;

    private Payment_Repository() {
    }

    public static PaymentRepository GetInstance(){
        if(instance == null){
            instance = new Payment_Repository();
        }
        return instance;
    }

    @Override
    public int AddPayment(int PatientId, Payment payment) {
        if (payment == null ) {
            throw new IllegalArgumentException("payment should be of type Payment");
        }
        int orderId = payment.getID();
        if(Order_Service.getInstance().GetById(orderId) == null) {
            throw new IllegalArgumentException("Order not found, you should add order first or check the id");
        }
        if(Patient_Repository.getInstance().GetPatient(PatientId) == null) {
            throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
        }
        var body = new LinkedHashMap<String, Object>();
        body.put("orderId", orderId);
        body.put("amount", payment.getAmount());
        body.put("paymentDate", null);
        body.put("paymentMethod", payment.getPaymethod());
        body.put("status", "Pending");
        OptionalInt newId = postForInt("/api/payments", body);
        if (newId.isEmpty()) {
            return -1;
        }
        payment.setID(newId.getAsInt());
        payment.setStatus("Pending");
        return newId.getAsInt();
    }

    @Override
    public int DeletePayment(int PatientId, int PaymentId) {
        if (Patient_Repository.getInstance().GetPatient(PatientId) == null) {
            throw new IllegalArgumentException("Patient not found, you should add patient first or check the id");
        }
        return delete("/api/payments/" + PaymentId) ? 0 : -1;
    }

    @Override
    public int UpdatePayment(int PatientId, Payment Newpayment) {
        var body = new LinkedHashMap<String, Object>();
        body.put("amount", Newpayment.getAmount());
        body.put("paymentDate", Newpayment.getPayday());
        body.put("paymentMethod", Newpayment.getPaymethod());
        body.put("status", Newpayment.getStatus());
        return putJson("/api/payments/" + Newpayment.getID(), body) ? Newpayment.getID() : -1;
    }

    @Override
    public List<Payment> GetById(int PatientId) {
        Type t = BaseService.listOf(ApiModels.MPayment.class);
        List<ApiModels.MPayment> list = (List<ApiModels.MPayment>) getJson("/api/payments/patient/" + PatientId, t).orElse(List.of());
        List<Payment> out = new ArrayList<>();
        for (var m : list) {
            out.add(mapPayment(m));
        }
        return out;
    }

    @Override  
    public List<Payment> GetAllPayments() {
        Type t = BaseService.listOf(ApiModels.MPayment.class);
        List<ApiModels.MPayment> list = (List<ApiModels.MPayment>) getJson("/api/payments", t).orElse(List.of());
        List<Payment> out = new ArrayList<>();
        for (var m : list) {
            out.add(mapPayment(m));
        }
        return out;
    }

    @Override
    public Payment GetPayment(int PaymentId) {
        return getJson("/api/payments/" + PaymentId, ApiModels.MPayment.class)
                .map(this::mapPayment)
                .orElse(null);
    }

    private Payment mapPayment(ApiModels.MPayment m) {
        var order = Order_Repository.getInstance().GetById(m.orderId);
        if (order == null) {
            order = new Order.builder()
                    .setOrderId(m.orderId)
                    .setOrderItems(new ArrayList<>())
                    .setTotalPrice(0)
                    .setStatus("")
                    .setOrderDate("")
                    .build();
        }
        String payday = m.paymentDate != null ? m.paymentDate : "";
        Payment p = new Payment(m.paymentId, m.amount, payday, m.paymentMethod != null ? m.paymentMethod : "", order);
        if (m.status != null) {
            p.setStatus(m.status);
        }
        return p;
    }
}
