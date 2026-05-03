package Service_Interfaces;

import Class_model.*;

import java.util.List;

/**
 * The PaymentServiceInterface defines the contract for payment processing services.
 * It includes methods for processing payments and generating receipts.
 * NOTE : Cancelled payments are not removed from the repository, but their status is updated, and their ID is set to -1. 
 *       withdrawal is differnt from deletion in Payment_Repository.
 * NOTE : Payment Id should be the same as Order Id.
 */
abstract interface PaymentServiceInterface {

    /**
     * Adds a new payment to the repository.
     * 
     * @param payment The Payment object to be added.
     * @return payment id on success, -1 else.
     */
    int AddPayment(int PatientId, Payment payment); 

    /**
     * Withdraws a payment from the repository using its unique identifier. marks the payment with id of -1.
     * @param PatientId The unique identifier of the patient associated with the payment.
     * @param PaymentId The unique identifier of the payment to be withdrawn.
     * @return The status of the withdrawal operation. -1 if failed, 0 if successful.
     */
    int WithdrawPayment(int PatientId,int PaymentId); 
    
    /**
     * Processes a payment based on the provided payment ID.
     * @param PatientId The unique identifier of the patient making the payment.
     * @param OrderId The unique identifier of the order associated with the payment.PaymentId is the same as OrderId.
     * @return The status of the payment processing operation. Payment id if successful, -1 if failed.
     */
    int ProcessPayment(int PatientId, int OrderId);

    /**
     * Generates a receipt for a given payment ID.
     *
     * @param paymentId The unique identifier of the payment for which the receipt is generated.
     * @return A string representation of the receipt on success , null on fail.
     */
    String generateReceipt(int paymentId);

    /**
     * Updates a specific field of a payment record in the repository.
     * 
     * @param PatientId The unique identifier of the patient associated with the payment.
     * @param query The field to be updated.
     * @param value The new value to be set for the specified field, only 3 queries are supported for now -> "amount" , "payday", "paymethod".
     * @return status code of payment id on success , and -1 else.
     */
    int UpdatePayment(int PatientId, String query, Object value);
}

public class Payment_service implements PaymentServiceInterface{

    /**
     * singleton design for less memory usage, only 1 object is needed.
     */
    private static Payment_service instance;

    private Payment_service() {
        // private constructor to prevent instantiation
    }

    public static Payment_service getInstance() {
        if (instance == null) {
            instance = new Payment_service();
        }
        return instance;
    }

    //works fine
    @Override
    public int AddPayment(int PatientId, Payment payment) {
        // Implementation for adding a payment
        try {
            return Payment_Repository.GetInstance().AddPayment(PatientId, payment);
        } catch (Exception e) {
            System.out.println("error adding payment: " + e.getMessage());
            return -1;
            // handle exception
        }
        
    }  

    //works fine
    @Override
    public int WithdrawPayment(int PatientId, int PaymentId) {
        try {
            return Payment_Repository.GetInstance().DeletePayment(PatientId,PaymentId);// Payment successfully withdrawn
            
        }catch (Exception e) {
            // handle exception
            System.out.println("error deleting payment: " + e.getMessage());
            return -1;
        }
    }

    //works fine
    @Override
    public String generateReceipt(int paymentId) {
        // Implementation for generating a receipt
        Payment payment = Payment_Repository.GetInstance().GetPayment(paymentId);
        if(payment != null) {
            return payment.toString();
        }
        return "no Receipt found for this payment";
    }

    //works fine
    @Override
    public int UpdatePayment(int PatientId, String query, Object value) {
        // First argument is the payment row id (legacy parameter name retained for interface stability).
        Payment payment = Payment_Repository.GetInstance().GetPayment(PatientId);
        if (payment == null || query == null) return -1;
        switch (query) {
            case "amount":
                if (value instanceof Double) {
                    payment.setAmount((Double) value);
                }
                if (value instanceof Integer) {
                    payment.setAmount((Integer) value);
                }
                break;
            case "payday":
                if (value instanceof String) {
                    payment.setPayday((String) value);
                }
                break;
            case "paymethod":
                if (value instanceof String) {
                    payment.setPaymethod((String) value);
                }
                break;
            default:
                return -1;
        }
        return Payment_Repository.GetInstance().UpdatePayment(PatientId, payment);
    }

    /**
     * {@code orderOrPaymentId} may be either the {@code OrderID} (legacy UI) or the {@code PaymentID} from the API.
     */
    public int ProcessPayment(int PatientId, int orderOrPaymentId){
        Payment temppay = Payment_Repository.GetInstance().GetPayment(orderOrPaymentId);
        if (temppay == null) {
            for (Payment p : Payment_Repository.GetInstance().GetById(PatientId)) {
                if (p.getOrder() != null && p.getOrder().getOrderId() == orderOrPaymentId) {
                    temppay = p;
                    break;
                }
            }
        }
        Patient tempPatient = Patient_Repository.getInstance().GetPatient(PatientId);
        if(tempPatient == null  || temppay == null) {
            return -1;
        }

        if(tempPatient.GetBalance() >= temppay.getOrder().getTotalPrice()){
            if("Paid".equals(temppay.getStatus()) || "Cancelled".equals(temppay.getStatus())){
                return -1;
            }
            temppay.setStatus("Paid");
            tempPatient.SetBalance(tempPatient.GetBalance() - temppay.getOrder().getTotalPrice());
            int orderId = temppay.getOrder().getOrderId();
            var ord = Order_Repository.getInstance().GetById(orderId);
            if (ord != null) {
                ord.setCheckedBy("Casher");
                ord.setStatus("Paid");
                Order_Repository.getInstance().UpdateOrder(PatientId, ord);
            }
            Payment_Repository.GetInstance().UpdatePayment(PatientId, temppay);
            Patient_Repository.getInstance().UpdatePatient(PatientId, tempPatient);
            Admin.setTotalIncome(Admin.gettotalIncome() + (tempPatient.GetBalance() - temppay.getOrder().getTotalPrice()));
            return temppay.getID();
        }
        else{
            return -1;
        }
    }
    public List<Payment> GetById(int PatientId) {
        // Implementation to get a payment by ID
        return Payment_Repository.GetInstance().GetById(PatientId);
    }
}

/*
 * // Implementation for adding a payment
        if(payment == null || Order_Service.getInstance().GetById(OrderId) == null || Patient_Repository.getInstance().GetPatient(PatientId) == null)
            return -1; //failed
        payment.setID(OrderId);
        payment.setStatus("Pending");
        payment.setOrder(Order_Repository.getInstance().GetById(OrderId));
        payment.setAmount(Order_Repository.getInstance().GetById(OrderId).getTotalPrice());
        Payment_Repository.GetInstance().AddPayment(PatientId, payment);
        Order_Repository.getInstance().GetById(OrderId).setStatus("Pending");

        return payment.getID();
 */